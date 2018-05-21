package com.g.laurent.go4lunch;

import android.app.Activity;
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
import com.g.laurent.go4lunch.Models.Callback_resto_fb;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmates;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Utils.Firebase_update;
import com.g.laurent.go4lunch.Views.GlideApp;
import com.g.laurent.go4lunch.Views.Resto_Details.WorkmatesViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private WorkmatesViewAdapter adapter;
    private Place_Nearby resto;
    private Workmates current_user;
    private String placeId;
    private Firebase_recover firebase_recover;
    private Firebase_update firebase_update;
    //private int PLACE_PICKER_REQUEST = 1;

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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        firebase_recover = new Firebase_recover(getActivity().getApplicationContext(),null,null,this,null);
        firebase_update = new Firebase_update(getActivity().getApplicationContext(),this);

        // Launch the search for restaurant details on Firebase storage
        firebase_recover.recover_resto_on_firebase(placeId);

        // Launch the search for user data on firebase
        if (currentUser != null)
            firebase_recover.recover_workmate_on_firebase(currentUser.getUid());

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback_resto_fb = (Callback_resto_fb) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callback_DetailResto");
        }
    }

    private void configure_recycler_view(){

        if(adapter == null) {
            // Create adapter passing in the sample user data
            adapter = new WorkmatesViewAdapter(getActivity().getApplicationContext(),resto.getWorkmatesList(),TYPE_DISPLAY_WORKMATES_BY_RESTO);
            // Attach the adapter to the recyclerview to populate items
            list_workmates_recycler.setAdapter(adapter);
            // Set layout manager to position the items
            list_workmates_recycler.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        } else
            adapter.notifyDataSetChanged();

    }

    public void configure_views_with_resto(Place_Nearby resto){

        this.resto=resto;

        // Create views
        if(resto!=null){
            apply_picture_restaurant();                         // PICTURE RESTO

            if(resto.getName_restaurant().length()>=15) {
                String name = resto.getName_restaurant().substring(0, 15) + "...";
                name_resto.setText(name); // NAME RESTO
            } else
                name_resto.setText(resto.getName_restaurant()); // NAME RESTO

            address_resto.setText(resto.getAddress());          // ADDRESS RESTO
            getRating(resto.getRating());                       // RATING RESTO

            // Configure recyclerView and buttons
            configure_recycler_view();
            configure_buttons();
        }
    }

    private void configure_buttons(){
        // Button CALL
        setColorButton(call_button,R.color.colorIconSelected);

        // Button WEBSITE
        setColorButton(website_button,R.color.colorIconSelected);

        // Button LIKE
        if(resto.getLiked()){
            setColorButton(like_button,R.color.colorStars);
            like_button.setEnabled(false);
        } else {
            setColorButton(like_button,R.color.colorIconSelected);
            setOnClickListenerButtonLike();
        }

        // Button choose resto
        if(did_I_choose_resto(resto.getWorkmatesList())) {
            setRestoChosen(true);
            button_valid.setEnabled(false);
        } else {
            setRestoChosen(false);
            setOnClickListenerButtonRestoValid();
        }
    }

    protected void getRating(Double rating) {

        int numStars;

        if (rating != null)
            numStars = Math.round(rating.floatValue());
        else
            numStars = 0;

        rating_resto.removeAllViews();

        if (numStars >= 1) {
            for (int i = 0; i < numStars - 1; i++) {
                ImageView imgView = new ImageView(getActivity().getApplicationContext());
                imgView.setImageResource(R.drawable.baseline_star_white_24);
                imgView.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(), (R.color.colorStars)));
                rating_resto.addView(imgView);
            }
        }
    }

    private void apply_picture_restaurant() {
        // Load the image using Glide
        GlideApp.with(getActivity().getApplicationContext())
                .load(firebase_recover.get_picture_resto(placeId))
                .into(picture_resto);
    }

    private void setColorButton(Button button, int color){

        button.setTextColor(getResources().getColor(color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            button.setCompoundDrawableTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity().getApplicationContext(),(color))));
        else {
            Drawable[] wrapDrawable = button.getCompoundDrawables();
            DrawableCompat.setTint(wrapDrawable[0], getResources().getColor(color));
        }
    }

    public void set_current_user(Workmates workmate) {
        current_user=workmate;
    }

    public void set_resto(List<Place_Nearby> list_restos) { }

    // ----------------------------------------------------------------------------------------------
    // ------------------------------- BUTTON CHOOSE RESTO ------------------------------------------
    // ----------------------------------------------------------------------------------------------

    private void setRestoChosen(Boolean select){

        if(getActivity().getApplicationContext()!=null){
            if(select)
                button_valid.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(),(R.color.colorGreen)));
            else
                button_valid.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(),(R.color.colorIconNotSelected)));
        }
    }

    private void setOnClickListenerButtonRestoValid(){
        button_valid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebase_update.update_chosen_status_resto(current_user.getId(),resto);
            }
        });
    }

    private Boolean did_I_choose_resto(List<Workmates> list_workmates){

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

    public void modify_state_button_choose() {
        firebase_recover.recover_resto_on_firebase(placeId);
        setRestoChosen(true);
        button_valid.setEnabled(false);
    }

    // ----------------------------------------------------------------------------------------------
    // ------------------------------- BUTTON LIKE RESTO --------------------------------------------
    // ----------------------------------------------------------------------------------------------

    private void setOnClickListenerButtonLike(){
        like_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebase_update.update_like_status_workmates(current_user.getId(), resto.getPlaceId());
            }
        });
    }

    public void modify_state_button_like() {
        setColorButton(like_button,R.color.colorStars);
        like_button.setEnabled(false);
    }

    @Override
    public void configure_and_show_restofragment(String placeId) {

    }
}




