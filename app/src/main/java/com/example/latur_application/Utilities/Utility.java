package com.example.latur_application.Utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Toast;

public class Utility {
    //Login
    public static final String IS_USER_SUCCESSFULLY_LOGGED_IN = "IS_USER_SUCCESSFULLY_LOGGED_IN";

    //Pref-key
    private final static String PREF_KEY = "LaturApp";

    // Internet Connection Constants
    public static final String NO_NETWORK_CONNECTED = "No Network Connected";
    public static final String NETWORK_CONNECTED    = "Network Connected";
    public static final String ERROR_MESSAGE = "something is wrong";
    public static final String OLD_DATE = "old_date";



    //----------------------Shared Preferences-----------------------------//
    public static void setPermissionDenied(Context ctx, String keyPermission, boolean isDenied) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_KEY, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(keyPermission, isDenied);
        editor.apply();
    }
    public static boolean isPermissionDenied(Context ctx, String keyPermission) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_KEY, 0);
        return prefs.getBoolean(keyPermission, false);
    }
    public static boolean getBooleanSavedData(Context ctx, String TAG) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_KEY, 0);
        return prefs.getBoolean(TAG, false);
    }
    public static String getSavedData(Context ctx, String TAG) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_KEY, 0);
        return prefs.getString(TAG, "");
    }
    public static void saveData(Context ctx, String TAG, String data) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_KEY, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TAG, data);
        editor.apply();
    }
    public static void clearData(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_KEY, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    //------------------------------------------------------- Toast ----------------------------------------------------------------------------------------------------------------

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
    //------------------------------------------------------- isEmptyString ----------------------------------------------------------------------------------------------------------------

    public static boolean isEmptyString(String str) {
        if (str == null) {
            return true;
        } else if (str.isEmpty()) {
            return true;
        } else return str.equalsIgnoreCase("null");
    }

    public static String getStringValue(String value){
        return isEmptyString(value) ? "" : value;
    }

    public static String getEditTextValue(EditText editText){
        return isEmptyString(editText.getText().toString()) ? "" : editText.getText().toString();
    }


//------------------------------------------------------- Interface -------------------------------------------------------------------------------------------------------------------------------------------------

    public interface DialogBoxOKClick{ void OkClick(DialogInterface okDialogBox);}
    public static void showOKCancelDialogBox(Context context, String title,String  msg, DialogBoxOKClick dialogBoxOKClick){
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, i) -> dialogBoxOKClick.OkClick(dialog))
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .create();
        alertDialog.show();
    }

    public static void showOKDialogBox(Context context, String title,String  msg, DialogBoxOKClick dialogBoxOKClick){
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, i) -> dialogBoxOKClick.OkClick(dialog) )
                .create();
        alertDialog.show();
    }

    public interface COLOR_CODE {
        String ORANGE = "#E78B13";
        String PURPLE = "#800080";
        String BLUE = "#0000FF";
        String RED = "#FF0000";
        String LIGHT_BLUE = "#1976D2";
        String GREEN = "#00FF00";
        String BLACK = "#000000";
        String PINK = "#FF10F0";
        String YELLOW = "#FFEA00";
        String DARK_GREY = "#BDBDBD";
        String GREY = "#EEEEEE";
        String LIGHT_GREY = "#BDBDBD";
        String SEA_DARK = "#26648E";
        String SEA_MID = "#4F8FC0";
        String SEA_LIGHT = "#53D2DC";

        String DEFAULT_COLOR = "#04faee";
    }
}
