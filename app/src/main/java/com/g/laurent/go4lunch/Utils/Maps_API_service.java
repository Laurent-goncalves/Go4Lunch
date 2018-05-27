package com.g.laurent.go4lunch.Utils;

import android.graphics.Bitmap;

import com.g.laurent.go4lunch.Utils.DetailsPlace.DetailsPlace;
import com.g.laurent.go4lunch.Utils.Search_Nearby.SearchNearby;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Maps_API_service {


//https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=48.6102599,2.474805&radius=500&type=restaurant&key=AIzaSyCAzX1ILkJlqSsTMkRJHSGEMAQWuqxSxKA

    // NEARBY SEARCH REQUEST
    @GET("nearbysearch/json")
    Observable<SearchNearby> getNearbyPlaces(@Query("key") String api,
                                             @Query("type") String type,
                                             @Query("radius") String radius,
                                             @Query("location") String location);


    // PLACE DETAILS REQUEST       https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJ64SRn-xv5kcRH7AlSsBFASQ&key=AIzaSyCAzX1ILkJlqSsTMkRJHSGEMAQWuqxSxKA
    @GET("details/json")
    Observable<DetailsPlace> getDetailsPlaces(@Query("key") String api,
                                              @Query("placeid") String placeId);

    // PLACE PHOTO REQUEST        https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=CmRaAAAATkFTQVJ2nDf5ugKfbLjyt-KiYHwuOW26pN6jcEMrRejd8O-fWaP7VFBpPtsiRmKQWfb-1jsEyQacR8XiDj9XjJZW00WqP3hDfu1b_CO01p57lQjPugHWg8eBbDvxnjTrEhCfiJb1_7mAE8543DJHblOHGhTUfrEIy4dV7SIH7flx4WGthM4mBw&key=AIzaSyCAzX1ILkJlqSsTMkRJHSGEMAQWuqxSxKA
    @GET("photo")
    Observable<String> getPhotoPlaces(@Query("key") String api,
                                      @Query("photoreference") String photoreference,
                                      @Query("maxheight") String maxheight);



    HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(logging).retryOnConnectionFailure(false);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient.build())
            .build();




}
