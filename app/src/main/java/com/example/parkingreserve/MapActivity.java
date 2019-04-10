package com.example.parkingreserve;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements  OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, MarkerDetailsDialog.MarkerDialogListener , RoutingListener, NumberPicker.OnValueChangeListener {

    private User mCurrentUser=new User();
    private User mSharedUser;
    final static String mId = FirebaseAuth.getInstance().getCurrentUser().getUid();


    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button shareBtn;
    ImageView hidingMarker;
    boolean SearchSharedParkingAroundStarted=false;
    LatLng sharedPosition;
    private LatLng armeniaLatLng = new LatLng(40.177200, 44.503490);
   // boolean SearchSharedParkingAroundStarted = false;
   // List<Marker> markers = new ArrayList<Marker>();
    boolean isClient = false;
    private Marker mCurrentMarker;
    private int sharedMinutes;
   private LatLng sharedLocation;
    Marker  markerShareClicked;


    private DrawerLayout drawerLayout;
    LinearLayout twoButtonLayout;
    Button nonSharedcancel,shareConfirm;
    TextView profilePhone,profileModelColor,profilePlate;
    LinearLayout infoPanel;
    TextView infoPanelText,GPS_StatusText;

    LocationManager locationManager ;
    boolean GpsStatus ;
    Context context;






    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        createNotificationChannel();
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        context=getApplicationContext();


        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);

        profilePhone=headerView.findViewById(R.id.profile_phone);
        profileModelColor=headerView.findViewById(R.id.profile_model_color);
        profilePlate=headerView.findViewById(R.id.profile_plate);



        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        int id = menuItem.getItemId();

                        if (id == R.id.nav_camera) {
                             Toast.makeText(getApplicationContext(),String.valueOf(1),Toast.LENGTH_SHORT).show();
                        } else if (id == R.id.nav_gallery) {

                        } else if (id == R.id.nav_slideshow) {
                            Toast.makeText(getApplicationContext(),String.valueOf(3),Toast.LENGTH_SHORT).show();
                        } else if (id == R.id.nav_manage) {


                        }

                        // close drawer when item is tapped
                        drawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

        shareBtn = findViewById(R.id.share);
        hidingMarker = findViewById(R.id.hiding_marker);

        twoButtonLayout=findViewById(R.id.shareAndCancelLayout);
        nonSharedcancel=findViewById(R.id.cancelNotFinnaly);
        shareConfirm=findViewById(R.id.shareFinally);

        infoPanel=findViewById(R.id.infoLayout);
        infoPanelText=findViewById(R.id.infoPanelText);
        GPS_StatusText=findViewById(R.id.infoGPS_Text);





        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(Utils.FIREBASE_ROOT).child(mId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                   mCurrentUser = childSnapshot.getValue(User.class);
                   profilePlate.setText(mCurrentUser.getPlate());
                   profilePhone.setText(mCurrentUser.getPhone());
                   profileModelColor.setText(mCurrentUser.getColor().concat(" ").concat(mCurrentUser.getModel()));




                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });







    shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


       if (shareBtn.getText().equals("start sharing parking")){
                    showNumberPicker();


                }
