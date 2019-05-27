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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

import java.util.List;

public class DriverMapActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    Button logoutbuttonDriver,SeetingDriver ;
     FirebaseAuth mauth;
    FirebaseUser Currentuser;
    private Boolean currentLogOutUserStatus = false;
    //getting request customer's id
    private String customerID = "";
    private String driverID;
    private DatabaseReference AssignedCustomerRef;
    private DatabaseReference AssignedCustomerPickUpRef;
    Marker PickUpMarker;
    private ValueEventListener AssignedCustomerPickUpRefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        mauth = FirebaseAuth.getInstance();
        driverID = mauth.getCurrentUser().getUid();
        Currentuser = mauth.getCurrentUser();
        logoutbuttonDriver = (Button)findViewById(R.id.logout_driv_btn);
        SeetingDriver = (Button)findViewById(R.id.settings_driver_btn);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        logoutbuttonDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLogOutUserStatus = true;
                DisconnectDriver();
                mauth.signOut();
                LogoutDriver();
            }
        });

        getAssignedCustomersRequest();
    }

    private void getAssignedCustomersRequest() {
        AssignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverID).child("CustomerRideId");
       AssignedCustomerPickUpRefListener= AssignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    customerID = dataSnapshot.getValue().toString();
                    //getting assigned customer location
                    GetAssignedCustomerPickupLocation();
                } else {
                    customerID = "";

                    if (PickUpMarker != null) {
                        PickUpMarker.remove();
                    }
                    if (AssignedCustomerPickUpRefListener != null) {
                        AssignedCustomerPickUpRef.removeEventListener(AssignedCustomerPickUpRefListener);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void GetAssignedCustomerPickupLocation() {
        AssignedCustomerPickUpRef = FirebaseDatabase.getInstance().getReference().child("Customer Requests")
                .child(customerID).child("l");
        AssignedCustomerPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    List<Object> customerLocationMap = (List<Object>) dataSnapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 0;

                    if(customerLocationMap.get(0) != null)
                    {
                        LocationLat = Double.parseDouble(customerLocationMap.get(0).toString());
                    }
                    if(customerLocationMap.get(1) != null)
                    {
                        LocationLng = Double.parseDouble(customerLocationMap.get(1).toString());
                    }

                    LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
                    PickUpMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Customer PickUp Location"));
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }
            private void LogoutDriver() {

                Intent welcomeintent = new Intent(DriverMapActivity.this, WelcomeActivity.class);
                welcomeintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(welcomeintent);
                finish();
            }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildgoogleapiclient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);

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

        if(getApplicationContext() != null)
        {
            //getting the updated location
            lastlocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));


            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference DriversAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
            GeoFire geoFireAvailability = new GeoFire(DriversAvailabilityRef);

            DatabaseReference DriversWorkingRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
            GeoFire geoFireWorking = new GeoFire(DriversWorkingRef);

            switch (customerID)
            {
                case "":
                    geoFireWorking.removeLocation(userID);
                    geoFireAvailability.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

                default:
                    geoFireAvailability.removeLocation(userID);
                    geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
        }

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
        if(!currentLogOutUserStatus)
        {
            DisconnectDriver();
        }
       }

    private void DisconnectDriver()
    {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference DriversAvailabiltyRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");

        GeoFire geoFire = new GeoFire(DriversAvailabiltyRef);
        geoFire.removeLocation(userID);
    }

}
