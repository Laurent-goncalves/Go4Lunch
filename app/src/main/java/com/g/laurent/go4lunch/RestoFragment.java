package com.g.laurent.go4lunch;

import android.app.ListActivity;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.g.laurent.go4lunch.Models.Workmates;
import com.g.laurent.go4lunch.Views.GlideApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RestoFragment extends Fragment {

    @BindView(R.id.picture_of_restaurant) ImageView picture_resto;
    @BindView(R.id.name_resto) TextView name_resto;
    @BindView(R.id.rating_restaurant_detail) LinearLayout rating_resto;
    @BindView(R.id.address_resto) TextView address_resto;
    @BindView(R.id.valid_restaurant) CircleImageView button_valid;
    @BindView(R.id.call_button) Button call_button;
    @BindView(R.id.like_button) Button like_button;
    @BindView(R.id.website_button) Button website_button;
    @BindView(R.id.list_workmates_joining_resto) RecyclerView list_workmates;
    private String placeId;
    private static String EXTRA_PLACE_ID = "placeId_resto";
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private List<String> list_workmates_id;
    private String restau_name;
    private FirebaseUser mCurrentUser;

    public RestoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_resto, container, false);
        ButterKnife.bind(this,view);
        placeId = getArguments().getString(EXTRA_PLACE_ID,null);
        launch_search_restaurant_firebase();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        return view;
    }

    private void launch_search_restaurant_firebase() {

        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("restaurants");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null) {

                    for (DataSnapshot datas : dataSnapshot.getChildren()){
                        if(getIdRestaurant(datas)!=null){
                            if(getIdRestaurant(datas).equals(placeId)) {
                                apply_picture_restaurant();
                                getNameRestaurant(datas);
                                name_resto.setText(restau_name);
                                address_resto.setText(getAddress(datas));
                                rating_calculation(datas);

                                color_buttons();
                                list_workmates_id = getWorkmatesJoining(datas);
                                setOnClickListenerButtonRestoValid(button_valid);
                                setButtonAsSelected(did_I_validate_resto(list_workmates_id),button_valid);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

    // ---------------------------------- RATING RESTO ----------------------------------------------
    private void rating_calculation(DataSnapshot datas) {

        Double rating = 0d;

        if(datas.child("rating")!=null) {
            if(datas.child("rating").getValue()!=null)
                rating = Double.parseDouble(datas.child("rating").getValue().toString());
        }

        int numStars = Math.round(rating.floatValue());
        rating_resto.removeAllViews();

        if(numStars>=1){
            for (int i = 0; i < numStars-1; i++) {
                ImageView imgView = new ImageView(getActivity().getApplicationContext());
                imgView.setImageResource(R.drawable.baseline_star_white_24);
                imgView.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(),(R.color.colorStars)));
                rating_resto.addView(imgView);
            }
        }
    }

    private String getIdRestaurant(DataSnapshot datas){
        if(datas.child("placeId")!=null)
            return (String) datas.child("placeId").getValue();
        else
            return null;
    }

    private String getAddress(DataSnapshot datas){
        if(datas.child("address")!=null)
            return (String) datas.child("address").getValue();
        else
            return null;
    }

    private void getNameRestaurant(DataSnapshot datas){
        if(datas.child("name_restaurant")!=null)
            restau_name= (String) datas.child("name_restaurant").getValue();
        else
            restau_name= null;
    }

    private List<String> getWorkmatesJoining(DataSnapshot datas){
        List<String> list_workmates=new ArrayList<>();

        if(datas.child("list_workmates")!=null){

            for(DataSnapshot datas_child : datas.child("list_workmates").getChildren()) {

                if (datas_child != null){
                    if(datas_child.getValue()!=null)
                        list_workmates.add(datas_child.getValue().toString());
                }
            }
        }

        return list_workmates;
    }

    private void color_buttons(){
        setColorPrimaryButtons(call_button);
        setColorPrimaryButtons(like_button);
        setColorPrimaryButtons(website_button);
    }

    private Boolean did_I_validate_resto(List<String> list_workmates){

        Boolean answer = false;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && list_workmates!=null) {
            // User is signed in

            // Search user id among workmates who want to lunch at this resto
            for(String id_user : list_workmates){

                if(id_user!=null){

                    if(id_user.equals(user.getUid())) {
                        answer = true;
                        break;
                    }
                }
            }
        }
        return answer;
    }

    private void apply_picture_restaurant() {

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        StorageReference storageReference = storageRef.child(placeId + ".jpg");

        // Load the image using Glide
        GlideApp.with(getActivity().getApplicationContext())
                .load(storageReference)
                .into(picture_resto);
    }

    private void setColorPrimaryButtons(Button button){

        button.setTextColor(getResources().getColor(R.color.colorIconSelected));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            button.setCompoundDrawableTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity().getApplicationContext(),(R.color.colorIconSelected))));
        else {
            Drawable[] wrapDrawable = button.getCompoundDrawables();
            DrawableCompat.setTint(wrapDrawable[0], getResources().getColor(R.color.colorIconSelected));
        }
    }

    private void setButtonAsSelected(Boolean select, CircleImageView button){

        if(getActivity().getApplicationContext()!=null){
            if(select)
                button.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(),(R.color.colorGreen)));
            else
                button.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(),(R.color.colorIconNotSelected)));
        }
    }

    private void setOnClickListenerButtonRestoValid(CircleImageView button){

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference mDatabase;
                mDatabase = FirebaseDatabase.getInstance().getReference();
                Workmates new_user;

                if(mCurrentUser!=null) {
                    // Create new user
                    new_user = new Workmates(mCurrentUser.getDisplayName(),mCurrentUser.getUid(),false, placeId, restau_name, null);
                    // create or update the new_user on Firebase in folder "workmates"
                    mDatabase.child("workmates").child(mCurrentUser.getUid()).setValue(new_user);
                    // create or update the new_user on Firebase in folder from chosen restaurant
                    mDatabase.child("restaurants").child(placeId).child("workmates_joining").child(mCurrentUser.getUid()).setValue(new_user);
                }
            }
        });
    }
}
