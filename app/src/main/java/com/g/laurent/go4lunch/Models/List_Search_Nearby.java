package com.g.laurent.go4lunch.Models;

import android.util.Log;
import com.g.laurent.go4lunch.Utils.Maps_API_stream;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Result;
import com.g.laurent.go4lunch.Utils.Search_Nearby.SearchNearby;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class List_Search_Nearby {

    private CallbackMapsActivity mCallbackMapsActivity;
    private List<Place_Nearby> list_places_nearby;

    public List_Search_Nearby(LatLng latLng, String radius, CallbackMapsActivity mCallbackMapsActivity) {

        this.mCallbackMapsActivity=mCallbackMapsActivity;
        list_places_nearby = new ArrayList<>();
        launch_request_search_nearby_places(latLng,radius);
    }

    private void build_list_places_nearby(SearchNearby searchNearby){

        if(searchNearby.getResults()!=null){

            List<Result> ListResults = searchNearby.getResults();

            for(Result result : ListResults){

                list_places_nearby.add(new Place_Nearby(result.getName(),
                        result.getPlaceId(),
                        result.getGeometry(),
                        result.getOpeningHours(),
                        result.getRating(),
                        result.getTypes(),
                        result.getVicinity(),null,null));
            }
        }
    }

    private void launch_request_search_nearby_places(LatLng latLng, String radius){

        String location = String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude);

        Disposable disposable = Maps_API_stream.streamFetchgetSearchNearbyPlaces("restaurant",radius, location).subscribeWith(new DisposableObserver<SearchNearby>() {

            @Override
            public void onNext(SearchNearby searchNearby) {
                build_list_places_nearby(searchNearby);

                if(mCallbackMapsActivity!=null)
                    mCallbackMapsActivity.update_list_nearby_places_firebase(list_places_nearby);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("eee ERROR  " + e.toString());
            }

            @Override
            public void onComplete() {
                Log.e("TAG", "On Complete !!");
            }
        });
    }

    public List<Place_Nearby> getList_places_nearby() {
        return list_places_nearby;
    }

    public void setList_places_nearby(List<Place_Nearby> list_places_nearby) {
        this.list_places_nearby = list_places_nearby;
    }
}
