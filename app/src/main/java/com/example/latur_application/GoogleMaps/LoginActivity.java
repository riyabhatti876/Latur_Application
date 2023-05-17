package com.example.latur_application.GoogleMaps;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.latur_application.Database.DataBaseHelper;
import com.example.latur_application.InternetConnection.ConnectivityReceiver;
import com.example.latur_application.R;
import com.example.latur_application.Utilities.SystemPermission;
import com.example.latur_application.Utilities.Utility;
import com.example.latur_application.databinding.ActivityLoginBinding;
import com.example.latur_application.volly.BaseApplication;
import com.example.latur_application.volly.URL_Utility;
import com.example.latur_application.volly.WSResponseInterface;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, WSResponseInterface {

    // TAG
    public static final String TAG = LoginActivity.class.getSimpleName();
    // Binding
    ActivityLoginBinding binding;
    // Activity
    Activity mActivity;
    // Boolean
    boolean isNewPasswordVisible = false;
    boolean isConfirmPasswordVisible = false;
    boolean isPasswordVisible = false;
    // ProgressBar
    private ProgressDialog progressDialog;

    // Broadcast Receiver
    BroadcastReceiver broadcastReceiver;

    // Database

    private DataBaseHelper dataBaseHelper;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Hide Action Bar
        if(getSupportActionBar() != null)   getSupportActionBar().hide();

        // Activity
        mActivity = this;
        // initDatabase
        initDatabase();
        // setOnClickListener
        setOnClickListener();

        // Password Edit Text Touch
        binding.edtPassword.setOnTouchListener((view, event) -> {
            final int Right = 2;
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= binding.edtPassword.getRight() - binding.edtPassword.getCompoundDrawables()[Right].getBounds().width()) {
                    int selection = binding.edtPassword.getSelectionEnd();
                    if (isPasswordVisible) {
                        binding.edtPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_password_not_visible, 0);
                        binding.edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        isPasswordVisible = false;
                    } else {
                        binding.edtPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_password_visible, 0);
                        binding.edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        isPasswordVisible = true;
                    }
                    binding.edtPassword.setSelection(selection);
                    return true;
                }
            }
            return false;
        });

        // network check broadcastReceiver!
        broadcastReceiver = new ConnectivityReceiver() {
            @Override
            protected void onNetworkChange(String alert) {
                Snackbar snackbar = Snackbar.make(binding.loginPageLayout, alert, Snackbar.LENGTH_SHORT);
                snackbar.setTextColor(Color.WHITE);
                snackbar.setBackgroundTint(Color.parseColor(Utility.COLOR_CODE.SEA_MID));
                snackbar.show();
            }
        };
        registerNetworkBroadcast();

    }

    //------------------------------------------------------- initDatabase ----------------------------------------------------------------------------------------------------------------------

    private void initDatabase() {
        dataBaseHelper = new DataBaseHelper(mActivity);
    }

//------------------------------------------------------- setOnClickListener ----------------------------------------------------------------------------------------------------------------

    @SuppressLint("SetTextI18n")
    private void setOnClickListener(){
        binding.btnLogin.setOnClickListener(this);
        binding.tvRegistration.setOnClickListener(this);
        binding.tvForgotPassword.setOnClickListener(this);
        binding.txtAppVersion.setText("App version: " + URL_Utility.APP_VERSION);
    }

    //------------------------------------------------------- onClick ----------------------------------------------------------------------------------------------------------------

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.btnLogin:
                if(SystemPermission.isInternetConnected(mActivity)){
                    processToLogin();

                }
                else{
                    showOKDialogBox(this, "Alert", "Need Internet Connection to Login.", DialogInterface::dismiss);
                }
                break;

            case R.id.tvForgotPassword:
                reDirectToForgotPassword();
                break;

        }

    }
    public static void showOKDialogBox(Context context, String title, String  msg, Utility.DialogBoxOKClick dialogBoxOKClick){
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, i) -> dialogBoxOKClick.OkClick(dialog) )
                .create();
        alertDialog.show();
    }


//------------------------------------------------------- process To Login ----------------------------------------------------------------------------------------------------------------

    private void processToLogin() {
        if (Utility.isEmptyString(binding.edtUsername.getText().toString().trim())) {
            Utility.showToast(mActivity, getResources().getString(R.string.Please_enter_username));
            return;
        }
        if (Utility.isEmptyString(binding.edtPassword.getText().toString().trim())) {
            Utility.showToast(mActivity, getResources().getString(R.string.Please_enter_password));
            return;
        }
        if(!Utility.isEmptyString(binding.edtUsername.getText().toString().trim()) && Patterns.EMAIL_ADDRESS.matcher(binding.edtUsername.getText().toString()).matches()){
            showProgressBar("Logging In...");
            processToLoginAuthentication();
        }
        else{
            Toast.makeText(mActivity, "Please Enter valid Email ID", Toast.LENGTH_SHORT).show();
        }
    }

