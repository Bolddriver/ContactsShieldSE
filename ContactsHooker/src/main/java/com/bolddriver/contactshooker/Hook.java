package com.bolddriver.contactshooker;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.CancellationSignal;
import android.util.Log;

import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    private static String TAG = "contactsBold";
    private static final int sdk = android.os.Build.VERSION.SDK_INT;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //TODO 包名
        if (loadPackageParam.packageName.equals("com.bolddriver.contactsreader")) {
            //Hook query方法，修改参数中的uri为模块的uri
            XposedHelpers.findAndHookMethod(ContentResolver.class, "query", Uri.class, String[].class, String.class, String[].class, String.class, CancellationSignal.class, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG, "=====before: arg0: " + param.args[0] + "arg4: " + param.args[4]);
                    param.args[0] = Uri.parse("content://com.bolddriver.contactshooker.provider.ContactsInfoProvider/contacts");
                }
            });

        }

        //为被hook的应用开启权限，以便能够访问模块的ContentProvider
        if (loadPackageParam.packageName.equals("android")) {
            String targetPkgName = "com.bolddriver.contactsreader"; // Replace this with the target app package name
            String[] newPermissions = new String[]{ // Put the new permissions here
                    "android.permission.QUERY_ALL_PACKAGES"
            };

            Class<?> PermissionManagerService = XposedHelpers.findClass(
                    sdk >= 33 /* android 13+ */ ?
                            "com.android.server.pm.permission.PermissionManagerServiceImpl" :
                            "com.android.server.pm.permission.PermissionManagerService", loadPackageParam.classLoader);
            Class<?> AndroidPackage = XposedHelpers.findClass(
                    "com.android.server.pm.parsing.pkg.AndroidPackage", loadPackageParam.classLoader);
            Class<?> PermissionCallback = XposedHelpers.findClass(
                    sdk >= 33 /* android 13+ */ ?
                            "com.android.server.pm.permission.PermissionManagerServiceImpl$PermissionCallback" :
                            "com.android.server.pm.permission.PermissionManagerService$PermissionCallback", loadPackageParam.classLoader);

            // PermissionManagerService(Impl) - restorePermissionState
            XposedHelpers.findAndHookMethod(PermissionManagerService, "restorePermissionState",
                    AndroidPackage, boolean.class, String.class, PermissionCallback, int.class, new XC_MethodHook() {

                        @SuppressWarnings("unchecked")
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                            // params
                            Object pkg = param.args[0];
                            int filterUserId = (int) param.args[4];

                            // obtém os campos
                            Object mState = XposedHelpers.getObjectField(param.thisObject, "mState");
                            Object mRegistry = XposedHelpers.getObjectField(param.thisObject, "mRegistry");
                            Object mPackageManagerInt = XposedHelpers.getObjectField(param.thisObject, "mPackageManagerInt");

                            // Continua ?
                            String packageName = (String) XposedHelpers.callMethod(pkg, "getPackageName");
                            Object ps = XposedHelpers.callMethod(mPackageManagerInt,
                                    sdk >= 33 /* android 13+ */ ?
                                            "getPackageStateInternal" :
                                            "getPackageSetting", packageName);
                            if (ps == null)
                                return;

                            int[] getAllUserIds = (int[]) XposedHelpers.callMethod(param.thisObject, "getAllUserIds");
                            int userHandle_USER_ALL = XposedHelpers.getStaticIntField(Class.forName("android.os.UserHandle"), "USER_ALL");
                            final int[] userIds = filterUserId == userHandle_USER_ALL ? getAllUserIds : new int[]{filterUserId};

                            for (int userId : userIds) {

                                List<String> requestedPermissions;
                                Object userState = XposedHelpers.callMethod(mState, "getOrCreateUserState", userId);
                                int appId = (int) XposedHelpers.callMethod(ps, "getAppId");
                                Object uidState = XposedHelpers.callMethod(userState, "getOrCreateUidState", appId);

                                // package 1
                                if (packageName.equals("com.bolddriver.contactsreader")) {
                                    requestedPermissions = (List<String>) XposedHelpers.callMethod(pkg, "getRequestedPermissions");
                                    for (String i : requestedPermissions) {
                                        Log.d(TAG, "requestedPermission: " + i);
                                    }
                                    grantInstallOrRuntimePermission(requestedPermissions, uidState, mRegistry, "android.permission.QUERY_ALL_PACKAGES");
                                }
                            }
                        }
                    });
        }
    }

    private static void grantInstallOrRuntimePermission(List<String> requestedPermissions, Object uidState,
                                                        Object registry, String permission) {
        if (!requestedPermissions.contains(permission)) {
            XposedHelpers.callMethod(uidState, "grantPermission", XposedHelpers.callMethod(registry, "getPermission", permission));
        }
    }
}
