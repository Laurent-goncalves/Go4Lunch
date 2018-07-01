package com.g.laurent.go4lunch.Utils;

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

public interface MapsAPIservice {

    // NEARBY SEARCH REQUEST
    @GET("nearbysearch/json")
    Observable<SearchNearby> getNearbyPlaces(@Query("key") String api,
                                             @Query("type") String type,
                                             @Query("radius") String radius,
                                             @Query("location") String location);

    // PLACE DETAILS REQUEST
    @GET("details/json")
    Observable<DetailsPlace> getDetailsPlaces(@Query("key") String api,
                                              @Query("placeid") String placeId);

    HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(logging).retryOnConnectionFailure(false);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient.build())
            .build();
}