//------------------------------------------------------- process To Login Authentication ----------------------------------------------------------------------------------------------------------------

    private void processToLoginAuthentication() {
        Map<String, String> params = new HashMap<>();
        String data = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(URL_Utility.PARAM_EMAIL_ID, binding.edtUsername.getText().toString().trim());
            jsonObject.put(URL_Utility.PARAM_PASSWORD, binding.edtPassword.getText().toString().trim());
            jsonObject.put(URL_Utility.APP_VERSION, URL_Utility.APP_VERSION);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            data =  jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.put("data",data);
        Log.e(TAG, "Form ->" + data);

        URL_Utility.ResponseCode responseCode = URL_Utility.ResponseCode.WS_LOGIN;
        BaseApplication.getInstance().makeHttpPostRequest(mActivity, responseCode, URL_Utility.WS_LOGIN, params, false, false);
    }


//---------------------------------------------------------- Forgot Password ------------------------------------------------------------

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void reDirectToForgotPassword(){
        try{
            // Dialog Box
            Dialog dialog = new Dialog(mActivity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.change_password_view);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // Edit Text
            TextView title = dialog.findViewById(R.id.headLineTitle);
            title.setText("Forget Password");
            LinearLayout llOldPassword = dialog.findViewById(R.id.oldPasswordLayout);
            llOldPassword.setVisibility(View.GONE);
            LinearLayout llEmailID   = dialog.findViewById(R.id.emailIdLayout);
            llEmailID.setVisibility(View.VISIBLE);
            EditText emailId         = dialog.findViewById(R.id.emailID);
            EditText newPassword     = dialog.findViewById(R.id.newPassword);
            EditText confirmPassword = dialog.findViewById(R.id.confirmPassword);
            // Submit Button
            Button submitButton = dialog.findViewById(R.id.profileChangeSubmitButton);
            // Close Button
            Button closeButton = dialog.findViewById(R.id.profileChangeCloseButton);
            closeButton.setOnClickListener(view -> dialog.dismiss());
            // Submit Button
            submitButton.setOnClickListener(view -> {

                if(!Utility.isEmptyString(emailId.getText().toString()) && !Utility.isEmptyString(newPassword.getText().toString()) && !Utility.isEmptyString(confirmPassword.getText().toString())) {

                    if(!newPassword.getText().toString().equals(confirmPassword.getText().toString())){
                        Toast.makeText(mActivity, "password wrong", Toast.LENGTH_SHORT).show();
                        newPassword.setError("password not match");
                        confirmPassword.setError("password not match");
                    }
                    else{
                        if (SystemPermission.isInternetConnected(mActivity)){
                            forgotPasswordSubmit(emailId,newPassword);
                        } else {
                            dismissProgressBar();
                            Utility.showOKCancelDialogBox(mActivity, "Connection Error", "Need Internet Connection to Change Password", DialogInterface::dismiss);
                        }
                    }
                }
                else{
                    Toast.makeText(mActivity, "Enter Field", Toast.LENGTH_SHORT).show();
                }
            });



            newPassword.setOnTouchListener((view, event) -> {
                final int Right = 2;
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(event.getRawX() >= newPassword.getRight() - newPassword.getCompoundDrawables()[Right].getBounds().width()){
                        int selection = newPassword.getSelectionEnd();
                        if(isNewPasswordVisible){
                            newPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_password_not_visible,0);
                            newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            isNewPasswordVisible = false;
                        }
                        else{
                            newPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_password_visible,0);
                            newPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            isNewPasswordVisible = true;
                        }
                        newPassword.setSelection(selection);
                        return true;
                    }
                }
                return false;
            });


            confirmPassword.setOnTouchListener((view, event) -> {
                final int Right = 2;
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(event.getRawX() >= confirmPassword.getRight() - confirmPassword.getCompoundDrawables()[Right].getBounds().width()){
                        int selection = confirmPassword.getSelectionEnd();
                        if(isConfirmPasswordVisible){
                            confirmPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_password_not_visible,0);
                            confirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            isConfirmPasswordVisible = false;
                        }
                        else{
                            confirmPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_password_visible,0);
                            confirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            isConfirmPasswordVisible = true;
                        }
                        confirmPassword.setSelection(selection);
                        return true;
                    }
                }
                return false;
            });


            // Dialog Box Show
            dialog.show();
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }

    private void forgotPasswordSubmit(EditText edtEmailID, EditText edtNewPassword){
        showProgressBar("Forget Password...");
        Map<String, String> params1 = new HashMap<>();
        String data = "";
        JSONObject params = new JSONObject();
        try {
            params.put(URL_Utility.PARAM_NEW_PASSWORD, edtNewPassword.getText().toString());
            params.put(URL_Utility.PARAM_EMAIL_ID    , edtEmailID.getText().toString());
        }
        catch (JSONException e){
            dismissProgressBar();
            e.printStackTrace();
        }
        Log.e(TAG, params.toString());
        try {
            data =  params.toString();
        } catch (Exception e) {
            dismissProgressBar();
            e.printStackTrace();
        }
        params1.put("data",data);
        if(SystemPermission.isInternetConnected(mActivity)){
            URL_Utility.ResponseCode responseCode = URL_Utility.ResponseCode.WS_FORGOT_PASSWORD;
            BaseApplication.getInstance().makeHttpPostRequest(mActivity, responseCode, URL_Utility.WS_FORGOT_PASSWORD, params1, false, false);
        }
        else {
            dismissProgressBar();
            Utility.showOKCancelDialogBox(mActivity, "Connection Error", "Need Internet Connection to Change Password", DialogInterface::dismiss);
        }
    }


