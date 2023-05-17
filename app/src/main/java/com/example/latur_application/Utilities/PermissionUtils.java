package com.example.latur_application.Utilities;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    // TAG
    private static final String TAG = "PermissionUtils";
    // Storage Permission
    public static final int STORAGE_PERMISSION = 1;
    // Storage Permission above Android 11
    public static final int ANDROID_11_STORAGE_PERMISSION = 333;

    public static  final int ANDROID_10_STORAGE_PERMISSION = 111;
    // Permission Granted
    public static final String PERMISSION_GRANTED = "Permission Granted";
    // Permission not Granted
    public static final String PERMISSION_NOT_GRANTED = "Permission Granted";
    public static final String PERMISSION_ALREADY_GRANTED = "Permission Already Granted";

    // Need Permission Alert
    public static final String NEED_MANAGEMENT_STORAGE_PERMISSION_ALERT  = "Need Storage Permission to Store File. \n\nGo to Setting -> App info -> Mine Survey -> Permissions -> Allow Storage -> Allow Management of All Files";


//---------------------------------------------- Storage Permission ------------------------------------------------------------------------------------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static boolean isInternalStorage(Context context){
        // Above Android 11 Storage permission
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        // Below Android 11 Storage permission
        else
        {
            boolean read_external_storage_permission  = (ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED);
            boolean write_external_storage_permission = (ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            return read_external_storage_permission && write_external_storage_permission ;
        }
    }

    // Android 11 Permission
    public static void requestPermissionForAndroid_11_Above_And_Below_Versions(Activity activity){

        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",activity.getApplicationContext().getPackageName())));
                activity.startActivityForResult(intent,333);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(intent, 333);
            }
        } else {
            //below android 11
            if (SDK_INT >= Build.VERSION_CODES.R) {
                requestPermissionsStorage(activity);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static void requestPermissionsStorage(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{MANAGE_EXTERNAL_STORAGE}, ANDROID_10_STORAGE_PERMISSION);
        ActivityCompat.requestPermissions(activity,new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);
    }

    public static boolean onStorageRequestPermissionsResult(int[] grantResults){
        // Permission Granted or Not
            boolean READ_EXTERNAL_STORAGE  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            return READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE;
    }


}
