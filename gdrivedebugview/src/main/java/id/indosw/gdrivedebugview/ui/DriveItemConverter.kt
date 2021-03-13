package id.indosw.gdrivedebugview.ui

import id.indosw.gdrivedebugview.ui.viewObject.DriveActionItem
import id.indosw.gdrivedebugview.ui.viewObject.DriveItem

class DriveItemConverter {

    fun addDriveItem(driveId: String?, title: String?, icon: Int?, mimeType: String?,fileSize: String?,lastUpdate: String?): DriveItem {
        val driveItem = DriveItem()
        driveItem.driveId = driveId
        driveItem.title = title
        driveItem.icon = icon
        driveItem.mimeType = mimeType
        driveItem.fileSize = fileSize
        driveItem.lastUpdate = lastUpdate
        return driveItem
    }

    fun addDriveActionItem(title: String?, icon: Int?): DriveActionItem {
        val driveItem = DriveActionItem()
        driveItem.title = title
        driveItem.icon = icon
        return driveItem
    }
}