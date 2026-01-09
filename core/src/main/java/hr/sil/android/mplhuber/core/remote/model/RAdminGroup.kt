package hr.sil.android.mplhuber.core.remote.model

data class RAdminGroup (var groupId: Long, var adminGroupList: List<RGroupInfo>) {
    constructor() : this(-1, listOf())
}