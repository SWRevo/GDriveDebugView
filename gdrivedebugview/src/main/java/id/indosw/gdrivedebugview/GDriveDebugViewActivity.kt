package id.indosw.gdrivedebugview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.indosw.gdrivedebugview.ui.FileInfoDialogFragment
import id.indosw.gdrivedebugview.ui.GdriveDebugViewFragment
import id.indosw.gdrivedebugview.ui.viewObject.CreateFolderFragment

class GDriveDebugViewActivity : AppCompatActivity(), FileInfoDialogFragment.OnFragmentInteractionListener,
    CreateFolderFragment.OnFragmentInteractionListener {
    private lateinit var fragment: GdriveDebugViewFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gdrive_debug_view_activity)
        if (savedInstanceState == null) {
            fragment = GdriveDebugViewFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow()
        }
    }
    override fun onBackPressed() {
        if (fragment.isAdded) {
            if (fragment.canGoBack()) {
                super.onBackPressed()
            } else {
                fragment.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
    override fun onDelete(driveId: String) {
        fragment.onDelete(driveId)
    }
    override fun onDownload(driveId: String) {
        fragment.onDownload(driveId)
    }
    override fun onCreateFolderDialog(folderName: String) {
        fragment.onCreateFolder(folderName)
    }
}