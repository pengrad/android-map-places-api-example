package com.github.pengrad.mapsplaces;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pengrad.recyclerview.RecyclerViewHolder;
import com.github.pengrad.recyclerview.RecyclerViewListAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.axxiss.places.PlacesSettings;
import io.github.axxiss.places.Response;
import io.github.axxiss.places.callback.PlacesCallback;
import io.github.axxiss.places.enums.Params;
import io.github.axxiss.places.enums.PlaceType;
import io.github.axxiss.places.enums.Request;
import io.github.axxiss.places.model.Place;
import io.github.axxiss.places.request.NearbySearch;
import io.github.axxiss.places.request.PlaceParams;
import io.github.axxiss.places.request.PlacesClient;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener {

    public static LatLng HANOI_LOCATION = new LatLng(21.0274259, 105.8222217);
    public static float DEFAULT_ZOOM = 14;

    private GoogleMap mMap;

    private RecyclerViewListAdapter<Place> adapter;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HANOI_LOCATION, DEFAULT_ZOOM));
        mMap.setOnMyLocationChangeListener(this);
        mMap.setMyLocationEnabled(true);
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
                adapter.addAll(response.getResults());
                for (Place place : response.getResults()) {
                    Log.d("+++++", place.getName() + " " + place.getTypes());
                    io.github.axxiss.places.model.Location location = place.getGeometry().getLocation();
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
    public void onMyLocationChange(Location location) {
        mLocation = location;
        mMap.setOnMyLocationChangeListener(null);
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

    }
}
