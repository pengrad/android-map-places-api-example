package com.github.pengrad.mapsplaces;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

import io.github.axxiss.places.PlacesSettings;
import io.github.axxiss.places.Response;
import io.github.axxiss.places.callback.PlacesCallback;
import io.github.axxiss.places.enums.Params;
import io.github.axxiss.places.enums.PlaceType;
import io.github.axxiss.places.enums.Request;
import io.github.axxiss.places.model.Location;
import io.github.axxiss.places.model.Place;
import io.github.axxiss.places.request.NearbySearch;
import io.github.axxiss.places.request.PlaceParams;
import io.github.axxiss.places.request.PlacesClient;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener {

    public static LatLng HANOI_LOCATION = new LatLng(21.0274259, 105.8222217);
    public static float DEFAULT_ZOOM = 14;

    private GoogleMap mMap;

    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ListView listView = (ListView) findViewById(R.id.listview);
        arrayAdapter  = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(arrayAdapter);

        Toolbar slideToolbar = (Toolbar) findViewById(R.id.slide_toolbar);
        TextView sliteTitle = (TextView) findViewById(R.id.slide_title);
        SlidingUpPanelLayout slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setDragView(sliteTitle);
        slidingUpPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.SimplePanelSlideListener() {
            @Override
            public void onPanelExpanded(View panel) {
                toolbar.setVisibility(View.INVISIBLE);
                sliteTitle.setVisibility(View.GONE);
                slideToolbar.setVisibility(View.VISIBLE);
                slideToolbar.setTitle("Results");
                slideToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

//                slideToolbar.setTitleTextColor(getColor(android.R.color.white));
                slideToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
                slideToolbar.setNavigationOnClickListener(v -> slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED));
            }

            @Override
            public void onPanelCollapsed(View panel) {
                toolbar.setVisibility(View.VISIBLE);
                slideToolbar.setVisibility(View.GONE);
                sliteTitle.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPanelAnchored(View panel) {
                toolbar.setVisibility(View.VISIBLE);
                slideToolbar.setVisibility(View.GONE);
                sliteTitle.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.setMyLocationEnabled(false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HANOI_LOCATION, DEFAULT_ZOOM));
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraChangeListener(this);
        UiSettings mapUI = mMap.getUiSettings();
        mapUI.setMapToolbarEnabled(false);
        searchPlaces(HANOI_LOCATION);
    }

    private void searchPlaces(LatLng latLng) {
        PlacesSettings.getInstance().setApiKey(getString(R.string.google_places_key));

        List<PlaceType> placeTypes = new ArrayList<>();
        placeTypes.add(PlaceType.Cafe);

        NearbySearch search = new NearbySearch(latLng.latitude, latLng.longitude, 1000);
        search.setType(placeTypes);

        PlaceParams params = new PlaceParams();
        params.put(Params.Location, PlaceParams.buildLocation(latLng.latitude, latLng.longitude));
        params.put(Params.Radius, 1000);
        params.setTypes(new PlaceType[]{PlaceType.Cafe});

        PlacesClient.sendRequest(Request.NearbySearch, params, new PlacesCallback() {
            @Override
            public void onSuccess(Response response) {
                for (Place place : response.getResults()) {
                    arrayAdapter.add(place.getName());

                    Log.d("+++++", place.getName() + " " + place.getTypes());
                    Location location = place.getGeometry().getLocation();
                    LatLng position = new LatLng(location.getLat(), location.getLng());
                    mMap.addMarker(new MarkerOptions().title(place.getName()).position(position));
                }
            }

            @Override
            public void onException(Exception e) {
                Log.e("++++", e.getMessage(), e);
            }
        });
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }
}
