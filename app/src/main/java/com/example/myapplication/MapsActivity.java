package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;

import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.MapsActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;


import com.example.myapplication.databinding.ActivityMapsBinding;

import org.w3c.dom.Text;

import java.security.cert.PKIXCertPathBuilderResult;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap map;
    private ActivityMapsBinding binding;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location lastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float currentAzimuth = 0f;
    private SensorManager mSensorManager;

    private double lastLat;
    private double lastLong;
    private LatLng lastLocation = new LatLng(lastLat, lastLong);

    private EditText edit;
    private Button mockButton;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText longitude, latitude, name;
    private Button save, cancel;
    private ArrayList<Marker> markerList = new ArrayList<Marker>();
    private ArrayList<View> markerView = new ArrayList<View>();
    private LocationItemDao dao;
    private LocationDatabase db;
    private ConstraintLayout constraintLayout;
    private float bearingAngle;
    private int total_marker_existed = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        Places.initialize(getApplicationContext(), getString(R.string.maps_api_key));
        placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // UI Mock Elements
        edit = findViewById(R.id.mock_degrees);
        mockButton = findViewById(R.id.mock_button);
        constraintLayout = findViewById(R.id.constraint);
        Button addLocation = (Button) findViewById(R.id.addLocation);
        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewLocationDialog();
            }
        });

        db = LocationDatabase.getSingleton(this);
        dao = db.locationItemDao();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        /** Add a marker in Sydney and move the camera **/

        // curr pos (32.87, -117.22)
        List<LocationItem> locItems = dao.getAll();
        for(LocationItem l: locItems){
            LatLng newLatLng = new LatLng(l.longitude, l.latitude);
            Marker inputLocationMarker = map.addMarker(new MarkerOptions()
                    .position(newLatLng)
                    .title(l.label));
            inputLocationMarker.showInfoWindow();
            markerList.add(inputLocationMarker);
            addNewView(l.label);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy(){
        db.makeDatabase(getApplicationContext());
        super.onDestroy();
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            updateLocationUI();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                lastLat = lastKnownLocation.getLatitude();
                                lastLong = lastKnownLocation.getLongitude();
                                UpdateCameraLocation.updateCameraLocation(lastLat, lastLong, azimuth, map, DEFAULT_ZOOM);
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = 0.97f;
        synchronized (this) {
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];
            }

            if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * sensorEvent.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * sensorEvent.values[2];
            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if(success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                azimuth = setOrientation.setOrientation(orientation);
                getDeviceLocation();
            }
        }
        ImageView imageView = findViewById(R.id.compass);
        imageView.setRotation((float) -azimuth);
        UpdateIcon.updateIconOrientation(markerList, markerView, azimuth, lastLat, lastLong, DEFAULT_ZOOM);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    // below are two mock methods for UI testing
    public void onClickMock(View v) {
        String degree = edit.getText().toString();
        float deg = Float.valueOf(degree);
        mSensorManager.unregisterListener(this);

        if (lastKnownLocation != null) {
            lastLat = lastKnownLocation.getLatitude();
            lastLong = lastKnownLocation.getLongitude();
            UpdateCameraLocation.updateCameraLocation(lastLat, lastLong, azimuth, map, DEFAULT_ZOOM);
        }

        ImageView imageView = findViewById(R.id.compass);
        imageView.setRotation(-deg);
        UpdateIcon.updateIconOrientation(markerList, markerView, deg, lastLat, lastLong, DEFAULT_ZOOM);

    }

    public void onClickCenter(View v) {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    public void onClickDBDelete(View v) {
        dao.nukeTable();
        Toast.makeText(this, "Deleted all Saved Locations", Toast.LENGTH_LONG).show();
    }

    public void createNewLocationDialog(){
        dialogBuilder = new AlertDialog.Builder(this);
        final View locationPopupView = getLayoutInflater().inflate(R.layout.popup, null);
        latitude = (EditText) locationPopupView.findViewById(R.id.latitude);
        longitude = (EditText) locationPopupView.findViewById(R.id.longitude);
        name = (EditText) locationPopupView.findViewById(R.id.name);

        save = (Button) locationPopupView.findViewById(R.id.save);
        cancel = (Button) locationPopupView.findViewById(R.id.cancel);

        dialogBuilder.setView(locationPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_latitude = latitude.getText().toString();
                String text_longitude = longitude.getText().toString();
                String text_name = name.getText().toString();
                try {
                    double input_latitude = Double.parseDouble(text_latitude);
                    double input_longitude = Double.parseDouble(text_longitude);
                    dialog.dismiss();
                    //correct long/lat values
                    if(inputValidation(input_latitude, input_longitude)) {
                        Toast.makeText(locationPopupView.getContext(), "Saved!", Toast.LENGTH_LONG).show();
                        Marker inputLocationMarker = map.addMarker(new MarkerOptions()
                                .position(new LatLng(input_latitude, input_longitude))
                                .title(text_name));
                        inputLocationMarker.showInfoWindow();
                        markerList.add(inputLocationMarker);
                        addNewView(text_name);
                        //insert new markers to database
                        LocationItem newLoc = new LocationItem(input_latitude, input_longitude, text_name);
                        dao.insert(newLoc);
                    } else {
                        Toast.makeText(locationPopupView.getContext(), "Please input a correct latitude and longitude", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(locationPopupView.getContext(), "Please input a number", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public boolean inputValidation(double input_latitude, double input_longitude) {
        if(-90 <= input_latitude && input_latitude <= 90 && -180 <= input_longitude && input_longitude <= 180) {
            return true;
        } else {
            return  false;
        }
    }

    public void addNewView(String name) {

        ConstraintLayout.LayoutParams newLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        newLayoutParams.circleConstraint = R.id.compass;
        newLayoutParams.circleRadius = DpSpPxConversion.calculatePixels(150, this);
        newLayoutParams.circleAngle = bearingAngle-azimuth;

        View newLayout = getLayoutInflater().inflate(R.layout.label_with_icon, null);
        newLayout.setLayoutParams(newLayoutParams);
        TextView editText = (TextView) newLayout.findViewById(R.id.myImageViewText);
        //ImageView editImage = (ImageView) newLayout.findViewById(R.id.myImageView); Use when trying to change icon for different labels

        editText.setText(name);

        constraintLayout.addView(newLayout);
        markerView.add(newLayout);
    }

}