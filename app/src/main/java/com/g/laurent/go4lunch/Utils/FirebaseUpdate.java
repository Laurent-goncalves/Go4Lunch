package com.g.laurent.go4lunch.Utils;

import android.content.Context;
import com.g.laurent.go4lunch.Models.PlaceNearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.Controllers.Fragments.RestoFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUpdate {

    public Workmate workmate;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceWorkmates;
    private RestoFragment restoFragment;

    public FirebaseUpdate(Context context, RestoFragment restoFragment) {
        this.restoFragment=restoFragment;
        FirebaseApp.initializeApp(context);
        databaseReference= FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
    }

    public FirebaseUpdate(Context context) {
        FirebaseApp.initializeApp(context);
        databaseReference= FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
    }

    // ------------------------------------------------------------------------------------------------
    // -------------------------------- UPDATE DATAS ON WORKMATES -------------------------------------
    // ------------------------------------------------------------------------------------------------

    public void update_like_status_workmates(String user_id, PlaceNearby resto){
        databaseReferenceWorkmates.child(user_id).child("list_resto_liked").child(resto.getPlaceId()).setValue(resto.getPlaceId());

        if(restoFragment!=null)
            restoFragment.modify_state_button_like();
    }

    public void update_chosen_status_workmate(String user_id, PlaceNearby resto){
        databaseReferenceWorkmates.child(user_id).child("resto_id").setValue(resto.getPlaceId());
        databaseReferenceWorkmates.child(user_id).child("resto_name").setValue(resto.getName_restaurant());
        databaseReferenceWorkmates.child(user_id).child("resto_address").setValue(resto.getAddress());
        databaseReferenceWorkmates.child(user_id).child("resto_type").setValue(resto.getTypes().get(0));
        databaseReferenceWorkmates.child(user_id).child("chosen").setValue(true);

        if(restoFragment!=null)
            restoFragment.modify_state_button_choose();
    }

    public void create_new_user_firebase(FirebaseUser mCurrentUser){
        databaseReferenceWorkmates.child(mCurrentUser.getUid()).child("name").setValue(mCurrentUser.getDisplayName());
        databaseReferenceWorkmates.child(mCurrentUser.getUid()).child("id").setValue(mCurrentUser.getUid());

        if(mCurrentUser.getPhotoUrl()!=null)
            databaseReferenceWorkmates.child(mCurrentUser.getUid()).child("photo_url").setValue(mCurrentUser.getPhotoUrl().toString());
    }

    public void initialize_chosen_restaurant(String userId){
        databaseReferenceWorkmates.child(userId).child("resto_id").removeValue();
        databaseReferenceWorkmates.child(userId).child("resto_address").removeValue();
        databaseReferenceWorkmates.child(userId).child("resto_name").removeValue();
        databaseReferenceWorkmates.child(userId).child("resto_type").removeValue();
        databaseReferenceWorkmates.child(userId).child("chosen").setValue(false);
    }

    public void initialize_like_list_restaurant(String userId){
        databaseReferenceWorkmates.child(userId).child("list_resto_liked").removeValue();
    }
}
