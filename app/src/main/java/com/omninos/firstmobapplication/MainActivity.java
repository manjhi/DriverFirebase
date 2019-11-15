package com.omninos.firstmobapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private LocationManager locationManager;
    private GoogleApiClient client;
    private double preLat = 0.0, preLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setMap();
        getLocation();
//
//        if (App.getSinglton().getLatLng() != null) {
//            setMap();
//        } else {
//            Intent intent = new Intent(MainActivity.this, MyService.class);
//            if (!isMyServiceRunning(intent.getClass())) {
//                startService(intent);
//                setMap();
//            }
//        }

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        startLocationService();
//    }
//
//    private void startLocationService() {
//        if (App.getSinglton().getLatLng() == null) {
//            Intent intent = new Intent(MainActivity.this, MyService.class);
//            if (!isMyServiceRunning(intent.getClass())) {
//                startService(intent);
//                setMap();
//            }
//        } else {
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(App.getSinglton().getLatLng().latitude, App.getSinglton().getLatLng().longitude), 15));
//        }
//    }
//
//    private boolean isMyServiceRunning(Class<?> serviceClass) {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        assert manager != null;
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }

    private void setMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapHome);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
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
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
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
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
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

        if (CommonUtils.isNetworkConnected(MainActivity.this)) {
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_data));

            if (!success) {
                Log.e("Data", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("Data", "Can't find style. Error: ", e);
        }

//        if (App.getSinglton().getLatLng() != null) {
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(App.getSinglton().getLatLng().latitude, App.getSinglton().getLatLng().longitude), 15));
//        } else {
//            Intent intent = new Intent(MainActivity.this, MyService.class);
//            if (!isMyServiceRunning(intent.getClass())) {
//                startService(intent);
//            }
//        }
    }
}
