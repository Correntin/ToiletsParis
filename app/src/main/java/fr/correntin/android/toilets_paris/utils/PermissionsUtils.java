package fr.correntin.android.toilets_paris.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utils class to manage permissions<br/>
 * Created by Corentin on 21/11/2016.
 */

public final class PermissionsUtils {

    private static final String BLOCKED_PERMISSIONS_PREFERENCES_FILE = "blockedPermissions";
    private static final String BLOCKED_PERMISSIONS_KEY = "blockedPermissions";

    private PermissionsUtils() {
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasPermission(Context context, String permission) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        return true;
    }

    public static void askPermissions(Activity o, int permissionId, String... permissions) {

        List<String> permissionsRequested = new ArrayList<>();
        Set<String> blockedPermissions = PermissionsUtils.getBlockedPermissions(o);

        for (String permissionRequested : permissions) {
            if (hasPermission(o, permissionRequested) == false && blockedPermissions.contains(permissionRequested) == false)
                permissionsRequested.add(permissionRequested);
        }

        if (permissionsRequested.size() > 0)
            ActivityCompat.requestPermissions((Activity) o, permissionsRequested.toArray(new String[permissionsRequested.size()]), permissionId);
    }

    public static boolean canAskPermission(Context context, String permission) {
        final Set<String> blockedPermissions = getBlockedPermissions(context);

        return blockedPermissions != null && !blockedPermissions.contains(permission);
    }

    public static boolean canAskPermissions(Context context, String... permissions) {
        final Set<String> blockedPermissions = getBlockedPermissions(context);

        for (String permission : permissions) {
            if (blockedPermissions != null && blockedPermissions.contains(permission))
                return false;
        }
        return true;
    }

    private static void saveBlockedPermission(Context context, String... permissions) {

        if (permissions != null && permissions.length >= 1) {
            final SharedPreferences sp = context.getSharedPreferences(BLOCKED_PERMISSIONS_PREFERENCES_FILE, Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = sp.edit();

            Set<String> blockedPermissions = sp.getStringSet(BLOCKED_PERMISSIONS_KEY, new HashSet<String>());

            for (String permission : permissions)
                blockedPermissions.add(permission);

            editor.putStringSet(BLOCKED_PERMISSIONS_KEY, blockedPermissions);
            editor.commit();
        }
    }

    public static void checkResultAndSaveBlockedPermissionsIfNeeded(Activity activity, String... permissions) {

        final ArrayList<String> blockedPermissions = new ArrayList<>();
        for (String permission : permissions) {

            if (hasPermission(activity, permission) == false && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) == false) {
                blockedPermissions.add(permission);
            }
        }

        if (blockedPermissions.size() >= 1)
            saveBlockedPermission(activity, blockedPermissions.toArray(new String[blockedPermissions.size()]));
    }

    private static Set<String> getBlockedPermissions(Context context) {
        final SharedPreferences sp = context.getSharedPreferences(BLOCKED_PERMISSIONS_PREFERENCES_FILE, Context.MODE_PRIVATE);
        final Set<String> blockedPermissions = sp.getStringSet(BLOCKED_PERMISSIONS_KEY, new HashSet<String>());

        return blockedPermissions;
    }

    public static List<String> getPermissionsAsList(String... permissions) {
        List<String> permissionsAsList = new ArrayList<>();

        if (permissions != null && permissions.length > 0) {
            for (String permission : permissions)
                permissionsAsList.add(permission);
        }

        return permissionsAsList;
    }
}

