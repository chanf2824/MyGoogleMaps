package com.example.chanf.mygooglemaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    public static final int REQUEST_LOCATION = 0;

    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000*15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng temecula = new LatLng(33.4936, -117.1484);
        mMap.addMarker(new MarkerOptions().position(temecula).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(temecula));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            Log.d("MyGoogleMaps", "Failed Permission check 1");
            Log.d("MyGoogleMaps", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyGoogleMaps", "Failed Permission check 2");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
        }
        mMap.setMyLocationEnabled(true);


    }

    public void toggleView(View v){
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL)
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }
    public void trackMe(View v){
        getLocation();
    }

    public void getLocation(){
        try{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled) Log.d("MyMaps", "getLocation: GPS is enabled");

            //get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMaps", "getLocation: Network is enabled");

            if(!isNetworkEnabled && !isGPSenabled) {
                Log.d("MyMaps", "getLocation: No provider is enabled");
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Check Permissions Now
                    Log.d("MyMaps", "getLocation: Failed Permission check 1");
                    Log.d("MyMaps", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION);
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.d("MyMaps", "Get location: Failed Permission check 2");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_LOCATION);
                canGetLocation = true;
                if(isGPSenabled){
                    Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGps);
                    Log.d("MyMaps", "getLocation: Network GPS update request successful");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT);
                    }
                }
                if(isNetworkEnabled){
                    Log.d("MyMaps", "getLocation: Network enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    Log.d("MyMaps", "getLocation: Network GPS update request successful");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                }

            }

        } catch (Exception e) {
            Log.d("MyMaps", "Caught an exception in getLocation");
            e.printStackTrace();
        }
    }

    LocationListener locationListenerGps = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast
            Log.d("Location", "Location changed");
            //drop a marker on the map (create a method called dropAmarker()
            dropAMarker(location);

            //disable network updates (see LocationManager to remove updates)
            locationManager.removeUpdates(locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (ActivityCompat.checkSelfPermission(getParent(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now
                Log.d("MyMaps", "getLocation: Failed Permission check 1");
                Log.d("MyMaps", Integer.toString(ActivityCompat.checkSelfPermission(getParent(), Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
            if (ActivityCompat.checkSelfPermission(getParent(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMaps", "Get location: Failed Permission check 2");
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION);
            }
            //setup a switch statement on status
            //case: LocationProvider.AVAILABLE --> output a message to Log.d and/or Toast
            //case: LocationProvider.OUT_OF_SERVICE --> request updates from NETWORK_PROVIDER
            //case: LocationProvider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER
            //case: default --> request updates from NETWORK_PROVIDER
            switch(status){
                case LocationProvider.AVAILABLE: Log.d("Location", "Status: AVAILABLE");
                    Toast.makeText(getApplicationContext(), "GPS AVAILABLE", Toast.LENGTH_SHORT);
                    break;
                case LocationProvider.OUT_OF_SERVICE: Log.d("Location", "Status: OUT OF SERVICE");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE: Log.d("Location", "Status: TEMPORARILY UNAVAILABLE");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
                default: Log.d("Location", "Status: Default case");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider){}

        @Override
        public void onProviderDisabled(String provider){}
    };

    LocationListener locationListenerNetwork = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast
            Log.d("Location", "Location changed");
            Toast.makeText(getApplicationContext(), "Location changed", Toast.LENGTH_SHORT);
            //drop a marker on the map (create a method called dropAMarker)
            dropAMarker(location);
            //relaunch request for network location updates (requestLocationUpdates(NETWORK_PROVIDER))
            if (ActivityCompat.checkSelfPermission(getParent(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now
                Log.d("MyMaps", "getLocation: Failed Permission check 1");
                Log.d("MyMaps", Integer.toString(ActivityCompat.checkSelfPermission(getParent(), Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
            if (ActivityCompat.checkSelfPermission(getParent(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMaps", "Get location: Failed Permission check 2");
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION);
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            Log.d("Location", "onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    private void dropAMarker(Location loc){
        LatLng place = new LatLng(loc.getLatitude(), loc.getLongitude());
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(place)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

    }
}
