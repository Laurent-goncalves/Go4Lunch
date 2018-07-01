package com.g.laurent.go4lunch.Models;

import android.content.Context;
import android.widget.Toast;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.DetailsPlace.DetailsPlace;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Photo;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Result;
import com.g.laurent.go4lunch.Utils.MapsAPIstream;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;


public class ListSearchNearby implements Disposable {

    private List<PlaceNearby> list_places_nearby;
    private Disposable disposable;
    private MapsFragment mapsFragment;
    private ListRestoFragment listRestoFragment;
    private final static String CALLBACK_MAPS_FRAGMENT = "callbasck_maps_fragment";
    private final static String CALLBACK_LIST_RESTO_FRAGMENT = "callbasck_list_resto_fragment";
    private Context context;

    public ListSearchNearby(Context context, String api_key, LatLng latLng, String radius, String type, CallbackMultiActivity callbackMultiActivity) {
        list_places_nearby = new ArrayList<>();
        this.context=context;
        launch_request_search_nearby_places(api_key, latLng, radius, type, callbackMultiActivity);
    }

    private void build_list_places_nearby(DetailsPlace detailsPlace){

        if(detailsPlace!=null){
            if(detailsPlace.getResult()!=null){

                Result result = detailsPlace.getResult();

                // Get photo reference
                String photo_ref = null;
                if(result.getPhotos()!=null){

                    for(Photo photo : result.getPhotos()){
                        if(photo!=null){
                            photo_ref = photo.getPhotoReference();
                            if(photo_ref!=null)
                                break;
                        }
                    }
                }

                // Create PlaceNearby
                list_places_nearby.add(new PlaceNearby(result.getName(),
                        result.getPlaceId(),
                        result.getGeometry(),
                        result.getOpeningHours(),
                        result.getRating(),
                        result.getTypes(),
                        result.getVicinity(),
                        result.getFormattedPhoneNumber(),
                        photo_ref,
                        result.getWebsite(),
                        result.getIcon()));
            }
        }
    }

    private void launch_request_search_nearby_places(String api_key,LatLng latLng, String radius, String type, CallbackMultiActivity callbackMultiActivity){

        String location = String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude);

        disposable = MapsAPIstream.streamFetchgetSearchNearbyPlaces(api_key,type,radius,location)
                .flatMap(userResponse -> Observable.just(userResponse.getResults()))
                .flatMapIterable(ids -> ids)
                .flatMap(result -> MapsAPIstream.streamFetchgetDetailsPlaces(api_key,result))
                .subscribeWith(getSubscriber(callbackMultiActivity));
    }

    private DisposableObserver<DetailsPlace> getSubscriber(CallbackMultiActivity callbackMultiActivity){
        return new DisposableObserver<DetailsPlace>() {
            @Override
            public void onNext(DetailsPlace detailsPlace) {
                build_list_places_nearby(detailsPlace);
            }

            @Override
            public void onError(Throwable e) {
                callbackMultiActivity.message_error_API_request(e.toString());
            }

            @Override
            public void onComplete() {
                if(callbackMultiActivity!=null)
                    callbackMultiActivity.configureViewPagerAndTabs(list_places_nearby);
            }
        };
    }

    @Override
    public void dispose() {
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    // ------------------------------------------------------------------------------------------------
    // ------------------------------ AUTOCOMPLETE ----------------------------------------------------
    // ------------------------------------------------------------------------------------------------

    public ListSearchNearby(Context context, String api_key, List<String> list_placeId, MapsFragment mapsFragment) {
        // CONSTRUCTOR FOR AUTOCOMPLETE METHOD
        this.mapsFragment=mapsFragment;
        this.context=context;
        list_places_nearby = new ArrayList<>();
        launch_request_search_nearby_places_autocomplete(api_key, list_placeId, CALLBACK_MAPS_FRAGMENT);
    }

    public ListSearchNearby(Context context, String api_key, List<String> list_placeId, ListRestoFragment listRestoFragment) {
        // CONSTRUCTOR FOR AUTOCOMPLETE METHOD
        this.listRestoFragment=listRestoFragment;
        this.context=context;
        list_places_nearby = new ArrayList<>();
        launch_request_search_nearby_places_autocomplete(api_key, list_placeId, CALLBACK_LIST_RESTO_FRAGMENT);
    }

    private void launch_request_search_nearby_places_autocomplete(String api_key, List<String> list_places_Id, String callback){
        disposable = Observable.just(list_places_Id) // observable = list of ID's
                .flatMapIterable(ids -> ids) // for each ID of the list
                .flatMap(result -> MapsAPIstream.streamFetchgetDetailsPlaces(api_key,result)) // get details infos on this ID
                .subscribeWith(getSubscriberAutocomplete(callback));
    }

    private DisposableObserver<DetailsPlace> getSubscriberAutocomplete(String callback){
        return new DisposableObserver<DetailsPlace>() {
            @Override
            public void onNext(DetailsPlace detailsPlace) {
                build_list_places_nearby(detailsPlace);
            }

            @Override
            public void onError(Throwable e) {
                Toast toast = Toast.makeText(context,context.getResources().getString(R.string.error_get_list_restos) +"\n"
                        + e.toString(),Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            public void onComplete() {

                if(callback!=null && list_places_nearby!=null){
                    switch(callback){

                        case CALLBACK_MAPS_FRAGMENT:
                            mapsFragment.recover_list_workmates(list_places_nearby);
                            break;
                        case CALLBACK_LIST_RESTO_FRAGMENT:
                            listRestoFragment.recover_list_workmates(list_places_nearby);
                            break;
                    }
                }
            }
        };
    }

    // ------------------------------------------------------------------------------------------------
    // ------------------------------ GETTER and SETTER -----------------------------------------------
    // ------------------------------------------------------------------------------------------------

    public List<PlaceNearby> getList_places_nearby() {
        return list_places_nearby;
    }

}