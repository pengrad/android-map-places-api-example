package com.github.pengrad.mapsplaces;

import android.content.Context;
import android.location.Location;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pengrad.recyclerview.RecyclerViewHolder;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.axxiss.places.model.Photo;
import io.github.axxiss.places.model.Place;

/**
 * Stas Parshin
 * 01 December 2015
 */
public class MyRecyclerViewHolder extends RecyclerViewHolder<Place> {

    @Bind(R.id.place_title) TextView textTitle;
    @Bind(R.id.place_distance) TextView textDistance;
    @Bind(R.id.place_image) ImageView imageView;

    private LatLng baseLocation;

    public MyRecyclerViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public MyRecyclerViewHolder setBaseLocation(LatLng baseLocation) {
        this.baseLocation = baseLocation;
        return this;
    }

    @Override
    public void onBindItem(Place item) {
        textTitle.setText(item.getName());
        setImage(item);
        setDistance(item);
    }

    private void setImage(Place item) {
        Context context = itemView.getContext();
        List<Photo> photos = item.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            ImageUtils.loadGooglePhoto(context, imageView, photos.get(0).getPhoto_reference());
        } else {
            ImageUtils.loadIcon(context, item.getIcon(), imageView);
        }
    }

    private void setDistance(Place item) {
        if (baseLocation == null) {
            textDistance.setText("0 m");
            return;
        }
        double itemLat = item.getGeometry().getLocation().getLat();
        double itemLong = item.getGeometry().getLocation().getLng();
        float[] result = new float[1];
        Location.distanceBetween(baseLocation.latitude, baseLocation.longitude, itemLat, itemLong, result);
        textDistance.setText(String.format("%d m", (int) result[0]));
    }
}
