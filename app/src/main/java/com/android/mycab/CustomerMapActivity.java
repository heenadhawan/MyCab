package com.android.mycab;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapActivity extends FragmentActivity
        implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    Button logoutbuttonCustomer,SettingCustomer ;
    FirebaseAuth mauth;
    FirebaseUser Currentuser;
    private Button CallCabCarButton;
    private String customerID;
    private DatabaseReference CustomerDatabaseRef;
    private DatabaseReference DriversAvailableRef;
    private DatabaseReference DriverRef;
    private DatabaseReference DriverLocationRef;
    private int radius = 1;
    private LatLng CustomerPickUpLocation;
    Marker DriverMarker, PickUpMarker;
    GeoQuery geoQuery;
    private ValueEventListener DriverLocationRefListner;
    private Boolean driverFound = false, requestType = false;
    private String driverFoundID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        mauth = FirebaseAuth.getInstance();
        Currentuser = mauth.getCurrentUser();
        customerID = FirebaseAuth.getInstance().getUid();
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customer Requests");
        DriversAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        DriverLocationRef= FirebaseDatabase.getInstance().getReference().child("DriversWorking");
        logoutbuttonCustomer = (Button) findViewById(R.id.logout_customer_btn);
        SettingCustomer = (Button) findViewById(R.id.settings_customer_btn);
        CallCabCarButton = (Button) findViewById(R.id.call_a_car_button);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        logoutbuttonCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mauth.signOut();

                LogoutCustomer();
            }

        });

        CallCabCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestType) {
                    requestType = false;
                    geoQuery.removeAllListeners();
                    DriverLocationRef.removeEventListener(DriverLocationRefListner);

                    if (driverFound != null)
                    {
                        DriverRef = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child("Drivers").child(driverFoundID).child("CustomerRideID");

                        DriverRef.removeValue();

                        driverFoundID = null;
                    }

                    driverFound = false;
                    radius = 1;

                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                    geoFire.removeLocation(customerId);

                    if (PickUpMarker != null)
                    {
                        PickUpMarker.remove();
                    }
                    if (DriverMarker != null)
                    {
                        DriverMarker.remove();
                    }

                    CallCabCarButton.setText("Call a Cab");

                }
                else
                {
                    requestType = true;

                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                    geoFire.setLocation(customerId, new GeoLocation(lastlocation.getLatitude(), lastlocation.getLongitude()));

                    CustomerPickUpLocation = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                    PickUpMarker = mMap.addMarker(new MarkerOptions().position(CustomerPickUpLocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                    CallCabCarButton.setText("Getting your Driver...");
                    getClosetDriverCab();
                }
            }
        });
    }

//this method is call again and again searching driver

    private void getClosetDriverCab() {
        GeoFire geoFire = new GeoFire(DriversAvailableRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(CustomerPickUpLocation.latitude, CustomerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (driverFound != null)
                {
                   driverFound = true;
                    driverFoundID = key;
                    DriverRef = FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Drivers").child(driverFoundID);
                          HashMap drivermap = new HashMap();
                          drivermap.put("CustomerRideId",customerID);
                          DriverRef.updateChildren(drivermap);
                          GettingDriverLocation();
                          CallCabCarButton.setText("Looking for  driver location");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                 if(!driverFound)
                 {
               radius = radius +1;
               getClosetDriverCab();
         }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

      private void GettingDriverLocation() {
      DriverLocationRef.child(driverFoundID).child("l")
     .addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
          if(dataSnapshot.exists()  &&  requestType){
            List<Object> driverlocationMap = (List<Object>) dataSnapshot.getValue();
            double LocationLat = 0;
            double LocationLng = 0;
            CallCabCarButton.setText("Driver Found");
            if(driverlocationMap.get(0)!=null) {
                LocationLat = Double.parseDouble(driverlocationMap.get(0).toString());
            }
            if(driverlocationMap.get(1)!=null) {
                LocationLng = Double.parseDouble(driverlocationMap.get(1).toString());
            }
            //adding marker - to pointing where driver is - using this lat lng

            LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
            if(DriverMarker != null)
            {
                DriverMarker.remove();
            }

            Location location1 = new Location("");
            location1.setLatitude(CustomerPickUpLocation.latitude);
            location1.setLongitude(CustomerPickUpLocation.longitude);

            Location location2 = new Location("");
            location2.setLatitude(DriverLatLng.latitude);
            location2.setLongitude(DriverLatLng.longitude);

            float Distance = location1.distanceTo(location2);

            if (Distance < 90)
            {
                CallCabCarButton.setText("Driver's Reached");
            }
            else
            {
                CallCabCarButton.setText("Driver Found: " + String.valueOf(Distance));
            }
            DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("your driver is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
        }
        }


    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildgoogleapiclient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildgoogleapiclient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    public void LogoutCustomer()
    {
        Intent startPageIntent = new Intent(CustomerMapActivity.this, WelcomeActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastlocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

    }
}
