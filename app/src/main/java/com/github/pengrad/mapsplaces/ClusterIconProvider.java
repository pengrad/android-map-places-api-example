package com.github.pengrad.mapsplaces;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.androidmapsextensions.ClusterOptions;
import com.androidmapsextensions.ClusterOptionsProvider;
import com.androidmapsextensions.Marker;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.List;

/**
 * Stas Parshin
 * 30 November 2015
 */
public class ClusterIconProvider implements ClusterOptionsProvider {

    private static final int[] forCounts = {10, 100, 1000, 10000, Integer.MAX_VALUE};

    private Bitmap[] baseBitmaps;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect bounds = new Rect();

//    private MarkerOptions markerOptions = new MarkerOptions().anchor(0.5f, 0.5f);
    private ClusterOptions clusterOptions = new ClusterOptions().anchor(0.5f, 0.5f);

    public ClusterIconProvider(Resources resources) {
        baseBitmaps = new Bitmap[1];
        baseBitmaps[0] = BitmapFactory.decodeResource(resources, R.drawable.m1);

        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(15);
    }

    @Override
    public ClusterOptions getClusterOptions(List<Marker> list) {
        Bitmap base = baseBitmaps[0];
        Bitmap bitmap = base.copy(Bitmap.Config.ARGB_8888, true);

        String text = String.valueOf(list.size());
        paint.getTextBounds(text, 0, text.length(), bounds);
        float x = bitmap.getWidth() / 2.0f;
        float y = (bitmap.getHeight() - bounds.height()) / 2.0f - bounds.top;

        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, x, y, paint);

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
        return clusterOptions.icon(icon);
    }
}
