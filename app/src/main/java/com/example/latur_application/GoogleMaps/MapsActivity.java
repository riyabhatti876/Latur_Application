package com.example.latur_application.GoogleMaps;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.android.volley.VolleyError;
import com.example.latur_application.Database.DataBaseHelper;
import com.example.latur_application.R;
import com.example.latur_application.Utilities.SystemPermission;
import com.example.latur_application.Utilities.Utility;
import com.example.latur_application.WMSMaps.WMSProvider;
import com.example.latur_application.WMSMaps.WMSTileProviderFactory;
import com.example.latur_application.volly.BaseApplication;
import com.example.latur_application.volly.URL_Utility;
import com.example.latur_application.volly.WSResponseInterface;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.latur_application.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnPolygonClickListener, GoogleMap.OnMarkerClickListener, View.OnClickListener, WSResponseInterface {


    // TAG
    public static final String TAG = MapsActivity.class.getSimpleName();
    // Map
    GoogleMap mMap;
    // Binding
    ActivityMapsBinding binding;
    // Activity
    Activity mActivity;
    // DataBase
    private DataBaseHelper dataBaseHelper;
    // Base Application
    BaseApplication baseApplication;
    // ProgressDialog
    private ProgressDialog progressDialog;
    // Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest mRequest;
    private Location mCurrentLocation = null;
    private static final float DEFAULT_ZOOM = 20f;
    private boolean isGoToCurrentLocation = false;

    private ArrayList<Marker> geoJsonMarkerList = new ArrayList<>();
    private HashMap<String, Polygon> geoJsonPolygonLists = new HashMap<>();
    boolean isMarkerVisible = false;
    private static final String droneLayer = "http://173.249.24.149:8080/geoserver/shirol/wms?service=WMS&version=1.1.0&request=GetMap&layers=shirol%3AShirol_Base&bbox=74.58111479319136%2C16.72872028532293%2C74.62853852632936%2C16.76589925517693&width=768&height=602&srs=EPSG%3A4326&styles=&format=application/openlayers";

    private String currentLatitude = "";
    private String currentLongitude = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Activity
        mActivity = this;
        // init Database
        initDatabase();


        // base Application
        baseApplication = (BaseApplication) getApplication();
        // FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mActivity);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Location Call Back
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location loc : locationResult.getLocations()) {
                    mCurrentLocation = loc;
                    if (mCurrentLocation != null) {
                        currentLatitude = String.valueOf(mCurrentLocation.getLatitude());
                        currentLongitude = String.valueOf(mCurrentLocation.getLongitude());
                    }

                }
            }
        };

        LocationPermission();

    }
//------------------------------------------------------- InitDatabase --------------------------------------------------------------------------------------------------------------------------

    private void initDatabase() {
        dataBaseHelper = new DataBaseHelper(this);
    }
    //------------------------------------------------- addWMSLayer ------------------------------------------------------------------------------------------------------------------------

    private void addWMSLayer(String wmsLayerURl) {
        TileProvider tileProvider = WMSTileProviderFactory.getWMSTileProvider(WMSProvider.getWMSLayer(wmsLayerURl));
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
    }

//---------------------------------------------- Location Permission ------------------------------------------------------------------------------------------------------------------------

    private void LocationPermission() {
        if (SystemPermission.isLocation(mActivity)) {
            location();
        }
    }


//------------------------------------------------------- onMapReady ---------------------------------------------------------------------------------------------------------------------------

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Google Map
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        // set Map
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // Map Click Listener
        mMap.setOnMapClickListener(this);
        // Polygon Click Listener
        mMap.setOnPolygonClickListener(this);
        // Marker Click Listener
        mMap.setOnMarkerClickListener(this);
        // setOnClickListener
        setOnClickListener();



        //------------------------------------------------------- On Camera Idle ------------------------------------------------------------

        mMap.setOnCameraIdleListener(() -> {
            if (isMarkerVisible) {
                try {
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    service.execute(() -> handler.post(() -> {
                        if (mMap != null) {
                            float mapZoomLevel = mMap.getCameraPosition().zoom;
                            if (mapZoomLevel < 39f) {
                                Log.e(TAG, "Zoom < then required");
                                if (geoJsonMarkerList != null) {
                                    if (geoJsonMarkerList.size() > 0) {
                                        for (Marker m : geoJsonMarkerList) {
                                            m.setVisible(false);
                                        }
                                    }
                                }

                            } else {
                                if (geoJsonMarkerList != null) {
                                    if (geoJsonMarkerList.size() > 0) {
                                        for (Marker m : geoJsonMarkerList) {
                                            m.setVisible(true);
                                        }
                                    }
                                }
                            }
                        }
                    }));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        //------------------------------------------------------- On Camera Move Started ------------------------------------------------------------

        mMap.setOnCameraMoveStartedListener(i -> {
        });


        addWMSLayer(droneLayer);
    }
    //------------------------------------------------------- setOnClickListener ------------------------------------------------------------------------------------------------------------------------------------------------

    private void setOnClickListener() {
        binding.rlCurrentLocation.setOnClickListener(this);
        binding.rlMapType.setOnClickListener(this);
    }

    @SuppressLint("MissingPermission")
    private void location() {
        //now for receiving constant location updates:
        mRequest = LocationRequest.create();
        mRequest.setInterval(2000);//time in ms; every ~2 seconds
        mRequest.setFastestInterval(1000);
        mRequest.setPriority(PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(this, 500);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(mRequest, locationCallback, null);
    }

    protected void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    //---------------------------------------------- onPause ------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        dismissProgressBar();
    }





    private void reDirectToLoginPage() {
        String date = Utility.getSavedData(mActivity, Utility.OLD_DATE);
        Utility.clearData(this);
        Utility.saveData(mActivity, Utility.OLD_DATE, date);
        // Database Clear
        dataBaseHelper.logout();
        dismissProgressBar();
        startActivity(new Intent(this, SplashActivity.class));
    }



//------------------------------------------------------- ProgressBar Show/ Dismiss ------------------------------------------------------------------------------------------------------

    private void dismissProgressBar() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void showProgressBar() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading and Sync Data...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }
    }

    private void showProgressBar(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(msg);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }
    }

    //---------------------------------------------- onResume ------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

//---------------------------------------------- onResume ------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }

    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {

    }

    @Override
    public void onSuccessResponse(URL_Utility.ResponseCode responseCode, String response) {

    }

    @Override
    public void onErrorResponse(URL_Utility.ResponseCode responseCode, VolleyError error) {

    }
}