/*




    private void update_workmates_on_firebase(Workmates workmates){

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        databaseReference = databaseReference.child("workmates").child(workmates.getId());
        databaseReference.setValue(workmates);
    }

    private Workmates create_workmates(FirebaseUser mCurrentUser){

        String photoUrl = null;

        if(mCurrentUser.getPhotoUrl()!=null)
            photoUrl=mCurrentUser.getPhotoUrl().toString();

        return new Workmates(mCurrentUser.getDisplayName(),mCurrentUser.getUid(),photoUrl,true, placeId, resto.getName_restaurant(), "bar",null);
    }

    private Workmates recover_workmates_in_firebase(DataSnapshot datas){

        // Search id of user in firebase workmates list

        if(datas.child("id")!=null){

            for(DataSnapshot datas_C : datas.child("id").getChildren()) {

                List<String> list_resto_liked = new ArrayList<>();

                if (datas_C.child("list_resto_liked") != null) {
                    for (DataSnapshot datas_CC : datas_C.child("list_resto_liked").getChildren())
                        list_resto_liked.add((String) datas_CC.getValue());
                }

                System.out.println("eee1 " + list_resto_liked.toString());

                return new Workmates(
                        (String) datas_C.child("name").getValue(),
                        (String) datas_C.child("id").getValue(),
                        (String) datas_C.child("photoUrl").getValue(),
                        (Boolean) datas_C.child("chosen").getValue(),
                        (String) datas_C.child("resto_id").getValue(),
                        (String) datas_C.child("resto_name").getValue(),
                        (String) datas_C.child("resto_type").getValue(),
                        list_resto_liked);

            }
        }
        return null;
    }



                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        LatLng latLng = new LatLng(resto.getGeometry().getLocation().getLat(),resto.getGeometry().getLocation().getLng());
                        LatLngBounds latLngBounds = new LatLngBounds(latLng,latLng);
                        builder.setLatLngBounds(latLngBounds);
                        try {
                            startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                            e.printStackTrace();
                        }


*//*
public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getActivity());
                System.out.println("eeeeggg " + String.format("Place: %s", place.getName()));
            }
        }
    }*/
