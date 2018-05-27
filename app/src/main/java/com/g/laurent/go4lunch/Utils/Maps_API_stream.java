package com.g.laurent.go4lunch.Utils;

import com.g.laurent.go4lunch.Utils.DetailsPlace.DetailsPlace;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Result;
import com.g.laurent.go4lunch.Utils.Search_Nearby.SearchNearby;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class Maps_API_stream {

    public static Observable<SearchNearby> streamFetchgetSearchNearbyPlaces(String type, String radius, String location){
        Maps_API_service search_nearby_request = Maps_API_service.retrofit.create(Maps_API_service.class);

        return search_nearby_request.getNearbyPlaces("AIzaSyCAzX1ILkJlqSsTMkRJHSGEMAQWuqxSxKA",type,radius,location)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<DetailsPlace> streamFetchgetDetailsPlaces(Result result){
        Maps_API_service details_place_request = Maps_API_service.retrofit.create(Maps_API_service.class);

        return details_place_request.getDetailsPlaces("AIzaSyCAzX1ILkJlqSsTMkRJHSGEMAQWuqxSxKA",result.getPlaceId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<DetailsPlace> streamFetchgetDetailsPlaces(String placeId){
        Maps_API_service details_place_request = Maps_API_service.retrofit.create(Maps_API_service.class);

        return details_place_request.getDetailsPlaces("AIzaSyCAzX1ILkJlqSsTMkRJHSGEMAQWuqxSxKA",placeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

   /* public static Observable<String> streamFetchgetPhotoPlaces(Result result){
        Maps_API_service photo_place_request = Maps_API_service.retrofit.create(Maps_API_service.class);

        if(result!=null){
            if(result.getPhotos()!=null){
                if(result.getPhotos().get(0)!=null){
                    if(result.getPhotos().get(0).getPhotoReference()!=null){

                        return photo_place_request.getPhotoPlaces("AIzaSyCAzX1ILkJlqSsTMkRJHSGEMAQWuqxSxKA",result.getReference(), "200");
                    }
                }
            }
        }


        return null;
    }*/
}
/*

                .subscribeOn(Schedulers.io())
                // .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.newThread())  // TEST
                .timeout(20, TimeUnit.SECONDS);

 */