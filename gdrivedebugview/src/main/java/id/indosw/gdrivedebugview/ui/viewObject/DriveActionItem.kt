package id.indosw.gdrivedebugview.ui.viewObject

import id.indosw.gdrivedebugview.ui.DriveItemType
class DriveActionItem :
    RecycleViewBaseItem(DriveItemType.DRIVE_ACTION_TYPE) {
    var title: String? = null
    var icon: Int? = null
}