//------------------------------------------------------- Progress Bar ----------------------------------------------------------------------------------------------------------------

    private void dismissProgressBar() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void showProgressBar(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(message);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }
        else {
            progressDialog.setMessage(message);
        }
    }

//-----------------------------------------------------------------------------------Network Register-----------------------------------------------------------------------------------------------

    // Register Network
    protected void registerNetworkBroadcast(){
        registerReceiver(broadcastReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    // UnRegister Network
    protected void unregisterNetwork(){
        try{
            unregisterReceiver(broadcastReceiver);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------------- onDestroy -----------------------------------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetwork();
    }
//----------------------------------------------------------------------------------- reDirect -----------------------------------------------------------------------------------------------


    private void reDirectToMap() {
        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    }


//------------------------------------------------------- onSuccessResponse ----------------------------------------------------------------------------------------------------------------

    @Override
    public void onSuccessResponse(URL_Utility.ResponseCode responseCode, String response) {

        // Login
        if (responseCode == URL_Utility.ResponseCode.WS_LOGIN) {
            // Decrypt Response
            Log.e(TAG, "Login Response: " + response);
//                         Progress Bar
                        dismissProgressBar();
                        // ReDirect To Map
                        reDirectToMap();
                    }
                    else {
                        dismissProgressBar();
                        Utility.showToast(mActivity, "User Not Found");
                    }

        // Forgot Password -------------------------------------------
        if(responseCode == URL_Utility.ResponseCode.WS_FORGOT_PASSWORD){
            Log.e(TAG,"Forgot Response: "+response);
            if(!response.equals("")){
                try {
                    JSONObject mBoundaryObj = new JSONObject(response);
                    String msg = mBoundaryObj.optString("status");
                    Log.e(TAG, "Forgot Response Status: "+msg);
                    if (msg.equalsIgnoreCase("Success")) {
                        dismissProgressBar();
                        Utility.showOKDialogBox(mActivity, "Password", "Password Change Successfully", DialogInterface::dismiss);
                    }
                    else {
                        dismissProgressBar();
                        Utility.showOKDialogBox(mActivity, "Password", "Password Change not Successfully", DialogInterface::dismiss);
                    }
                }
                catch (JSONException e) {
                    dismissProgressBar();
                    Log.e(TAG,e.getMessage());
                    Utility.showToast(mActivity,Utility.ERROR_MESSAGE);
                }
            }
            else{
                Log.e(TAG,"Forgot Response Empty");
                Utility.showToast(mActivity,Utility.ERROR_MESSAGE);
                dismissProgressBar();
            }
        }

    }

//------------------------------------------------------- onErrorResponse ----------------------------------------------------------------------------------------------------------------

    @Override
    public void onErrorResponse(URL_Utility.ResponseCode responseCode, VolleyError error) {
        dismissProgressBar();
        Utility.showToast(mActivity,Utility.ERROR_MESSAGE);
        Log.e(TAG, "Error ResponseCode: " + responseCode);
        Log.e(TAG,"Error: "+error.getMessage());
    }
}