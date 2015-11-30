package com.github.pengrad.mapsplaces;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.pengrad.recyclerview.ItemClickListener;
import com.github.pengrad.recyclerview.RecyclerViewHolder;
import com.github.pengrad.recyclerview.RecyclerViewListAdapter;
import com.google.android.gms.maps.model.LatLng;

import io.github.axxiss.places.model.Place;

/**
 * Stas Parshin
 * 01 December 2015
 */
public class MyRecyclerViewAdapter extends RecyclerViewListAdapter<Place> {

    private LatLng baseLocation;

    public MyRecyclerViewAdapter(ItemClickListener<Place> itemClickListener) {
        super(itemClickListener);
    }

    public void setBaseLocation(LatLng baseLocation) {
        this.baseLocation = baseLocation;
    }

    @Override
    public RecyclerViewHolder<Place> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_list_item, parent, false);
        return new MyRecyclerViewHolder(view).setBaseLocation(baseLocation);
    }
}
