package com.g.laurent.go4lunch.Utils;

import com.g.laurent.go4lunch.Utils.DetailsPlace.DetailsPlace;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Result;
import com.g.laurent.go4lunch.Utils.Search_Nearby.SearchNearby;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;


public class Maps_API_stream {

    public static Observable<SearchNearby> streamFetchgetSearchNearbyPlaces(String api_key, String type, String radius, String location){
        Maps_API_service search_nearby_request = Maps_API_service.retrofit.create(Maps_API_service.class);

        return search_nearby_request.getNearbyPlaces(api_key,type,radius,location)
                .subscribeOn(Schedulers.io())
                //.observeOn(AndroidSchedulers.mainThread());
                .observeOn(Schedulers.newThread());  // TEST
    }

    public static Observable<DetailsPlace> streamFetchgetDetailsPlaces(String api_key, Result result){
        Maps_API_service details_place_request = Maps_API_service.retrofit.create(Maps_API_service.class);

        return details_place_request.getDetailsPlaces(api_key,result.getPlaceId())
                .subscribeOn(Schedulers.io())
                //.observeOn(AndroidSchedulers.mainThread());
                .observeOn(Schedulers.newThread());  // TEST
    }

    public static Observable<DetailsPlace> streamFetchgetDetailsPlaces(String api_key, String placeId){
        Maps_API_service details_place_request = Maps_API_service.retrofit.create(Maps_API_service.class);

        return details_place_request.getDetailsPlaces(api_key,placeId)
                .subscribeOn(Schedulers.io())
                //.observeOn(AndroidSchedulers.mainThread());
                .observeOn(Schedulers.newThread());  // TEST
    }

}
