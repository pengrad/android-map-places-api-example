package com.github.pengrad.mapsplaces;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Stas Parshin
 * 30 November 2015
 */
public class ImageUtils {

    public static final String PHOTO_URL = "https://maps.googleapis.com/maps/api/place/photo?photoreference=%s&key=%s&maxheight=100";

    public static void loadImage(Context context, ImageView imageView, String photoreference) {
        String url = String.format(PHOTO_URL, photoreference, context.getString(R.string.google_places_key));
        Glide.with(context).load(url).centerCrop().into(imageView);
    }

}
