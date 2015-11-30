package com.github.pengrad.mapsplaces;

import android.content.Context;

import io.github.axxiss.places.PlacesSettings;

/**
 * Stas Parshin
 * 30 November 2015
 */
public class GooglePlaceAdapter {

    public GooglePlaceAdapter(Context context) {
        PlacesSettings.getInstance().setApiKey(context.getString(R.string.google_places_key));
    }

    /*

    List<PlaceType> placeTypes = new ArrayList<>();
    placeTypes.add(PlaceType.Cafe);

    NearbySearch search = new NearbySearch(latLng.latitude, latLng.longitude, 1000);
    search.setType(placeTypes);

    PlaceParams params = new PlaceParams();
    params.put(Params.Location,PlaceParams.buildLocation(latLng.latitude,latLng.longitude));
    params.put(Params.Radius,1000);
    params.setTypes(new PlaceType[]

    {PlaceType.Cafe}

    );

    PlacesClient.sendRequest(Request.NearbySearch,params,new

    PlacesCallback() {
        @Override
        public void onSuccess (Response response){
            adapter.addAll(response.getResults());
            for (Place place : response.getResults()) {
                Log.d("+++++", place.getName() + " " + place.getTypes());
                io.github.axxiss.places.model.Location location = place.getGeometry().getLocation();
                LatLng position = new LatLng(location.getLat(), location.getLng());
                mMap.addMarker(new MarkerOptions().title(place.getName()).position(position));
            }
        }

        @Override
        public void onException (Exception e){
            Log.e("++++", e.getMessage(), e);
        }
    }

    );
*/
}