//    else if (shareBtn.getText().equals("Share")) {
//
//
//       }


                else if (shareBtn.getText().equals("Cancel Sharing")){


                    AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                    builder.setTitle("are you sure");
                    builder.setMessage("you want to cancel?");
                    builder.setCancelable(true);


                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            shareBtn.getBackground().clearColorFilter();
                            markerShareClicked.remove();
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(15.5f));
                    Intent intentStopTimer=new Intent(MapActivity.this,NotificationService.class);
                    stopService(intentStopTimer);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference ref = database.getReference(Utils.FIREBASE_ROOT).child(Utils.SHARED_LOCATION).child(mId);
                                   ref.setValue(null);
                            infoPanelText.setText(null);
                            recreate();




                            shareBtn.setText("start sharing parking");


                        }
                    });

                    builder.setNegativeButton("BACK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                        }
                    });
                              AlertDialog alertDialog=builder.create();
                              alertDialog.show();
                }
            }


        });



        checkGpsStatus();

    }


    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                    if (picker.getValue()<=0){
                        Toast.makeText(this,
                                "please select minutes", Toast.LENGTH_SHORT).show();

                    }
                    else {
                        sharedMinutes=picker.getValue();
                       // Toast.makeText(this,
                             //   "selected minutes " + sharedMinutes, Toast.LENGTH_SHORT).show();
                        infoPanelText.setText(Utils.INFO_MINUTES.concat(" ").concat(String.valueOf(sharedMinutes)));


                        if (mLastLocation!=null) {
                            hidingMarker.setVisibility(View.VISIBLE);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(18f));

                            shareBtn.setVisibility(View.GONE);
                            twoButtonLayout.setVisibility(View.VISIBLE);

                            nonSharedcancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    twoButtonLayout.setVisibility(View.GONE);
                                    shareBtn.setVisibility(View.VISIBLE);
                                    hidingMarker.setVisibility(View.INVISIBLE);
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15.5f));
                                    onLocationChanged(mLastLocation);
                                }
                            });

                            shareConfirm.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sharedLocation = new LatLng(sharedPosition.latitude, sharedPosition.longitude);
                                    Location sharedCheck = new Location(LocationManager.GPS_PROVIDER);
                                    sharedCheck.setLatitude(sharedLocation.latitude);
                                    sharedCheck.setLongitude(sharedPosition.longitude);

                                    if (mLastLocation.distanceTo(sharedCheck) < 2000) {

                                        //  sharedLocation = new LatLng(sharedPosition.latitude, sharedPosition.longitude);

                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Utils.FIREBASE_ROOT).child(Utils.SHARED_LOCATION);
                                        GeoFire geoFire = new GeoFire(ref);
                                        geoFire.setLocation(mId, new GeoLocation(sharedPosition.latitude, sharedPosition.longitude));


                                        markerShareClicked = mMap.addMarker(new MarkerOptions().position(sharedPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.shareclick2)));
                                        markerShareClicked.setTitle("Waiting");
                                        markerShareClicked.setTag(mId);


                                        hidingMarker.setVisibility(View.GONE);
                                        twoButtonLayout.setVisibility(View.GONE);
                                        shareBtn.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                                        // shareBtn.setBackgroundColor(getResources().getColor(R.color.Red));
                                        shareBtn.setVisibility(View.VISIBLE);
                                        shareBtn.setText("Cancel Sharing");

                                        infoPanelText.setText(Utils.WAITING);

                                        timerServiceStarter();


                                        final Handler handler = new Handler();
                                        Runnable task = new Runnable() {
                                            @Override
                                            public void run() {
                                                try {

                                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                    DatabaseReference IncomerLocationRef = database.getReference().child(Utils.FIREBASE_ROOT).child(Utils.SHARED_LOCATION).child(mId).child(Utils.INCOMER_LOCATION);

                                                    IncomerLocationRef.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            String incomerId = dataSnapshot.getValue(String.class);
                                                            getID(incomerId);
                                                            if (incomerId!=null){

                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        }
                                                    });
                                                    getIncomerLocation(database);
                                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(12.5f));
                                                } catch (Exception e) {
                                                    handler.postDelayed(this, 1000);
                                                }
                                            }
                                        };
                                        handler.post(task);


                                    } else
                                        Toast.makeText(getApplicationContext(), "canceled:sharing place is too far from you", Toast.LENGTH_LONG).show();

                                }
                            });

                            //shareBtn.setText("Share");
                        }
                        else Toast.makeText(MapActivity.this,"please enable GPS",Toast.LENGTH_LONG).show();


                    }



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap = googleMap;

        mFusedLocationClient = new FusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();

        } else {


            mMap.setMyLocationEnabled(true);
            buildGoogleApiClient();
        }

        checkLocationPermission();

        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(armeniaLatLng, 10f));
      //  mMap.animateCamera(CameraUpdateFactory.zoomTo(17.5f));


        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                sharedPosition = new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);

            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

