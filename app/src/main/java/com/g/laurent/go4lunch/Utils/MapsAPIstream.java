package com.g.laurent.go4lunch.Utils;

import com.g.laurent.go4lunch.Utils.DetailsPlace.DetailsPlace;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Result;
import com.g.laurent.go4lunch.Utils.Search_Nearby.SearchNearby;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;


public class MapsAPIstream {

    public static Observable<SearchNearby> streamFetchgetSearchNearbyPlaces(String api_key, String type, String radius, String location){
        MapsAPIservice search_nearby_request = MapsAPIservice.retrofit.create(MapsAPIservice.class);

        return search_nearby_request.getNearbyPlaces(api_key,type,radius,location)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread());
    }

    public static Observable<DetailsPlace> streamFetchgetDetailsPlaces(String api_key, Result result){
        MapsAPIservice details_place_request = MapsAPIservice.retrofit.create(MapsAPIservice.class);

        return details_place_request.getDetailsPlaces(api_key,result.getPlaceId())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread());
    }

    public static Observable<DetailsPlace> streamFetchgetDetailsPlaces(String api_key, String placeId){
        MapsAPIservice details_place_request = MapsAPIservice.retrofit.create(MapsAPIservice.class);

        return details_place_request.getDetailsPlaces(api_key,placeId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread());
    }

}
