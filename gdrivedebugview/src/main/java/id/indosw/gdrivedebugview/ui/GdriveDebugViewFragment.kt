@file:Suppress("DEPRECATION")

package id.indosw.gdrivedebugview.ui

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.drive.Drive
import com.google.api.client.http.InputStreamContent
import id.indosw.gdrivedebugview.R
import id.indosw.gdrivedebugview.databinding.GdriveDebugViewFragmentBinding
import id.indosw.gdrivedebugview.ui.dataClass.DriveHolder
import id.indosw.gdrivedebugview.ui.viewObject.CreateFolderFragment
import id.indosw.gdrivedebugview.ui.viewObject.DriveItem
import id.indosw.gdrivedebugview.ui.viewObject.RecycleViewBaseItem
import id.indosw.gdriverest.DriveServiceHelper
import id.indosw.gdriverest.DriveServiceHelper.Companion.getGoogleDriveService
import java.io.File
import java.text.DecimalFormat

@Suppress("DEPRECATION")
class GdriveDebugViewFragment : Fragment() {

    companion object {
        fun newInstance() = GdriveDebugViewFragment()
        private const val REQUEST_CODE_SIGN_IN = 100
        private const val OPEN_FILE_PICKER_REQUEST_CODE = 101
        private const val REQUEST_READ_STORAGE = 102
    }
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var viewModel: GdriveDebugViewViewModel
    private lateinit var driveServiceHelper: DriveServiceHelper
    private lateinit var adapter: DriveItemListAdapter
    private var drivePathHolder: ArrayList<DriveHolder?> = ArrayList()
    var recycleItemArrayList = ArrayList<RecycleViewBaseItem>()

    private var _binding: GdriveDebugViewFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = GdriveDebugViewFragmentBinding.inflate(inflater, container, false)
        val rootView = binding.root
        drivePathHolder.add(DriveHolder(null, "Root"))
        binding.folderList.hasFixedSize()
        binding.folderList.layoutManager = GridLayoutManager(context, 4)
        adapter = DriveItemListAdapter(recycleItemArrayList, object :
            DriveItemListAdapter.addClickListener {
            override fun onFileLongClick(position: Int) {
                val driveItem = recycleItemArrayList[position] as DriveItem
                val infoDialogFragment = FileInfoDialogFragment.newInstance(driveItem.driveId!!, drivePathHolder.getPath(), driveItem.mimeType!!, driveItem.title!!, "Size: " + driveItem.fileSize!!.toLong().bytesToMeg(), "Last Update:" + driveItem.lastUpdate!!)
                infoDialogFragment.show(childFragmentManager, "driveInfoDialog")
            }
            override fun onFolderLongClick(position: Int) {
                val driveItem = recycleItemArrayList[position] as DriveItem
                val infoDialogFragment = FileInfoDialogFragment.newInstance(driveItem.driveId!!, drivePathHolder.getPath(), driveItem.mimeType!!, driveItem.title!!, driveItem.fileSize!!.toLong().bytesToMeg(), "Last Update:" + driveItem.lastUpdate!!)
                infoDialogFragment.show(childFragmentManager, "driveInfoDialog")
            }
            override fun onBackClick(position: Int) {
                Log.d("test", "back to " + drivePathHolder[drivePathHolder.size - 2]?.driveId)
                queryDrive(drivePathHolder[drivePathHolder.size - 2]?.driveId)
                drivePathHolder.removeAt(drivePathHolder.size - 1)
                updateTitle()
            }
            override fun onFolderClick(position: Int) {
                Log.d("test", "select to " + (recycleItemArrayList[position] as DriveItem).driveId)
                queryDrive((recycleItemArrayList[position] as DriveItem).driveId)
                drivePathHolder.add(DriveHolder((recycleItemArrayList[position] as DriveItem).driveId, (recycleItemArrayList[position] as DriveItem).title))
                updateTitle()
            }
            override fun onFileClick(position: Int) {
            }
        })