//                Log.i("LOG","ON MARKER  "+" m "+ marker.getTag().toString());
//                Log.i("LOG","ON MARKER  "+"m " +   marker.getPosition().toString());

                //   Toast.makeText(getApplicationContext(),marker.getTag().toString()+""+marker.getPosition().toString(),Toast.LENGTH_LONG).show();

                if (!marker.getTag().equals(mId)){

                    mCurrentMarker = marker;
                    try {
                        String currentMarkerPosition=addressGetter(mCurrentMarker.getPosition());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String sharedId = mCurrentMarker.getTag().toString();

                    double marker_lat=mCurrentMarker.getPosition().latitude;
                    double marker_lon=mCurrentMarker.getPosition().longitude;
                    Location markerLocation=new Location(LocationManager.GPS_PROVIDER);
                    markerLocation.setLatitude(marker_lat);
                    markerLocation.setLongitude(marker_lon);
                    infoPanelText.setText("Distance : " + new DecimalFormat("##.##").format(mLastLocation.distanceTo(markerLocation)) + "m");
                    Handler handler=new Handler();
                    Runnable runnable=new Runnable() {
                        @Override
                        public void run() {
                            infoPanelText.setText(null);
                        }
                    };
                     handler.postDelayed(runnable,3000);

//                Log.i("LOG","ON MARKER  "+sharedId);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference(Utils.FIREBASE_ROOT).child(sharedId);
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            User userIn = childSnapshot.getValue(User.class);
                            mSharedUser = userIn;
                            MarkerDetailsDialog clickedMarker = MarkerDetailsDialog.newInstance(mSharedUser);
                            clickedMarker.show(getSupportFragmentManager(), null);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }


                });


            }







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


        mLastLocation = location;
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.5f));

        Log.i("LOG","LOCATIOOOON");


           if (!isClient) {

                getShareParkingAround();


        } else if (isClient) {

            enableLocationUpdates(mLastLocation);


        }



           final Handler handler=new Handler();
           Runnable gpsChecker=new Runnable() {
               @Override
               public void run() {
                   checkGpsStatus();
                   handler.postDelayed(this,1000);
               }
           };
           handler.postDelayed(gpsChecker,1000);



    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (isClient) {

            enableRequestInterval(mLocationRequest);
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //  LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            checkLocationPermission();
        } else {


            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }



    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    public Bitmap layoutToBitmap(String time){


        LinearLayout tv = (LinearLayout) this.getLayoutInflater().inflate(R.layout.marker_parking_icon, null, false);
        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        ImageView image = (ImageView) findViewById(R.id.markerIMG);

        TextView timer=tv.findViewById(R.id.markerTitle);
             timer.setText(time);


        tv.setDrawingCacheEnabled(true);
        tv.buildDrawingCache();
        Bitmap bm = tv.getDrawingCache();


        return bm;
    }


    ArrayList<Marker> markers = new ArrayList<>();

    private void getShareParkingAround() {



            //SearchSharedParkingAroundStarted = true;
            final DatabaseReference SharedLocation = FirebaseDatabase.getInstance().getReference().child(Utils.FIREBASE_ROOT).child(Utils.SHARED_LOCATION);


            GeoFire geoFire = new GeoFire(SharedLocation);
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 999999999);

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(final String key, GeoLocation location) {

                    if (!key.equals(mId)){

                    final TitleMarker title = new TitleMarker();

                    title.setKeyzapas(key);



                    final LatLng SharedParking = new LatLng(location.latitude, location.longitude);



                    final Marker mParkingMarker = mMap.addMarker(new MarkerOptions().position(SharedParking));



                    markers.add(mParkingMarker);

                    DatabaseReference sharedMinRef = FirebaseDatabase.getInstance().getReference().child(Utils.FIREBASE_ROOT).child(Utils.SHARED_LOCATION).child(key).child("sharedminutes");
                    sharedMinRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                            String timer = String.valueOf(dataSnapshot.getValue(Long.class));
                            title.setTitle(timer);
                            mParkingMarker.setTag(key);

                            try {
                                mParkingMarker.setIcon(BitmapDescriptorFactory.fromBitmap(layoutToBitmap(title.getTitle())));
                            }catch (IllegalArgumentException e){
                                   Log.i("LOG",e.getMessage());
                            }



                            markers.add(mParkingMarker);



                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }

                }

                @Override
                public void onKeyExited(String key) {
                    Log.i("Log", "EXITED KEY" + String.valueOf(key));
                    Log.i("Log", "EXITED SIZE   " + String.valueOf(markers.size()));

                    for (Marker markerIt : markers) {
                        Log.i("Log", "EXITED ID" + String.valueOf(markerIt.getId()));
                        Log.i("Log", "EXITED TAG" + String.valueOf(markerIt.getTag()));
                        if (key.equals(markerIt.getTag()))

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
        }


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





    private void removeNonUsedMarkers(Marker marker) {


        for (int i = 0; i < markers.size(); i++) {

            if (!markers.get(i).equals(marker)) {
                markers.get(i).remove();
            }

        }


    }








    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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


            } else {

                ActivityCompat.requestPermissions(MapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                        buildGoogleApiClient();
                        //   LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();

                }
                break;
            }
        }
    }

    com.google.android.gms.location.LocationCallback mLocationCallback = new LocationCallback() {


        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                   if (!SearchSharedParkingAroundStarted)
                       getShareParkingAround();
                }
            }
        }
    };



    private void enableLocationUpdates(Location location) {


       // String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Utils.FIREBASE_ROOT).child(Utils.INCOMER_LOCATION);
        GeoFire geoFire = new GeoFire(ref);

        geoFire.setLocation(mId, new GeoLocation(location.getLatitude(), location.getLongitude()));

        //  onLocationChanged(mLastLocation);


    }

    private void enableRequestInterval(LocationRequest locationRequest) {

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);

    }

    @Override
    public void OnReserveClicked() {


       if (mCurrentMarker.getTag()!=null) {

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference(Utils.FIREBASE_ROOT).child(Utils.SHARED_LOCATION).child(mCurrentMarker.getTag().toString());
            ref.child(Utils.INCOMER_LOCATION).setValue(mCurrentUser.getUserId());
            Toast.makeText(getApplicationContext(), "RESERVED", Toast.LENGTH_SHORT).show();
            isClient = true;
            onLocationChanged(mLastLocation);
            onConnected(null);
            removeNonUsedMarkers(mCurrentMarker);
            getRouteToMarker(mCurrentMarker.getPosition());
        }
        else Toast.makeText(MapActivity.this,"unavailable user. shared with other or canceled sharing",Toast.LENGTH_LONG).show();

    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        if (pickupLatLng != null && mLastLocation != null) {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener((RoutingListener) this)
                    .alternativeRoutes(false)
                    .key("AIzaSyDpSFLRfG3nCYUB7wjnLFjA4_eWE29cvKM")
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                    .build();
            routing.execute();
        }
    }


    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.ButtonColor};

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if (polylines != null) {
            if (polylines.size() > 0) {
                for (Polyline poly : polylines) {
                    poly.remove();

                }
            }
        } else polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {
            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(20 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
            Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {
    }

    private void erasePolylines() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();

    }


    private Marker mIncomerMarker;
    private DatabaseReference geoIncomerRef;
    private ValueEventListener IncomerLocationRefListener;
    String incomerId;

    private void getID(String id) {
        incomerId = id;

    }


    boolean notifConnect = false;
    boolean notifArrived = false;


    public void getConnectedNotification() {
        if (!notifConnect) {
            Intent notificationIntent = new Intent(this, NotificationService.class);
            notificationIntent.putExtra("ABC", "On His Way");
            ContextCompat.startForegroundService(MapActivity.this, notificationIntent);
            notifConnect = true;
        }
    }

    public void hasArrivedNotification() {
        if (!notifArrived) {
            Intent notificationIntent = new Intent(this, NotificationService.class);
            notificationIntent.putExtra("ABC", "Already There");
            ContextCompat.startForegroundService(MapActivity.this, notificationIntent);
            notifArrived = true;
        }
    }

    public void timerServiceStarter(){
        Intent intentTimer=new Intent(MapActivity.this,NotificationService.class);
         intentTimer.putExtra("ABC",sharedMinutes);
         ContextCompat.startForegroundService(MapActivity.this,intentTimer);

//        Intent intent = new Intent(CreateForegroundServiceActivity.this, MyForeGroundService.class);
//        intent.setAction(MyForeGroundService.ACTION_START_FOREGROUND_SERVICE);
//        startService(intent);


    }



    private void getIncomerLocation(final FirebaseDatabase database) {



        geoIncomerRef = database.getReference().child(Utils.FIREBASE_ROOT).child(Utils.INCOMER_LOCATION).child(incomerId).child("l");

        geoIncomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    if (mIncomerMarker != null) {
                        mIncomerMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(sharedLocation.latitude);
                    loc1.setLongitude(sharedLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);










                    float distance = loc2.distanceTo(loc1);
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11.5f));
//                    float  zooming =12.5f;
//                          if (distance<=1000){
//                              zooming=15.5f;
//                              mMap.animateCamera(CameraUpdateFactory.zoomTo(zooming));
//                          }
//                          else if (distance<=500){
//                              zooming=16.5f;
//                              mMap.animateCamera(CameraUpdateFactory.zoomTo(zooming));
//                          }
//                          else if (distance<=300){
//                              zooming=17.5f;
//                              mMap.animateCamera(CameraUpdateFactory.zoomTo(zooming));
//                          }
//                          else  if (distance<=150){
//                              zooming=18.5f;
//                              mMap.animateCamera(CameraUpdateFactory.zoomTo(zooming));
//                          }


                    String driverCurrentAddress;
                    String incomerDistanceInfo;
                    incomerDistanceInfo="\n"+"Distance : " + new DecimalFormat("##.##").format(distance) + "m"+"\n";

                    try {
                        driverCurrentAddress= addressGetter(driverLatLng);
                        int indexToCut=driverCurrentAddress.indexOf(",");
                        infoPanelText.setText(driverCurrentAddress.substring(0,indexToCut)+incomerDistanceInfo);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    Log.i("LOG",String.valueOf(distance));

                    if (distance < 60) {
                        hasArrivedNotification();




                    } else {
                        getConnectedNotification();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc2.getLatitude(), loc2.getLongitude()), 18f));


                    }
                    mIncomerMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("driver arrived").icon(BitmapDescriptorFactory.fromResource(R.drawable.incoming_car)));

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });


    }


    public static final String CHANNEL_ID = "NotificationChannelId";

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager;
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);

        }
    }

  public String addressGetter(LatLng currentPosition) throws IOException {
        String address = "";
        Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
        List<Address> addressList = geocoder.getFromLocation(currentPosition.latitude, currentPosition.longitude, 1);
        address = addressList.get(0).getAddressLine(0);

        return address;

    }


    public void showNumberPicker(){
        NumberPickerDialog newFragment = new NumberPickerDialog();
        newFragment.setValueChangeListener(this);
        newFragment.show(getSupportFragmentManager(), "time picker");
    }


    public void getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int strDate=calendar.get(Calendar.MINUTE);

//        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss");
//        String strDate = "Current Time : " + mdformat.format(calendar.getTime().getMinutes());

       // shareBtn.setText(String.valueOf(strDate));

    }

    public void checkGpsStatus(){


        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

       GpsStatus  =  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (GpsStatus){

            GPS_StatusText.setText(null);
            infoPanelText.setVisibility(View.VISIBLE);
            shareBtn.setClickable(true);
            nonSharedcancel.setClickable(true);
            shareConfirm.setClickable(true);
            if (mCurrentMarker!=null){
                mCurrentMarker.setVisible(true);}



        }
        else {
            GPS_StatusText.setText(Utils.NO_GPS);
            infoPanelText.setVisibility(View.GONE);
            shareBtn.setClickable(false);
            nonSharedcancel.setClickable(false);
            if (mCurrentMarker!=null){
                mCurrentMarker.setVisible(false);
           }

        }


    }




}

