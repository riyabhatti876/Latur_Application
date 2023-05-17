package com.example.latur_application.GoogleMaps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.latur_application.R;
import com.example.latur_application.Utilities.Utility;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {

    public static final String TAG = "Splash";

//---------------------------------------onCreate--------------------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide Action Bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        new Handler().postDelayed(() -> {
            try
            {
                if(Utility.getBooleanSavedData(this, Utility.IS_USER_SUCCESSFULLY_LOGGED_IN)) {
                    reDirectMap();
                }
                else
                {
                    reDirectPermission();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);

    }

    //---------------------------------------------------------- reDirect -------------------------------------------------------------------------------------------------------------

    private void reDirectMap() {
        Intent intent = new Intent(SplashActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    private void reDirectPermission() {
        Intent intent = new Intent(SplashActivity.this, PermissionActivity.class);
        startActivity(intent);
        finish();
    }
}