        binding.addButton.setOnClickListener {
            if (binding.createFolder.visibility == View.VISIBLE) {
                toggleMenu(false)
            } else {
                toggleMenu(true)
            }
        }
        binding.createFolder.setOnClickListener {
            val createFolderFragment = CreateFolderFragment.newInstance()
            createFolderFragment.show(childFragmentManager, "createFolderDialog")
            toggleMenu(false)
        }
        binding.uploadFileButton.setOnClickListener {
            val checkSelfPermission = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_STORAGE)
            } else {
                openFilePicker()
            }
            toggleMenu(false)
        }
        binding.folderList.adapter = adapter
        binding.folderList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && binding.addButton.isShown) {
                    binding.addButton.hide()
                } else if (dy < 0 && !binding.addButton.isShown) {
                    binding.addButton.show()
                }
            }
            //override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                //if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //binding.addButton.show()
                //}
                //super.onScrollStateChanged(recyclerView, newState)
            //}
        })
        return rootView
    }
    private fun toggleMenu(isShow: Boolean) {
        if (isShow) {
            binding.createFolder.visibility = View.VISIBLE
            binding.uploadFile.visibility = View.VISIBLE
        } else {
            binding.createFolder.visibility = View.GONE
            binding.uploadFile.visibility = View.GONE
        }
    }
    fun ArrayList<DriveHolder?>.getPath(): String {
        val stringBuilder = java.lang.StringBuilder()
        stringBuilder.append("Path:")
        val listItem = this
        for (i in listItem.indices) {
            stringBuilder.append(listItem[i]?.driveTitle)
            if (i != (listItem.size)) {
                stringBuilder.append("/")
            }
        }
        return stringBuilder.toString()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_READ_STORAGE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openFilePicker()
                }
            }
        }
    }
    fun canGoBack(): Boolean {
        return drivePathHolder.size == 1
    }
    fun onBackPressed() {
        if (drivePathHolder.size > 1) {
            queryDrive(drivePathHolder[drivePathHolder.size - 2]?.driveId)
            drivePathHolder.removeAt(drivePathHolder.size - 1)
            updateTitle()
        }
    }
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
            addCategory(Intent.CATEGORY_OPENABLE)
            // Filter to show only images, using the image MIME data type.
            // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
            // To search for all documents available via installed storage providers,
            // it would be "*/*".
            type = "*/*"
        }
        startActivityForResult(intent, OPEN_FILE_PICKER_REQUEST_CODE)
    }
    private fun updateTitle() {
        val drivePath = StringBuilder()
        for (name in drivePathHolder) {
            drivePath.append(name?.driveTitle).append("/")
        }
        binding.toolbar.title = drivePath.toString()
    }
    override fun onStart() {
        super.onStart()
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (lastSignedInAccount == null) {
            signIn()
        } else {
            driveServiceHelper = DriveServiceHelper(getGoogleDriveService(context, lastSignedInAccount, "DebugView"))
            queryDrive(drivePathHolder.last()?.driveId)
        }
        updateTitle()
    }
    private fun queryDrive(driveId: String?) {
        binding.progressBar.visibility = View.VISIBLE
        driveServiceHelper.queryFiles(driveId).addOnSuccessListener {
            binding.progressBar.visibility = View.GONE
            val newList = ArrayList<RecycleViewBaseItem>()
            if (drivePathHolder.size > 1) {
                newList.add(DriveItemConverter().addDriveActionItem("back", R.drawable.ic_arrow_back))
            }
            for (fileItem in it) {
                Log.d("test", "item id " + fileItem.id)
                newList.add(DriveItemConverter().addDriveItem(fileItem.id, fileItem.name,
                    fileItem.mimeType?.let { it1 -> getFileOrFolderIcon(it1) }, fileItem.mimeType, fileItem.size.toString(), fileItem.modifiedTime.toString()))
            }
            val folderListDiffUtil = FolderListDiffUtil(recycleItemArrayList, newList)
            val calculateDiff = DiffUtil.calculateDiff(folderListDiffUtil)
            recycleItemArrayList.clear()
            recycleItemArrayList.addAll(newList)
            calculateDiff.dispatchUpdatesTo(adapter)
        }
    }
    private fun getFileOrFolderIcon(mimeType: String): Int {
        return when (mimeType) {
            DriveServiceHelper.TYPE_GOOGLE_DRIVE_FOLDER -> {
                R.drawable.ic_folder_vd
            }
            else -> {
                R.drawable.ic_file_vd
            }
        }
    }
    @Suppress("PrivatePropertyName")
    private val MEGABYTE = (1024L * 1024L).toDouble()
    fun Long.bytesToMeg(): String {
        return DecimalFormat("##.##").format(this / MEGABYTE) + "MB"
    }
    private fun signIn() {
        mGoogleSignInClient = buildGoogleSignInClient()
        startActivityForResult(buildGoogleSignInClient().signInIntent, REQUEST_CODE_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> if (resultCode == Activity.RESULT_OK && resultData != null) {
                handleSignInResult(resultData)
            }
            OPEN_FILE_PICKER_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    binding.uploadProgressBar.visibility = View.VISIBLE
                    Toast.makeText(context, "Uploading", Toast.LENGTH_SHORT).show()
                    val uri = resultData.data
                    val contentResolver = context!!.contentResolver
                    var name = "file_name"
                    val cursor = contentResolver.query(uri!!, null, null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        name = cursor.getString(nameIndex)
                    }
                    cursor?.close()
                    val extensionFromMimeType = MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(contentResolver.getType(uri))
                    val root: List<String> = if (drivePathHolder.last()?.driveId == null) {
                        listOf("root")
                    } else {
                        listOf(drivePathHolder.last()?.driveId!!)
                    }
                    val metadata = com.google.api.services.drive.model.File()
                        .setParents(root)
                        .setMimeType(extensionFromMimeType)
                        .setName(name)
                    val inputStreamContent = InputStreamContent(null, contentResolver.openInputStream(uri))
                    driveServiceHelper.uploadFile(metadata, inputStreamContent)
                        .addOnSuccessListener {
                            binding.uploadProgressBar.visibility = View.GONE
                            if (drivePathHolder.last()?.driveId != null) {
                                queryDrive(drivePathHolder.last()?.driveId)
                            } else {
                                queryDrive(null)
                            }
                        }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }
    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleSignInAccount ->
                driveServiceHelper = DriveServiceHelper(getGoogleDriveService(context, googleSignInAccount, "DebugView"))
                queryDrive(drivePathHolder.last()?.driveId)
            }
            .addOnFailureListener { e -> Log.e(TAG, "Unable to sign in.", e) }
    }
    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Drive.SCOPE_FILE)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context!!, signInOptions)
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GdriveDebugViewViewModel::class.java)
    }
    fun onDelete(driveId: String) {
        binding.progressBar.visibility = View.VISIBLE
        driveServiceHelper.deleteFolderFile(driveId)
            .addOnSuccessListener { queryDrive(drivePathHolder.last()?.driveId) }
    }
    fun onCreateFolder(folderName: String) {
        binding.progressBar.visibility = View.VISIBLE
        driveServiceHelper.createFolder(folderName, drivePathHolder.last()?.driveId)
            .addOnSuccessListener {
                queryDrive(drivePathHolder.last()?.driveId)
                //drivePathHolder.add(DriveHolder(it.id, folderName))
                //updateTitle()
            }
    }
    fun onDownload(driveId: String) {
        binding.progressBar.visibility = View.VISIBLE
        driveServiceHelper.downloadFile(File(context!!.filesDir!!, "test.jpg"), driveId)
            .addOnSuccessListener {
                Toast.makeText(context, "download complete", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }
}
