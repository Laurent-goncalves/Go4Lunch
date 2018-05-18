package com.g.laurent.go4lunch;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.g.laurent.go4lunch.Models.Workmates;
import com.g.laurent.go4lunch.Views.GlideApp;
import com.g.laurent.go4lunch.Views.Resto_Details.WorkmatesViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RestoFragment extends BaseRestoFragment {

    @BindView(R.id.picture_of_restaurant) ImageView picture_resto;
    @BindView(R.id.name_resto) TextView name_resto;
    @BindView(R.id.rating_restaurant_detail) LinearLayout rating_resto;
    @BindView(R.id.address_resto) TextView address_resto;
    @BindView(R.id.valid_restaurant) CircleImageView button_valid;
    @BindView(R.id.call_button) Button call_button;
    @BindView(R.id.like_button) Button like_button;
    @BindView(R.id.website_button) Button website_button;
    @BindView(R.id.list_workmates_joining_resto) RecyclerView list_workmates_recycler;
    private final static String TYPE_DISPLAY_WORKMATES_BY_RESTO = "list_of_workmates_by_resto";
    private final static String EXTRA_PLACE_ID = "placeId_resto";
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseUser mCurrentUser;
    private WorkmatesViewAdapter adapter;

    public RestoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and associate the views
        View view = inflater.inflate(R.layout.fragment_resto, container, false);
        ButterKnife.bind(this,view);

        // Initialize variables
        placeId = getArguments().getString(EXTRA_PLACE_ID,null);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Launch the search for restaurant details on Firebase storage
        launch_search_restaurant_firebase();
        return view;
    }

    private void launch_search_restaurant_firebase() {

        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("restaurants").child(placeId);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot datas) {
                if(datas!=null) {

                    resto = create_place_nearby_from_datas_firebase(datas);

                    // Create views
                    if(resto!=null){

                        apply_picture_restaurant();
                        name_resto.setText(resto.getName_restaurant());
                        address_resto.setText(resto.getAddress());
                        getRating(datas);
                        color_buttons();
                        setOnClickListenerButtonRestoValid(button_valid);
                        setButtonAsSelected(did_I_validate_resto(list_workmates),button_valid);
                        configure_recycler_view();
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

    @Override
    protected void getRating(DataSnapshot datas) {

        int numStars;

        if(rating!=null)
            numStars = Math.round(rating.floatValue());
        else
            numStars = 0;

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

    private void color_buttons(){
        setColorPrimaryButtons(call_button);
        setColorPrimaryButtons(like_button);
        setColorPrimaryButtons(website_button);
    }

    private Boolean did_I_validate_resto(List<Workmates> list_workmates){

        Boolean answer = false;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && list_workmates!=null) {
            // User is signed in

            // Search user id among workmates who want to lunch at this resto
            for(Workmates workmates : list_workmates){

                if(workmates!=null){
                    if(workmates.getId()!=null){
                        if(workmates.getId().equals(user.getUid())) {
                            answer = true;
                            break;
                        }
                    }
                }
            }
        }

        return answer;
    }

    private void apply_picture_restaurant() {

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
                    new_user = create_workmates(mCurrentUser);
                    // create or update the new_user on Firebase in folder "workmates"
                    mDatabase.child("workmates").child(mCurrentUser.getUid()).setValue(new_user);
                    // create or update the new_user on Firebase in folder from chosen restaurant
                    mDatabase.child("restaurants").child(placeId).child("workmates_joining").child(mCurrentUser.getUid()).setValue(new_user);
                    setButtonAsSelected(true, button_valid);
                }
            }
        });
    }

    private Workmates create_workmates(FirebaseUser mCurrentUser){

        String photoUrl = null;

        if(mCurrentUser.getPhotoUrl()!=null)
            photoUrl=mCurrentUser.getPhotoUrl().toString();

        return new Workmates(mCurrentUser.getDisplayName(),mCurrentUser.getUid(),photoUrl,true, placeId, resto.getName_restaurant(), "bar");
    }

    private void configure_recycler_view(){

        if(adapter == null) {
            // Create adapter passing in the sample user data
            adapter = new WorkmatesViewAdapter(getActivity().getApplicationContext(),list_workmates,TYPE_DISPLAY_WORKMATES_BY_RESTO);
            // Attach the adapter to the recyclerview to populate items
            list_workmates_recycler.setAdapter(adapter);
            // Set layout manager to position the items
            list_workmates_recycler.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        } else
            adapter.notifyDataSetChanged();

    }
}
