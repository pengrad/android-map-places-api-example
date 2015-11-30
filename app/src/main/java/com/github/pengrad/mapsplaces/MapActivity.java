package com.github.pengrad.mapsplaces;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.OnMapReadyCallback;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.axxiss.places.model.Place;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MapActivity extends RxAppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener, GoogleMap.OnMarkerClickListener {

    public static final String EXTRA_TYPE = "TYPE";

    public static final int DEFAULT_RADIUS = 5000;
    public static LatLng HANOI_LOCATION = new LatLng(21.0274259, 105.8222217);
    public static float DEFAULT_ZOOM = 13;

    private GooglePlaceAdapter googlePlaceAdapter;
    private Location location;
    private GoogleMap map;
    private MyRecyclerViewAdapter adapter;
    private String placeType;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.recycler_view) RecyclerView recyclerView;
    @Bind(R.id.sliding_layout) SlidingUpPanelLayout slidingUpPanelLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ButterKnife.bind(this);

        initToolbar();
        initListView();
        initSlider();

        placeType = getIntent().getStringExtra(EXTRA_TYPE);
        googlePlaceAdapter = new GooglePlaceAdapter(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getExtendedMapAsync(this);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void initListView() {
        adapter = new MyRecyclerViewAdapter((item, view, adapterPosition) -> Toast.makeText(this, item.getName(), Toast.LENGTH_SHORT).show());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void initSlider() {
        Toolbar slideToolbar = (Toolbar) findViewById(R.id.slide_toolbar);
        TextView slideTitle = (TextView) findViewById(R.id.slide_title);
        int titleColor = getResources().getColor(android.R.color.white);
        SliderListener listener = new SliderListener(toolbar, slideToolbar, slideTitle, recyclerView, slidingUpPanelLayout, titleColor);
        slidingUpPanelLayout.setDragView(slideTitle);
        slidingUpPanelLayout.setPanelSlideListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.setMyLocationEnabled(false);
    }

    @Override
    public void onBackPressed() {
        SlidingUpPanelLayout.PanelState state = slidingUpPanelLayout.getPanelState();
        if (state == SlidingUpPanelLayout.PanelState.ANCHORED ||
                state == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else super.onBackPressed();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setClustering(new ClusteringSettings().clusterOptionsProvider(new ClusterIconProvider(getResources())).addMarkersDynamically(true));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(HANOI_LOCATION, DEFAULT_ZOOM));
        map.setOnMyLocationChangeListener(this);
        map.setMyLocationEnabled(true);
        map.setOnMarkerClickListener(this);

        UiSettings mapUI = map.getUiSettings();
        mapUI.setMapToolbarEnabled(false);
        mapUI.setMyLocationButtonEnabled(false);

        getLocationAndSearch();
    }

    @Override
    public void onMyLocationChange(Location location) {
        this.location = location;
        map.setOnMyLocationChangeListener(null);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker == null) {
            // shit happens (Note 2, android 4.1)
            return false;
        }
        if (marker.isCluster()) {
            List<Marker> markers = marker.getMarkers();
            LatLngBounds.Builder builder = LatLngBounds.builder();
            for (Marker m : markers) {
                builder.include(m.getPosition());
            }
            LatLngBounds bounds = builder.build();
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            return true;
        }
        return false;
    }

    @OnClick(R.id.button_plus)
    void mapDoZoomPlus() {
        map.animateCamera(CameraUpdateFactory.zoomIn());
    }

    @OnClick(R.id.button_minus)
    void mapDoZoomMinus() {
        map.animateCamera(CameraUpdateFactory.zoomOut());
    }

    @OnClick(R.id.button_location)
    void onMyLocationButton() {
        Location location = map.getMyLocation();
        if (location != null) {
            LatLng l = new LatLng(location.getLatitude(), location.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(l, map.getCameraPosition().zoom));
        }
    }

    @OnClick(R.id.button_loadMore)
    void loadMore() {
        execute(googlePlaceAdapter.getNextPage());
    }

    private void getLocationAndSearch() {
        Toast.makeText(this, "Getting location...", Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        Runnable checkLocationTask = new Runnable() {
            int count = 0;

            @Override
            public void run() {
                if (location != null || count > 5) {
                    LatLng location = locationForSearch();
                    adapter.setBaseLocation(location);
                    searchPlaces(location);
                } else {
                    handler.postDelayed(this, 1000);
                }
                count++;
            }
        };
        checkLocationTask.run();
    }

    private LatLng locationForSearch() {
        LatLng position = HANOI_LOCATION;
        Location location = this.location != null ? this.location : map.getMyLocation();
        if (location == null) {
            Toast.makeText(this, "Can't get location, use default - Hanoi", Toast.LENGTH_SHORT).show();
        } else {
            position = new LatLng(location.getLatitude(), location.getLongitude());
        }
        return position;
    }

    private void searchPlaces(LatLng latLng) {
        execute(googlePlaceAdapter.getPlacesByType(latLng, DEFAULT_RADIUS, placeType));
    }

    private void execute(Observable<Place[]> observable) {
        observable.compose(bindUntilEvent(ActivityEvent.STOP))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(e -> new Place[0])
                .subscribe(places -> {
                    adapter.addAll(places);
                    for (Place place : places) {
                        io.github.axxiss.places.model.Location location = place.getGeometry().getLocation();
                        LatLng pos = new LatLng(location.getLat(), location.getLng());
                        Marker marker = map.addMarker(new MarkerOptions().title(place.getName()).position(pos));
                        ImageUtils.loadMarkerIcon(this, marker, place.getIcon());
                    }
                });
    }
}
