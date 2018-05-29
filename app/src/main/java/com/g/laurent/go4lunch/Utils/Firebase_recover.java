package com.g.laurent.go4lunch.Utils;

import android.content.Context;
import android.provider.ContactsContract;

import com.g.laurent.go4lunch.Controllers.Activities.MultiActivity;
import com.g.laurent.go4lunch.Controllers.Fragments.ListMatesFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.Controllers.Fragments.RestoFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;


public class Firebase_recover {

    private List<Workmate> list_workmates;
    private DatabaseReference databaseReferenceWorkmates;
    private ListRestoFragment listRestoFragment;
    private MapsFragment mapsFragment;
    private RestoFragment restoFragment;
    private ListMatesFragment listMatesFragment;
    private MultiActivity multiActivity;
    private final static String CALLBACK_RESTOFRAGMENT = "callback_restofragment";
    private final static String CALLBACK_LISTRESTOFRAGMENT = "callback_listrestofragment";
    private final static String CALLBACK_LISTMATESFRAGMENT = "callback_listmatesfragment";
    private final static String CALLBACK_MAPSFRAGMENT = "callback_mapsfragment";
    private String callback;

    public Firebase_recover(Context context, ListRestoFragment listRestoFragment) {
        FirebaseApp.initializeApp(context);
        this.listRestoFragment=listRestoFragment;
        this.callback = CALLBACK_LISTRESTOFRAGMENT;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
    }

    public Firebase_recover(Context context, MapsFragment mapsFragment) {
        FirebaseApp.initializeApp(context);
        this.mapsFragment=mapsFragment;
        this.callback = CALLBACK_MAPSFRAGMENT;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
    }

    public Firebase_recover(Context context, RestoFragment restoFragment) {
        FirebaseApp.initializeApp(context);
        this.restoFragment=restoFragment;
        this.callback = CALLBACK_RESTOFRAGMENT;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
    }

    public Firebase_recover(Context context, ListMatesFragment listMatesFragment) {
        FirebaseApp.initializeApp(context);
        this.listMatesFragment=listMatesFragment;
        this.callback = CALLBACK_LISTMATESFRAGMENT;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
    }

    public Firebase_recover(Context context, MultiActivity multiActivity, String userId) {
        FirebaseApp.initializeApp(context);
        this.multiActivity=multiActivity;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates").child(userId).child("resto_id");
    }

    // ----------------------------------------------------------------------------------------------
    // ------------------------------- RECOVER WORKMATES --------------------------------------------
    // ----------------------------------------------------------------------------------------------

    public void recover_list_workmates(){

        databaseReferenceWorkmates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot datas) {
                if(datas!=null) {
                    list_workmates = new ArrayList<>();

                    for(DataSnapshot id : datas.getChildren()){

                        Workmate workmate = create_workmate_with_firebase_datas(id);

                        if(!is_workmate_in_list(workmate.getId(),list_workmates))
                            list_workmates.add(workmate);
                    }

                    switch(callback){
                        case CALLBACK_RESTOFRAGMENT:
                            if(restoFragment!=null)
                                restoFragment.set_list_of_workmates(list_workmates);
                            break;
                        case CALLBACK_LISTRESTOFRAGMENT:
                            if(listRestoFragment!=null)
                                listRestoFragment.set_list_of_workmates(list_workmates);
                            break;
                        case CALLBACK_LISTMATESFRAGMENT:
                            if(listMatesFragment!=null)
                                listMatesFragment.set_list_of_workmates(list_workmates);
                            break;
                        case CALLBACK_MAPSFRAGMENT:
                            if(mapsFragment!=null)
                                mapsFragment.set_list_of_workmates(list_workmates);
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

    public void recover_workmate_restoId(String userId){

        databaseReferenceWorkmates= databaseReferenceWorkmates.child(userId).child("resto_id");

        databaseReferenceWorkmates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                if(data!=null) {
                    listRestoFragment.setPlaceId((String) data.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

    public void recover_workmate_like(String userId, String restoId_liked){

        databaseReferenceWorkmates= databaseReferenceWorkmates.child(userId).child("list_resto_liked");

        databaseReferenceWorkmates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                if(data!=null) {

                    for(DataSnapshot datas : data.getChildren()){


                    }
                    listRestoFragment.setPlaceId((String) data.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

    public void show_lunch_current_user(){

        databaseReferenceWorkmates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                if(data!=null) {
                    if(multiActivity!=null)
                        multiActivity.configure_and_show_restofragment((String) data.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

    private Boolean is_workmate_in_list(String user_id, List<Workmate> list_workmates){

        for(Workmate workmate : list_workmates){
            if(workmate!=null){
                if(workmate.getId()!=null) {
                    if (workmate.getId().equals(user_id))
                        return true;
                }
            }
        }
        return false;
    }

    // ----------------------------------------------------------------------------------------------
    // ------------------------------- RECOVER DATAS FROM 1 WORKMATE --------------------------------
    // ----------------------------------------------------------------------------------------------

    private Workmate create_workmate_with_firebase_datas(DataSnapshot datas){

        // Search id of user in firebase workmates list

        List<String> list_resto_liked = new ArrayList<>();

        if (datas.child("list_resto_liked") != null) {
            for (DataSnapshot datas_child : datas.child("list_resto_liked").getChildren())
                list_resto_liked.add((String) datas_child.getValue());
        }

        return new Workmate(
                (String) datas.child("name").getValue(),
                (String) datas.child("id").getValue(),
                (String) datas.child("photoUrl").getValue(),
                (Boolean) datas.child("chosen").getValue(),
                (String) datas.child("resto_id").getValue(),
                (String) datas.child("resto_name").getValue(),
                (String) datas.child("resto_type").getValue(),
                list_resto_liked);

    }

    // ----------------------------------------------------------------------------------------------
    // ----------------------------------- TOOLS , GETTER AND SETTER --------------------------------
    // ----------------------------------------------------------------------------------------------

}