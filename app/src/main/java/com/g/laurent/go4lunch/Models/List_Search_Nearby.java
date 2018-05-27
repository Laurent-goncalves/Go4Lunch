package com.g.laurent.go4lunch.Models;

import android.util.Log;
import com.g.laurent.go4lunch.ListRestoFragment;
import com.g.laurent.go4lunch.MapsFragment;
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


public class List_Search_Nearby {

    private List<Place_Nearby> list_places_nearby;
    private Disposable disposable;
    private MapsFragment mapsFragment;
    private ListRestoFragment listRestoFragment;
    private final static String CALLBACK_MAPS_FRAGMENT = "callbasck_maps_fragment";
    private final static String CALLBACK_LIST_RESTO_FRAGMENT = "callbasck_list_resto_fragment";

    public List_Search_Nearby(LatLng latLng, String radius, String type, MapsFragment mapsFragment) {

        this.mapsFragment=mapsFragment;
        list_places_nearby = new ArrayList<>();
        launch_request_search_nearby_places(latLng, radius, type,CALLBACK_MAPS_FRAGMENT);
    }

    public List_Search_Nearby(LatLng latLng, String radius, String type, ListRestoFragment listRestoFragment) {

        this.listRestoFragment=listRestoFragment;
        list_places_nearby = new ArrayList<>();
        launch_request_search_nearby_places(latLng, radius, type,CALLBACK_LIST_RESTO_FRAGMENT);
    }

    public List_Search_Nearby(LatLng latLng, String radius, String type) {
        list_places_nearby = new ArrayList<>();
        launch_request_search_nearby_places(latLng, radius, type,null);
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

    private void launch_request_search_nearby_places(LatLng latLng, String radius, String type, String callback){

        String location = String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude);

        disposable = Maps_API_stream.streamFetchgetSearchNearbyPlaces(type,radius,location)
                .flatMap(userResponse -> Observable.just(userResponse.getResults()))
                .flatMapIterable(ids -> ids)
                .flatMap(Maps_API_stream::streamFetchgetDetailsPlaces)
                .subscribeWith(getSubscriber(callback));
    }

    private DisposableObserver<DetailsPlace> getSubscriber(String callback){
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
                //disposable.dispose();

                if(callback!=null){
                    switch(callback){

                        case CALLBACK_MAPS_FRAGMENT:
                            mapsFragment.recover_list_workmates(list_places_nearby);
                            break;
                        case CALLBACK_LIST_RESTO_FRAGMENT:
                            listRestoFragment.recover_list_workmates(list_places_nearby);
                            break;
                    }
                }

                Log.e("TAG","On Complete !!");
            }
        };
    }

    public List<Place_Nearby> getList_places_nearby() {
        return list_places_nearby;
    }

    public void setList_places_nearby(List<Place_Nearby> list_places_nearby) {
        this.list_places_nearby = list_places_nearby;
    }
}
