package com.g.laurent.go4lunch.Utils;

import android.content.Context;
import com.g.laurent.go4lunch.Controllers.Activities.MultiActivity;
import com.g.laurent.go4lunch.Controllers.Fragments.ListMatesFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.Models.Callback_alarm;
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
    private final static String CALLBACK_ALARM = "callback_alarm";
    private final static String RENEW_LIST_WORKMATES = "renew_list_workmates";
    private final static String INITIAL_LIST_WORKMATES = "initial_list_workmates";
    private final static String EXTRA_PLACE_ID = "placeId_resto";
    private String callback;
    private Callback_alarm callback_alarm;
    private Context context;
    private List<String> list_restos_liked;
    private String resto_id_chosen;

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
        this.context = context;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates").child(userId).child("resto_id");
    }

    public Firebase_recover(Context context, String userId) {
        FirebaseApp.initializeApp(context);
        this.context = context;
        list_restos_liked = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates").child(userId);
    }

    public Firebase_recover(Context context, Callback_alarm callback){
        FirebaseApp.initializeApp(context);
        this.callback = CALLBACK_ALARM;
        this.callback_alarm = callback;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
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
                                restoFragment.set_list_of_workmates(list_workmates,INITIAL_LIST_WORKMATES);
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
                        case CALLBACK_ALARM:
                            if(callback_alarm!=null)
                                callback_alarm.send_notification(list_workmates);
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

    public void recover_workmate_liked_restos(){

        DatabaseReference databaseReferenceListResto= databaseReferenceWorkmates.child("list_resto_liked");

        databaseReferenceListResto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                if(data!=null) {
                    for(DataSnapshot datas : data.getChildren())
                        list_restos_liked.add((String) datas.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void recover_workmate_chosen_resto(){

        DatabaseReference databaseReferenceChosenResto= databaseReferenceWorkmates.child("resto_id");

        databaseReferenceChosenResto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                if(data!=null) {
                    resto_id_chosen = (String) data.getValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
                (String) datas.child("photo_url").getValue(),
                (Boolean) datas.child("chosen").getValue(),
                (String) datas.child("resto_id").getValue(),
                (String) datas.child("resto_name").getValue(),
                (String) datas.child("resto_address").getValue(),
                (String) datas.child("resto_type").getValue(),
                list_resto_liked);

    }

    // ----------------------------------------------------------------------------------------------
    // ----------------------------------- TOOLS , GETTER AND SETTER --------------------------------
    // ----------------------------------------------------------------------------------------------


    public List<String> getList_restos_liked() {
        return list_restos_liked;
    }

    public String getResto_id_chosen() {
        return resto_id_chosen;
    }

    public void renew_list_workmates() {

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

                    restoFragment.set_list_of_workmates(list_workmates,RENEW_LIST_WORKMATES);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });

    }
}
