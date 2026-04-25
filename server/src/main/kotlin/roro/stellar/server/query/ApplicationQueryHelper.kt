package roro.stellar.server.query

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import rikka.hidden.compat.PackageManagerApis
import rikka.hidden.compat.UserManagerApis
import rikka.parcelablelist.ParcelableListSlice
import roro.stellar.server.ConfigManager
import roro.stellar.server.ServerConstants.MANAGER_APPLICATION_ID
import roro.stellar.server.shizuku.ShizukuApiConstants
import roro.stellar.server.util.ProviderDiscovery

object ApplicationQueryHelper {
    private const val SHIZUKU_MANAGER_PERMISSION = "moe.shizuku.manager.permission.MANAGER"

    fun getApplications(userId: Int, configManager: ConfigManager): ParcelableListSlice<PackageInfo?> {
        val list = ArrayList<PackageInfo?>()
        val users = ArrayList<Int?>()
        if (userId == -1) {
            users.addAll(UserManagerApis.getUserIdsNoThrow())
        } else {
            users.add(userId)
        }

        for (user in users) {
            for (pi in PackageManagerApis.getInstalledPackagesNoThrow(
                (PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS or PackageManager.GET_PROVIDERS).toLong(),
                user!!
            )) {
                if (MANAGER_APPLICATION_ID == pi.packageName) continue
                if (pi.requestedPermissions?.contains(SHIZUKU_MANAGER_PERMISSION) == true) continue
                val applicationInfo = pi.applicationInfo ?: continue
                val uid = applicationInfo.uid
                if (
                    applicationInfo.metaData?.getBoolean(ShizukuApiConstants.META_DATA_KEY, false) == true ||
                    ProviderDiscovery.hasShizukuProvider(pi) ||
                    configManager.find(uid)?.permissions?.containsKey(ShizukuApiConstants.PERMISSION_NAME) == true
                ) {
                    list.add(pi)
                }
            }
        }
        return ParcelableListSlice(list)
    }
}
