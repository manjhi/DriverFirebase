package com.omninos.firstmobapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Manjinder Singh on 15 , November , 2019
 */
public class MyService extends Service {

    private double preLat = 0.0, preLng = 0.0;
    private LocationManager locationManager;
    private GoogleApiClient client;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getLocation();
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startService(new Intent(MyService.this, MyService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    locationData(location.getLatitude(), location.getLongitude());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        } else if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 500, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    locationData(location.getLatitude(), location.getLongitude());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

        if (CommonUtils.isNetworkConnected(MyService.this)) {
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
