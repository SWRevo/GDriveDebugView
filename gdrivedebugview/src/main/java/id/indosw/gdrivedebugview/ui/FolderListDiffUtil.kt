package id.indosw.gdrivedebugview.ui

import androidx.recyclerview.widget.DiffUtil
import id.indosw.gdrivedebugview.ui.viewObject.DriveActionItem
import id.indosw.gdrivedebugview.ui.viewObject.DriveItem
import id.indosw.gdrivedebugview.ui.viewObject.RecycleViewBaseItem

class FolderListDiffUtil(
        private var oldList: ArrayList<RecycleViewBaseItem>,
        private var newList: ArrayList<RecycleViewBaseItem>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if (oldList[oldItemPosition] is DriveItem && newList[newItemPosition] is DriveItem) {
            true
        } else oldList[oldItemPosition] is DriveActionItem && newList[newItemPosition] is DriveActionItem
    }
    override fun getOldListSize(): Int {
        return oldList.size
    }
    override fun getNewListSize(): Int {
        return newList.size
    }
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if (oldList[oldItemPosition] is DriveItem && newList[newItemPosition] is DriveItem) {
            ((oldList[oldItemPosition] as DriveItem).title == (newList[newItemPosition] as DriveItem).title) && (oldList[oldItemPosition] as DriveItem).driveId == (newList[newItemPosition] as DriveItem).driveId
        } else if (oldList[oldItemPosition] is DriveActionItem && newList[newItemPosition] is DriveActionItem) {
            ((oldList[oldItemPosition] as DriveActionItem).title == (newList[newItemPosition] as DriveActionItem).title)
        } else {
            false
        }
    }
}