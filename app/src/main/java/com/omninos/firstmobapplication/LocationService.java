package com.omninos.firstmobapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Manjinder Singh on 15 , November , 2019
 */
public class LocationService extends Service
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, com.google.android.gms.location.LocationListener {

    private double preLat = 0.0, preLng = 0.0;

    public static final int LOCATION_INTERVAL = 5000;
    public static final int FASTEST_LOCATION_INTERVAL = 5000;
    public static final String ACTION_LOCATION_BROADCAST = LocationService.class.getName() +
            "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    private static final String TAG = LocationService.class.getSimpleName();
    LocationRequest mLocationRequest = new LocationRequest();
    private GoogleApiClient mLocationClient;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onCreate();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "== Error On onConnected() Permission not granted");
            //Permission not granted by user so cancel the further execution.
            return;
        }

        if (mLocationClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
            Log.d(TAG, "Connected to Google API");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
//            Toast.makeText(this, "Destory", Toast.LENGTH_SHORT).show();
            Log.v("location_service", "location != null");
            Log.v("location_service", location.getLatitude() + "," + location.getLongitude());
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            App.getSinglton().setLatLng(latLng);
            locationData(location.getLatitude(), location.getLongitude());
            sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void sendMessageToUI(String lat, String lng) {
        Log.v(TAG, "Sending info...");
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private void locationData(double latitude, double longitude) {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }

        if (CommonUtils.isNetworkConnected(LocationService.this)) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            @SuppressLint("HardwareIds") DatabaseReference addLat = database.getReference("User").child(telephonyManager.getDeviceId()).child("Location").child("Lat");
            @SuppressLint("HardwareIds") DatabaseReference addLng = database.getReference("User").child(telephonyManager.getDeviceId()).child("Location").child("Lng");
            @SuppressLint("HardwareIds") DatabaseReference addPreLat = database.getReference("User").child(telephonyManager.getDeviceId()).child("Location").child("PreLat");
            @SuppressLint("HardwareIds") DatabaseReference addPreLng = database.getReference("User").child(telephonyManager.getDeviceId()).child("Location").child("PreLng");
            addLat.setValue(latitude);
            addLng.setValue(longitude);
            if (preLng == 0.0) {
                addPreLat.setValue(latitude);
                addPreLng.setValue(longitude);
            } else {
                addPreLat.setValue(preLat);
                addPreLng.setValue(preLng);
            }
            preLat = latitude;
            preLng = longitude;
        } else {
            Toast.makeText(this, "Network Issue", Toast.LENGTH_SHORT).show();
        }
    }
}
