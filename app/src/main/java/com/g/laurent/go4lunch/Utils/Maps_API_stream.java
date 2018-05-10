package com.g.laurent.go4lunch.Utils;

import com.g.laurent.go4lunch.Utils.Search_Nearby.SearchNearby;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class Maps_API_stream {

    public static Observable<SearchNearby> streamFetchgetSearchNearbyPlaces(String type, String radius, String location){
        Maps_API_service search_nearby_request = Maps_API_service.retrofit.create(Maps_API_service.class);

        return search_nearby_request.getNearbyPlaces("AIzaSyCAzX1ILkJlqSsTMkRJHSGEMAQWuqxSxKA",type,radius,location)
                .subscribeOn(Schedulers.io())
                // .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.newThread())  // TEST
                .timeout(20, TimeUnit.SECONDS);

    }
}
