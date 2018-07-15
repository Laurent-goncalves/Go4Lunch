package com.g.laurent.go4lunch.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;
import com.g.laurent.go4lunch.Controllers.Fragments.ListMatesFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.Models.Callback_alarm;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.Controllers.Fragments.RestoFragment;
import com.g.laurent.go4lunch.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;


public class FirebaseRecover {

    private List<Workmate> list_workmates;
    private DatabaseReference databaseReferenceWorkmates;
    private ListRestoFragment listRestoFragment;
    private MapsFragment mapsFragment;
    private RestoFragment restoFragment;
    private ListMatesFragment listMatesFragment;
    private final static String CALLBACK_RESTOFRAGMENT = "callback_restofragment";
    private final static String CALLBACK_LISTRESTOFRAGMENT = "callback_listrestofragment";
    private final static String CALLBACK_LISTMATESFRAGMENT = "callback_listmatesfragment";
    private final static String CALLBACK_MAPSFRAGMENT = "callback_mapsfragment";
    private final static String CALLBACK_ALARM = "callback_alarm";
    private final static String RENEW_LIST_WORKMATES = "renew_list_workmates";
    private final static String INITIAL_LIST_WORKMATES = "initial_list_workmates";
    private String callback;
    private Context context;
    private Callback_alarm callback_alarm;


    public FirebaseRecover(Context context, ListRestoFragment listRestoFragment) {
        FirebaseApp.initializeApp(context);
        this.context=context;
        this.listRestoFragment=listRestoFragment;
        this.callback = CALLBACK_LISTRESTOFRAGMENT;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
    }

    public FirebaseRecover(Context context, MapsFragment mapsFragment) {
        FirebaseApp.initializeApp(context);
        this.context=context;
        this.mapsFragment=mapsFragment;
        this.callback = CALLBACK_MAPSFRAGMENT;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
    }

    public FirebaseRecover(Context context, RestoFragment restoFragment) {
        FirebaseApp.initializeApp(context);
        this.context=context;
        this.restoFragment=restoFragment;
        this.callback = CALLBACK_RESTOFRAGMENT;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
    }

    public FirebaseRecover(Context context, ListMatesFragment listMatesFragment) {
        FirebaseApp.initializeApp(context);
        this.context=context;
        this.listMatesFragment=listMatesFragment;
        this.callback = CALLBACK_LISTMATESFRAGMENT;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
    }

    public FirebaseRecover(Context context, Callback_alarm callback){
        FirebaseApp.initializeApp(context);
        this.context=context;
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
            public void onDataChange(@NonNull DataSnapshot datas) {
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast toast = Toast.makeText(context,context.getResources().getString(R.string.firebase_database_err) + "\n"
                        + databaseError.toString(),Toast.LENGTH_LONG);
                toast.show();
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

        List<String> list_resto_liked = new ArrayList<>();

        for (DataSnapshot datas_child : datas.child("list_resto_liked").getChildren())
            list_resto_liked.add((String) datas_child.getValue());

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
    // ----------------------------------- RENEW LIST WORKMATES -------------------------------------
    // ----------------------------------------------------------------------------------------------

    public void renew_list_workmates() {

        databaseReferenceWorkmates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datas) {
                list_workmates = new ArrayList<>();

                for(DataSnapshot id : datas.getChildren()){

                    Workmate workmate = create_workmate_with_firebase_datas(id);

                    if(!is_workmate_in_list(workmate.getId(),list_workmates))
                        list_workmates.add(workmate);
                }

                restoFragment.set_list_of_workmates(list_workmates,RENEW_LIST_WORKMATES);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast toast = Toast.makeText(context,context.getResources().getString(R.string.firebase_database_err) + "\n"
                        + databaseError.toString(),Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
}