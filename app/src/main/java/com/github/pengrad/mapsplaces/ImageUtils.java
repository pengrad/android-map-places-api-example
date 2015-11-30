package com.github.pengrad.mapsplaces;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.androidmapsextensions.Marker;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Stas Parshin
 * 30 November 2015
 */
public class ImageUtils {

    public static final String PHOTO_URL = "https://maps.googleapis.com/maps/api/place/photo?photoreference=%s&key=%s&maxheight=100";

    public static void loadGooglePhoto(Context context, ImageView imageView, String photoreference) {
        String url = String.format(PHOTO_URL, photoreference, context.getString(R.string.google_places_key));
        loadIcon(context, url, imageView);
    }

    public static void loadIcon(Context context, String url, ImageView imageView) {
        if (TextUtils.isEmpty(url)) return;
        Glide.with(context).load(url).centerCrop().into(imageView);
    }

    public static void loadMarkerIcon(Context context, Marker marker, String iconUrl) {
        if (TextUtils.isEmpty(iconUrl)) return;
        Glide.with(context).load(iconUrl).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
                marker.setIcon(icon);
            }
        });
    }

}
