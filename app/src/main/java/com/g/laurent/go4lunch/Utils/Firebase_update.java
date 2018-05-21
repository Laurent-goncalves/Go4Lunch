package com.g.laurent.go4lunch.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.g.laurent.go4lunch.ListRestoFragment;
import com.g.laurent.go4lunch.MapsFragment;
import com.g.laurent.go4lunch.Models.Callback_DetailResto;
import com.g.laurent.go4lunch.Models.Callback_resto_fb;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmates;
import com.g.laurent.go4lunch.RestoFragment;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Geometry;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Location;
import com.g.laurent.go4lunch.Utils.Search_Nearby.OpeningHours;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Firebase_update implements GoogleApiClient.OnConnectionFailedListener, Callback_resto_fb {

    public Workmates workmate;
    private Context context;

    private List<Place_Nearby> list_restos;
    private int count_id;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceWorkmates;
    private DatabaseReference databaseReferenceRestos;
    private RestoFragment restoFragment;
    private String user_id;
    private String placeId;

    public Firebase_update(Context context, RestoFragment restoFragment) {
        this.context=context;
        list_restos = new ArrayList<>();

        FirebaseApp.initializeApp(context);
        storage = FirebaseStorage.getInstance();
        storageRef= storage.getReference();

        this.restoFragment=restoFragment;

        databaseReference= FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
        databaseReferenceRestos= databaseReference.child("restaurants");
    }

    // ----------------------------------------------------------------------------------------------
    // -------------------------------- UPDATE DATAS ON WORKMATE-------------------------------------
    // ----------------------------------------------------------------------------------------------

    public void update_full_workmate_data(Workmates workmate){
        databaseReferenceWorkmates.child(workmate.getId()).setValue(workmate);
    }

    public void update_like_status_workmates(String user_id, String placeId){
        databaseReferenceWorkmates.child(user_id).child("list_resto_liked").child(placeId).setValue(placeId);

        if(restoFragment!=null)
            restoFragment.modify_state_button_like();
    }

    public Workmates create_new_workmate_on_firebase(FirebaseUser mCurrentUser){

        String photoUrl = null;

        if(mCurrentUser.getPhotoUrl()!=null)
            photoUrl=mCurrentUser.getPhotoUrl().toString();

        return new Workmates(mCurrentUser.getDisplayName(),mCurrentUser.getUid(),photoUrl,null, null, null, null,null);
    }

    // ----------------------------------------------------------------------------------------------
    // -------------------------------   UPDATE DATAS ON RESTO  -------------------------------------
    // ----------------------------------------------------------------------------------------------

    private void update_list_restos_on_firebase(List<Place_Nearby> list_search_nearby){

        databaseReferenceRestos.removeValue();
        list_restos=list_search_nearby;
        count_id = 0;

        if(list_search_nearby!=null) {
            for(Place_Nearby place : list_search_nearby) {
                // Add resto to firebase
                databaseReferenceRestos.child(place.getPlaceId()).setValue(place);

                // Add picture of the resto to firebase
                getPlacePhotosAsync(place.getPlaceId());
            }
        }
    }

    private void getPlacePhotosAsync(final String placeId) {

        final Google_Maps_Utils google_maps_utils = new Google_Maps_Utils(context);

        Places.GeoDataApi.getPlacePhotos(google_maps_utils.getGoogleApiClient(), placeId)
                .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {

                    @Override
                    public void onResult(PlacePhotoMetadataResult photos) {
                        if (!photos.getStatus().isSuccess())
                            return;

                        PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                        if (photoMetadataBuffer.getCount() > 0) {
                            photoMetadataBuffer.get(0)
                                    .getScaledPhoto(google_maps_utils.getGoogleApiClient(), 100,100)
                                    .setResultCallback(mDisplayPhotoResultCallback);
                        }
                        photoMetadataBuffer.release();
                    }
                });
    }

    private ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback
            = new ResultCallback<PlacePhotoResult>() {

        @Override
        public void onResult(PlacePhotoResult placePhotoResult) {
            if (!placePhotoResult.getStatus().isSuccess()) {
                return;
            }

            // if success, store the bitmap picture on Firebase storage
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            placePhotoResult.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);

            byte[] data = baos.toByteArray();

            // Create a reference
            String placeId = list_restos.get(count_id).getPlaceId();
            StorageReference mountainsRef = storageRef.child(placeId + ".jpg");
            count_id++;

            UploadTask uploadTask = mountainsRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
        }
    };

    public void update_chosen_status_resto(String user_id, Place_Nearby resto){

        this.user_id = user_id;
        this.placeId = resto.getPlaceId();
        Callback_resto_fb callback = this;
        Firebase_recover firebase_recover = new Firebase_recover(context,null,null,null,null);
        firebase_recover.recover_list_restos(null,callback);

        update_chosen_status_workmate(user_id, resto);
        restoFragment.modify_state_button_choose();
    }

    private void update_chosen_status_workmate(String user_id, Place_Nearby resto){
        databaseReferenceWorkmates.child(user_id).child("resto_id").setValue(resto.getPlaceId());
        databaseReferenceWorkmates.child(user_id).child("resto_name").setValue(resto.getName_restaurant());
        databaseReferenceWorkmates.child(user_id).child("resto_type").setValue(resto.getTypes().get(0));
        databaseReferenceWorkmates.child(user_id).child("chosen").setValue(true);
    }

    @Override
    public void update_chosen_list_restos(List<Place_Nearby> list_restos) {

        for(Place_Nearby resto : list_restos) {

            if(resto!=null){
                if(resto.getPlaceId()!=null){
                    if(resto.getPlaceId().equals(placeId)){ // if restaurant chosen
                        databaseReferenceRestos.child(placeId).child("mWorkmatesList").child(user_id).setValue(user_id); // add the user_id in the list
                    } else { // if other restaurant
                        List<Workmates> list_workmates = resto.getWorkmatesList();  // Recover list of workmates joining this resto
                        if (is_workmate_in_list(user_id, list_workmates)) {         // If the user is in the list, ...
                            databaseReferenceRestos.child(placeId).child("mWorkmatesList").child(user_id).removeValue(); // ... remove it
                            break;
                        }
                    }
                }
            }
        }

    }

    private Boolean is_workmate_in_list(String user_id, List<Workmates> list_workmates){

        for(Workmates workmate : list_workmates){
            if(workmate!=null){
                if(workmate.getId().equals(user_id))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
