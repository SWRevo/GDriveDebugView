package id.indosw.gdrivedebugview.ui.viewObject

import id.indosw.gdrivedebugview.ui.DriveItemType

class DriveItem :
    RecycleViewBaseItem(DriveItemType.DRIVE_ITEM_TYPE) {
    var driveId: String? = null
    var title: String? = null
    var icon: Int? = null
    var mimeType: String? = null
    var fileSize: String? = null
    var lastUpdate: String? = null
}
