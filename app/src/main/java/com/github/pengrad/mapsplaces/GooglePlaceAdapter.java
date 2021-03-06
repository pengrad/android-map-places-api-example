package com.github.pengrad.mapsplaces;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import io.github.axxiss.places.PlacesSettings;
import io.github.axxiss.places.Response;
import io.github.axxiss.places.callback.PlacesCallback;
import io.github.axxiss.places.enums.Params;
import io.github.axxiss.places.enums.Request;
import io.github.axxiss.places.model.Place;
import io.github.axxiss.places.request.PlaceParams;
import io.github.axxiss.places.request.PlacesClient;
import rx.Observable;
import rx.Subscriber;

/**
 * Stas Parshin
 * 30 November 2015
 */
public class GooglePlaceAdapter {

    private String nextPageToken;

    public GooglePlaceAdapter() {
        PlacesSettings.getInstance().setApiKey(BuildConfig.GOOGLE_PLACES_API_KEY);
    }

    public Observable<Place[]> getPlacesByType(LatLng latLng, int radius, @Nullable String placeType) {
        return Observable.create(subscriber -> {
            PlaceParams params = new PlaceParams();
            params.put(Params.Location, PlaceParams.buildLocation(latLng.latitude, latLng.longitude));
            params.put(Params.Radius, radius);
            if (!TextUtils.isEmpty(placeType)) {
                params.put(Params.Types, placeType);
            }
            executeRequest(subscriber, params);
        });
    }

    public Observable<Place[]> getAllPlaces(LatLng latLng, int radius) {
        return getPlacesByType(latLng, radius, null);
    }

    public Observable<Place[]> getNextPage() {
        return Observable.create(subscriber -> {
            if (TextUtils.isEmpty(nextPageToken)) {
                subscriber.onCompleted();
                return;
            }
            PlaceParams params = new PlaceParams();
            params.put(Params.PageToken, nextPageToken);
            executeRequest(subscriber, params);
        });
    }

    private void executeRequest(Subscriber<? super Place[]> subscriber, PlaceParams params) {
        PlacesClient.sendRequest(Request.NearbySearch, params, new PlacesCallback() {
            @Override
            public void onSuccess(Response response) {
                nextPageToken = response.getNextPageToken();
                subscriber.onNext(response.getResults());
                subscriber.onCompleted();
            }

            @Override
            public void onException(Exception e) {
                Log.e("++++", e.getMessage(), e);
                subscriber.onError(e);
            }
        });
    }

}
