package com.g.laurent.go4lunch.Models;

import android.util.Log;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.Utils.DetailsPlace.DetailsPlace;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Photo;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Result;
import com.g.laurent.go4lunch.Utils.Maps_API_stream;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class List_Search_Nearby implements Disposable {

    private List<Place_Nearby> list_places_nearby;
    private Disposable disposable;
    private MapsFragment mapsFragment;
    private ListRestoFragment listRestoFragment;
    private final static String CALLBACK_MAPS_FRAGMENT = "callbasck_maps_fragment";
    private final static String CALLBACK_LIST_RESTO_FRAGMENT = "callbasck_list_resto_fragment";


    public List_Search_Nearby(String api_key, LatLng latLng, String radius, String type, CallbackMultiActivity callbackMultiActivity) {
        list_places_nearby = new ArrayList<>();
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

                // Create Place_Nearby
                list_places_nearby.add(new Place_Nearby(result.getName(),
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

        disposable = Maps_API_stream.streamFetchgetSearchNearbyPlaces(api_key,type,radius,location)
                .flatMap(userResponse -> Observable.just(userResponse.getResults()))
                .flatMapIterable(ids -> ids)
                .flatMap(result -> Maps_API_stream.streamFetchgetDetailsPlaces(api_key,result))
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
                Log.e("TAG","On Error"+Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {

                if(callbackMultiActivity!=null)
                    callbackMultiActivity.configureViewPagerAndTabs(list_places_nearby);

                Log.e("TAG","On Complete !!");
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

    public List_Search_Nearby(String api_key, List<String> list_placeId, MapsFragment mapsFragment) {
        // CONSTRUCTOR FOR AUTOCOMPLETE METHOD
        this.mapsFragment=mapsFragment;
        list_places_nearby = new ArrayList<>();
        launch_request_search_nearby_places_autocomplete(api_key, list_placeId, CALLBACK_MAPS_FRAGMENT);
    }

    public List_Search_Nearby(String api_key, List<String> list_placeId, ListRestoFragment listRestoFragment) {
        // CONSTRUCTOR FOR AUTOCOMPLETE METHOD
        this.listRestoFragment=listRestoFragment;
        list_places_nearby = new ArrayList<>();
        launch_request_search_nearby_places_autocomplete(api_key, list_placeId, CALLBACK_LIST_RESTO_FRAGMENT);
    }

    private void launch_request_search_nearby_places_autocomplete(String api_key, List<String> list_places_Id, String callback){

        disposable = Observable.just(list_places_Id)
                .flatMapIterable(ids -> ids)
                .flatMap(result -> Maps_API_stream.streamFetchgetDetailsPlaces(api_key,result))
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
                Log.e("TAG","On Error"+Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {

                if(callback!=null && list_places_nearby!=null){
                    switch(callback){

                        case CALLBACK_MAPS_FRAGMENT:
                            System.out.println("eee  recover workmates mapsFragment   " + list_places_nearby.get(0).getName_restaurant());
                            mapsFragment.recover_list_workmates(list_places_nearby);
                            break;
                        case CALLBACK_LIST_RESTO_FRAGMENT:
                            System.out.println("eee  recover workmates listRestoFragment   " + list_places_nearby.get(0).getName_restaurant());
                            listRestoFragment.recover_list_workmates(list_places_nearby);
                            break;
                    }
                }

                Log.e("TAG","On Complete !!");
            }
        };
    }

    // ------------------------------------------------------------------------------------------------
    // ------------------------------ GETTER and SETTER -----------------------------------------------
    // ------------------------------------------------------------------------------------------------

    public List<Place_Nearby> getList_places_nearby() {
        return list_places_nearby;
    }

}