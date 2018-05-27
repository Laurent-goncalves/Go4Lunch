package com.g.laurent.go4lunch.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import com.g.laurent.go4lunch.Models.Callback_resto_fb;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.RestoFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class Firebase_update implements GoogleApiClient.OnConnectionFailedListener {

    public Workmate workmate;
    private Context context;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceWorkmates;
    private RestoFragment restoFragment;

    public Firebase_update(Context context, RestoFragment restoFragment) {
        this.context=context;
        this.restoFragment=restoFragment;
        FirebaseApp.initializeApp(context);
        databaseReference= FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
    }

    public Firebase_update(Context context) {
        this.context=context;
        FirebaseApp.initializeApp(context);
        databaseReference= FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
    }

    // ------------------------------------------------------------------------------------------------
    // -------------------------------- UPDATE DATAS ON WORKMATES -------------------------------------
    // ------------------------------------------------------------------------------------------------

    public void update_full_workmate_data(Workmate workmate){
        databaseReferenceWorkmates.child(workmate.getId()).setValue(workmate);
    }

    public void update_like_status_workmates(String user_id, Place_Nearby resto){
        databaseReferenceWorkmates.child(user_id).child("list_resto_liked").child(resto.getPlaceId()).setValue(resto.getPlaceId());

        if(restoFragment!=null)
            restoFragment.modify_state_button_like();
    }

    public void update_chosen_status_workmate(String user_id, Place_Nearby resto){
        databaseReferenceWorkmates.child(user_id).child("resto_id").setValue(resto.getPlaceId());
        databaseReferenceWorkmates.child(user_id).child("resto_name").setValue(resto.getName_restaurant());
        databaseReferenceWorkmates.child(user_id).child("resto_type").setValue(resto.getTypes().get(0));
        databaseReferenceWorkmates.child(user_id).child("chosen").setValue(true);

        if(restoFragment!=null)
            restoFragment.modify_state_button_choose();
    }

    public void create_new_user_firebase(FirebaseUser mCurrentUser){
        databaseReferenceWorkmates.child(mCurrentUser.getUid()).child("name").setValue(mCurrentUser.getDisplayName());
        databaseReferenceWorkmates.child(mCurrentUser.getUid()).child("id").setValue(mCurrentUser.getUid());
       // databaseReferenceWorkmates.child(mCurrentUser.getUid()).child("photoUrl").setValue(mCurrentUser.getPhotoUrl());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
