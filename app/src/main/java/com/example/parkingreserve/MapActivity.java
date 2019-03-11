package com.example.parkingreserve;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements  OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {


    boolean locationIsShared=false;

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button shareBtn;

    ImageView hidingMarker;

    LatLng sharedPosition;

    private LatLng armeniaLatLng = new LatLng(40.177200, 44.503490);



    boolean SearchSharedParkingAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();




    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        shareBtn = findViewById(R.id.share);
        hidingMarker = findViewById(R.id.hiding_marker);



        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User").child("SharedPosition");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(sharedPosition.latitude, sharedPosition.longitude));


                try {
                    mMap.addMarker(new MarkerOptions().position(sharedPosition).title(addressGetter(new LatLng(sharedPosition.latitude,sharedPosition.longitude))).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_parking)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                hidingMarker.setVisibility(View.GONE);

                shareBtn.setText("Waiting for reserve....");

            }

        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


        }
        else {

            checkLocationPermission();
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(armeniaLatLng, 10f));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.5f));




        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                sharedPosition = new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getApplicationContext(),"OnClick",Toast.LENGTH_SHORT).show();
                Log.i("LOG","ONCLICK");

                return true;
            }
        });

    }


    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {

        mLastLocation=location;
        LatLng latLng=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
     //   mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.5f));
        getShareParkingAround();

//        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User").child("SharedLocation");
//        GeoFire geoFire=new GeoFire(ref);

//        geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


        }
        else {

              checkLocationPermission();
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }




    private void getShareParkingAround() {

            //SearchSharedParkingAroundStarted = true;
            DatabaseReference SharedLocation = FirebaseDatabase.getInstance().getReference().child("User").child("SharedPosition");



            GeoFire geoFire = new GeoFire(SharedLocation);
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 999999999);

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {

                    for (Marker markerIt : markers) {
                        if (markerIt.getTag().equals(key))
                            return;
                    }

                    LatLng SharedParking = new LatLng(location.latitude, location.longitude);

                    Marker mParkingMarker = mMap.addMarker(new MarkerOptions().position(SharedParking).title(key).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_parking)));
                    mParkingMarker.setTag(key);

                    markers.add(mParkingMarker);


                }

                @Override
                public void onKeyExited(String key) {
                    for (Marker markerIt : markers) {
                        if (markerIt.getTag().equals(key))
                            markerIt.remove();
                    }

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }

            });



//    @Override
//    protected void onStop() {
//
//        super.onStop();
//        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User").child("SharedLocation");
//        GeoFire geoFire=new GeoFire(ref);
//        geoFire.removeLocation(userId);
//
//    }


    }



    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(MapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    com.google.android.gms.location.LocationCallback mLocationCallback = new LocationCallback(){


        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                    if(!SearchSharedParkingAroundStarted)
                        getShareParkingAround();
                }
            }
        }
    };



    private String addressGetter(LatLng currentPosition) throws IOException {
        String address="";
        Geocoder geocoder =new Geocoder(MapActivity.this, Locale.getDefault());
        List<Address> addressList =geocoder.getFromLocation(currentPosition.latitude,currentPosition.longitude,1);
        address=addressList.get(0).getAddressLine(0);

        return address;

    }


//    public void locationAsDestination (Location location){
//
//    }
//
//    public  void locationAsDriving(Location location){
//        mLastLocation=location;
//        LatLng latLng=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
//        //   mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//      //  mMap.animateCamera(CameraUpdateFactory.zoomTo(17.5f));
//        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User").child("IncomingCarLocation");
//        GeoFire geoFire=new GeoFire(ref);
//
//        geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
//
//        getShareParkingAround();
//
//    }



}

