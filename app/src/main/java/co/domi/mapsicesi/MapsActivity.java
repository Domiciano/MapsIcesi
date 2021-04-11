package co.domi.mapsicesi;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private String user;
    private LocationManager manager;
    private Marker me;
    private ArrayList<Marker> points;
    private Button addBtn;
    private TextView title;

    //Lugares
    private Polygon icesi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        addBtn = findViewById(R.id.addBtn);
        title = findViewById(R.id.title);

        user = getIntent().getExtras().getString("user");
        points = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 2, this);

        setInitialPos();

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        addBtn.setOnClickListener(
                (v)->{

                }
        );


        icesi = mMap.addPolygon(
                new PolygonOptions()
                        .add(new LatLng(3.3431395682235645, -76.53093673288822))
                        .add(new LatLng(3.343433774359046, -76.52729094028473))
                        .add(new LatLng(3.341588541472695, -76.52730971574783))
                        .add(new LatLng(3.341492815529008, -76.52833364903927))
                        .add(new LatLng(3.3400505658279447, -76.52844831347466))
                        .add(new LatLng(3.339813928229764, -76.5312260761857))
                        .add(new LatLng(3.3431395682235645, -76.53093673288822))
                        .fillColor( Color.argb(10, 255,0,0) )
                        .strokeColor( Color.BLACK )
        );


        Toast.makeText(this, "Espere mientras lo ubicamos (NETWORK PROVIDER)", Toast.LENGTH_LONG).show();
    }


    @SuppressLint("MissingPermission")
    public void setInitialPos(){
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null) {
            updateMyLocation(location);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        updateMyLocation(location);

        boolean imAtIcesi = PolyUtil.containsLocation(new LatLng(location.getLatitude(), location.getLongitude()), icesi.getPoints(), false);
        if(imAtIcesi){
            addBtn.setText("Esta en Icesi");
        }

    }

    public void updateMyLocation(Location location){
        LatLng myPos = new LatLng(location.getLatitude(), location.getLongitude());
        if(me == null){
            me = mMap.addMarker(new MarkerOptions().position(myPos).title("Yo"));
        }else{
            me.setPosition(myPos);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPos, 15));

        computeDistances();

    }

    private void computeDistances() {

        for(int i=0 ; i<points.size() ; i++){
            Marker marker = points.get(i);
            LatLng markerLoc = marker.getPosition();
            LatLng meLoc = me.getPosition();

            double meters = SphericalUtil.computeDistanceBetween(markerLoc, meLoc);

            Log.e(">>>", "Metros a marcador " + i + ": " + meters + "m");

            if(meters<50){
                addBtn.setText("Usted esta pisando un marcador");
            }

        }

        if(icesi != null) {
            double distanceToIcesi = 1000000000;
            for (int i = 0; i < icesi.getPoints().size(); i++) {
                LatLng punto = icesi.getPoints().get(i);
                double meters = SphericalUtil.computeDistanceBetween(punto, me.getPosition());
                distanceToIcesi = Math.min(meters, distanceToIcesi);
            }
            title.setText("Distancia a Icesi: " + distanceToIcesi);
        }


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

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom( latLng , 16));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Marker p = mMap.addMarker(new MarkerOptions().position(latLng).title("Marcador").snippet("Este es un marcador de prueba"));
        points.add(p);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, marker.getPosition().latitude + ", " + marker.getPosition().longitude, Toast.LENGTH_LONG).show();
        Log.e(">>>",marker.getPosition().latitude + ", " + marker.getPosition().longitude);
        marker.showInfoWindow();
        return true;
    }
}