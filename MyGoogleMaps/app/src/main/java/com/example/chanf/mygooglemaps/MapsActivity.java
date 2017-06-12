package com.example.chanf.mygooglemaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private Location myLocation;
    private EditText editSearch;
    private List<Address> locs;

    private static final int REQUEST_LOCATION = 2;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;
    private static final float MY_LOC_ZOOM_FACTOR = 17f;
    private static final double FIVE_MILE_RADIUS = 5/69;


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
       // mMap.setMyLocationEnabled(true);
        //Log.d("MyMaps", "setMyLocationEnabled: TRUE");
       // Toast.makeText(this, "Location enabled", Toast.LENGTH_SHORT).show();


    }

    public void toggleView(View v) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL)
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }

    public void trackMe(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMaps", "TrackMe: Permissions failed");
            return;
        }
        mMap.setMyLocationEnabled(false);
        if (!(isGPSenabled && isNetworkEnabled)) {
            Log.d("MyMaps", "trackMe: called getLocation");
            getLocation();
            Log.d("MyMaps", "trackMe: called getLocation2");
        } else {
            isGPSenabled = isNetworkEnabled = false;
            locationManager.removeUpdates(locationListenerGps);
            Log.d("MyMaps", "trackMe: removed GPS updates");
            locationManager.removeUpdates(locationListenerNetwork);
            Log.d("MyMaps", "trackMe: removed Network");
        }
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

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
            }

            //get GPS status
            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled) Log.d("MyMaps", "getLocation: GPS is enabled");
            else Log.d("MyMaps", "getLocation: GPS is disabled");

            //get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMaps", "getLocation: Network is enabled");
            else Log.d("MyMaps", "getLocation: Network is disabled");

            if (!isNetworkEnabled && !isGPSenabled) {
                Log.d("MyMaps", "getLocation: No provider is enabled");
            } else {
                mMap.setMyLocationEnabled(false);
                Log.d("MyMaps", "getLocation: MyLocationEnabled = false");
                canGetLocation = true;

                if (isGPSenabled) {
                    Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGps);
                    Log.d("MyMaps", "getLocation: Network GPS update request successful");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT).show();
                }
                if (isNetworkEnabled) {
                    Log.d("MyMaps", "getLocation: Network enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    Log.d("MyMaps", "getLocation: Network update request successful");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT).show();
                }

            }

        } catch (Exception e) {
            Log.d("MyMaps", "Caught an exception in getLocation");
            e.printStackTrace();
        }
    }

    //method used when location changes
    private void dropAMarker(String provider) {

        LatLng userLocation;

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMaps", "dropAMarker: Failed Permission check 1");
                return;
            }
            myLocation = new Location(provider);
        }
        if (myLocation == null) {
            Log.d("MyMaps", "dropAMarker: myLocation is null");
            Toast.makeText(getApplicationContext(), "myLocation is invalid", Toast.LENGTH_SHORT).show();
        } else {

            if (provider.equals("GPS")) {
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(5)
                        .strokeColor(Color.GREEN)
                        .strokeWidth(2).fillColor(Color.GREEN));
                Log.d("MyMaps", "dropAMarker: GPS");

            } else {
                myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(5)
                        .strokeColor(Color.RED)
                        .strokeWidth(2).fillColor(Color.RED));
                //markers.add(circle);
                Log.d("MyMaps", "dropAMarker:  NETWORK");
            }

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
            mMap.animateCamera(update);
            Log.d("MyMaps", provider + ": Marker placed; " + userLocation);
        }

    }

    //method for button to remove markers
    public void removeAllMarkers(View v)
    {
        mMap.clear();
        Log.d("MyMaps", "Markers removed");
        Toast.makeText(this, "Markers cleared", Toast.LENGTH_SHORT).show();
    }

    //method for button to initiate search
    public void search(View v){
        //has location manager instantiation
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        editSearch = (EditText)findViewById(R.id.editTextSearch);
        String search = editSearch.getText().toString();
        Log.d("MyMaps", "Search text = " + search);
        List<Address> locs = new ArrayList<Address>();

        //part of debug
        //StringBuffer buffer = new StringBuffer();

        if (search.trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please input a search", Toast.LENGTH_SHORT).show();
        } else {
            Geocoder geocoder = new Geocoder(this);

            //permissions and last known location
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMaps", "search: Failed Permission check 1");
                return;
            }

            Location myLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (myLoc == null) {
                myLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.d("MyMaps", "Loc was null; now tied to network provider");
            }
            Log.d("MyMaps", "myLoc is not null");

            try {
                //get locations
                locs = geocoder.getFromLocationName(search, 50, myLoc.getLatitude() - 0.0324637681, myLoc.getLongitude() - 0.03332387845,
                        myLoc.getLatitude() + 0.0324637681, myLoc.getLongitude() + 0.03332387845);
                //locs = geocoder.getFromLocationName(search, 5);
                Log.d("MyMaps", "search: locs list size = " + locs.size());
            } catch(Exception e) {
                e.printStackTrace();
                Log.d("MyMap", "Exception in search method");
            }
            //geocoder.isPresent();
            if (locs.size() > 0) {
                for (int i = 0; i < locs.size(); i++) {
                    Address add = locs.get(i);
                    //buffer.append(add.toString() + "\n");
                    LatLng pos = new LatLng(add.getLatitude(), add.getLongitude());

                    Log.d("MyMaps", pos.toString());

                    //put marker and circle (circle not apparent enough)
                    mMap.addMarker(new MarkerOptions().position(pos).title(add.getFeatureName())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                    Circle circle = mMap.addCircle(new CircleOptions()
                            .center(pos)
                            .radius(100)
                            .strokeColor(Color.MAGENTA)
                            .strokeWidth(2).fillColor(Color.MAGENTA));
                    Log.d("MyMaps", "added circle");
                }
                //showMessage("Search", buffer.toString());
                Toast.makeText(this, "Markers placed", Toast.LENGTH_SHORT).show();
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(myLoc.getLatitude(), myLoc.getLongitude()), 10);
                mMap.animateCamera(update);
                Log.d("MyMaps", "camera zoomed");
            }
            else
                Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();

        }

    }

  /*  private double getChangeLongitude(double latitude, int miles) {
        double degreesRadians = (Math.PI/180);
        double radiansDegrees = (180/Math.PI);
        double r = 3690*(Math.cos(latitude * (degreesRadians)));
        return ((miles/r)*radiansDegrees);
    } */

    private void showMessage(String title, String message) {
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true); //cancel using back button
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    LocationListener locationListenerGps = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast
            Log.d("MyMaps", "onLocationChanged (GPS): Location changed");
           // Toast.makeText(getApplicationContext(), "GPS: Location changed", Toast.LENGTH_SHORT).show();
            //drop a marker on the map (create a method called dropAmarker()
            dropAMarker("GPS");

            //disable network updates (see LocationManager to remove updates)
            locationManager.removeUpdates(locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now
                Log.d("MyMaps", "getLocation: Failed Permission check 1");
                Log.d("MyMaps", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
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

            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "Status: AVAILABLE");
                    Toast.makeText(getApplicationContext(), "GPS AVAILABLE", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "Status: OUT OF SERVICE");
                    Toast.makeText(getApplicationContext(), "GPS OUT OF SERVICE", Toast.LENGTH_SHORT).show();
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "Status: TEMPORARILY UNAVAILABLE");
                    Toast.makeText(getApplicationContext(), "GPS TEMP UNAVAILABLE", Toast.LENGTH_SHORT).show();
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
                default:
                    Log.d("MyMaps", "Status: Default case");
                    Toast.makeText(getApplicationContext(), "GPS DEFAULT", Toast.LENGTH_SHORT).show();
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now
                Log.d("MyMaps", "getLocation: Failed Permission check 1");
                Log.d("MyMaps", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMaps", "Get location: Failed Permission check 2");
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION);
            }

            //output message in Log.d and Toast
            Log.d("MyMaps", "onLocationChanged (Network): Location changed");
           // Toast.makeText(getApplicationContext(), "Network: Location changed", Toast.LENGTH_SHORT).show();

            //drop a marker on the map (create a method called dropAMarker)
            dropAMarker("NETWORK");

            //relaunch request for network location updates (requestLocationUpdates(NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListenerGps);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMaps", "onStatusChanged (Network)");
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
}
