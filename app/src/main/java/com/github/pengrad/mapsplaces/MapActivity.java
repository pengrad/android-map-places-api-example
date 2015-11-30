package com.github.pengrad.mapsplaces;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.OnMapReadyCallback;
import com.androidmapsextensions.SupportMapFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.pengrad.recyclerview.RecyclerViewHolder;
import com.github.pengrad.recyclerview.RecyclerViewListAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.axxiss.places.model.Place;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MapActivity extends RxAppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener, GoogleMap.OnMarkerClickListener {

    public static final int DEFAULT_RADIUS = 1000;
    public static LatLng HANOI_LOCATION = new LatLng(21.0274259, 105.8222217);
    public static float DEFAULT_ZOOM = 14;

    private GoogleMap mMap;

    private RecyclerViewListAdapter<Place> adapter;
    private Location mLocation;

    private GooglePlaceAdapter googlePlaceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ButterKnife.bind(this);

        googlePlaceAdapter = new GooglePlaceAdapter(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getExtendedMapAsync(this);

        adapter = new RecyclerViewListAdapter<Place>((item, view, adapterPosition) -> {
            Toast.makeText(this, item.getName(), Toast.LENGTH_SHORT).show();
        }) {
            @Override
            public RecyclerViewHolder<Place> onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_list_item, parent, false);
                return new RecyclerViewHolder<Place>(view) {
                    @Override
                    public void onBindItem(Place item) {
                        TextView textView = (TextView) itemView.findViewById(R.id.place_title);
                        textView.setText(item.getName());
                    }
                };
            }
        };
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

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
                recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
                int height = slidingUpPanelLayout.getHeight() / 2 - slidingUpPanelLayout.getPanelHeight() / 2;
                recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
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
        mMap.setClustering(new ClusteringSettings().clusterOptionsProvider(new ClusterIconProvider(getResources())).addMarkersDynamically(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HANOI_LOCATION, DEFAULT_ZOOM));
        mMap.setOnMyLocationChangeListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);

        UiSettings mapUI = mMap.getUiSettings();
        mapUI.setMapToolbarEnabled(false);
        mapUI.setMyLocationButtonEnabled(false);

        getLocationAndSearch();
    }

    private void getLocationAndSearch() {
        Toast.makeText(this, "Getting location...", Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        Runnable checkLocationTask = new Runnable() {
            int count = 0;

            @Override
            public void run() {
                if (mLocation != null || count > 5) {
                    searchPlaces(locationForSearch());
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
        Location location = mLocation != null ? mLocation : mMap.getMyLocation();
        if (location == null) {
            Toast.makeText(this, "Can't get location, use default - Hanoi", Toast.LENGTH_SHORT).show();
        } else {
            position = new LatLng(location.getLatitude(), location.getLongitude());
        }
        return position;
    }

    @Override
    public void onMyLocationChange(Location location) {
        mLocation = location;
        mMap.setOnMyLocationChangeListener(null);
    }

    @OnClick(R.id.button_plus)
    void mapDoZoomPlus() {
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
    }

    @OnClick(R.id.button_minus)
    void mapDoZoomMinus() {
        mMap.animateCamera(CameraUpdateFactory.zoomOut());
    }

    @OnClick(R.id.button_location)
    void onMyLocationButton() {
        Location location = mMap.getMyLocation();
        if (location != null) {
            LatLng l = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, mMap.getCameraPosition().zoom));
        }
    }

    @OnClick(R.id.button_loadMore)
    void loadMore() {
        execute(googlePlaceAdapter.getNextPage());
    }

    private void searchPlaces(LatLng latLng) {
        execute(googlePlaceAdapter.getAllPlaces(latLng, DEFAULT_RADIUS));
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
                        Marker marker = mMap.addMarker(new MarkerOptions().title(place.getName()).position(pos));
                        loadMarkerIcon(marker, place.getIcon());
                    }
                });
    }

    private void loadMarkerIcon(final Marker marker, String iconUrl) {
        if (TextUtils.isEmpty(iconUrl)) {
            return;
        }
        Glide.with(this).load(iconUrl).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
                marker.setIcon(icon);
            }
        });
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
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            return true;
        }
        return false;
    }
}
