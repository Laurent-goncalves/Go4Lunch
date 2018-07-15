package com.g.laurent.go4lunch.Controllers.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.g.laurent.go4lunch.Controllers.Activities.WebActivity;
import com.g.laurent.go4lunch.Models.PlaceNearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.FirebaseRecover;
import com.g.laurent.go4lunch.Utils.FirebaseUpdate;
import com.g.laurent.go4lunch.Views.WorkmatesViews.WorkmatesViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
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
    @BindView(R.id.text_nobody_joining) TextView text_nobody;
    @BindView(R.id.list_workmates_joining_resto) RecyclerView list_workmates_recycler;
    private final static String TYPE_DISPLAY_WORKMATES_BY_RESTO = "list_of_workmates_by_resto";
    private final static String EXTRA_RESTO_DETAILS = "resto_details";
    private final static String RENEW_LIST_WORKMATES = "renew_list_workmates";
    private final static String INITIAL_LIST_WORKMATES = "initial_list_workmates";
    private static final String EXTRA_PREFERENCES = "preferences";
    private static final String EXTRA_RESTO_JSON = "resto_to_json";
    private static final String EXTRA_LINK = "linkaddress";
    private PlaceNearby resto;
    private String placeId;
    private Context context;
    private FirebaseUpdate mFirebase_update;
    private List<Workmate> list_workmates_joining;
    private List<Workmate> list_workmates_liked;
    private FirebaseUser mCurrentUser;
    private String api_key;

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
        context = getActivity().getApplicationContext();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Create PlaceNearby from resto in json format
        Gson gson = new Gson();
        String resto_json = getArguments().getString(EXTRA_RESTO_DETAILS,null);
        resto = gson.fromJson(resto_json,PlaceNearby.class);

        placeId = resto.getPlaceId(); // recover the id of the resto

        mFirebase_update = new FirebaseUpdate(context,this);
        api_key = context.getResources().getString(R.string.google_maps_key2);
        recover_list_workmates();

        return view;
    }

    // ----------------------------------------------------------------------------------------------
    // ---------------------------------------- WORKMATES -------------------------------------------
    // ----------------------------------------------------------------------------------------------

    public void recover_list_workmates() {
        FirebaseRecover firebase_recover = new FirebaseRecover(context,this);
        firebase_recover.recover_list_workmates();
    }

    public void renew_list_workmates() {
        FirebaseRecover firebase_recover = new FirebaseRecover(context,this);
        firebase_recover.renew_list_workmates();
    }

    public void set_list_of_workmates(List<Workmate> list_workmates_resto, String type) {

        this.list_workmates_joining = new ArrayList<>();
        this.list_workmates_liked= new ArrayList<>();

        // create list of workmates which chose this restaurant
        for(Workmate workmate : list_workmates_resto){
            if(workmate!=null){
                if(workmate.getResto_id()!=null){
                    if(workmate.getResto_id().equals(placeId))
                        this.list_workmates_joining.add(workmate);
                }
            }
        }

        // create list of workmates which like this restaurant
        for(Workmate workmate : list_workmates_resto){
            if(workmate!=null){
                if(workmate.getList_resto_liked()!=null){

                    for(String id_resto : workmate.getList_resto_liked()){
                        if(id_resto.equals(placeId))
                            this.list_workmates_liked.add(workmate);
                    }
                }
            }
        }

        switch(type){

            case INITIAL_LIST_WORKMATES:
                configure_views_with_resto();
                break;

            case RENEW_LIST_WORKMATES:
                configure_recycler_view();
                configure_button_like();
                configure_button_choose_resto();
                break;
        }
    }

    private void configure_recycler_view(){

        // check is anyone is joining the restaurant
        if(list_workmates_joining!=null){
            if(list_workmates_joining.size()!=0){
                // Remove the textView for indicating nobody is joining
                text_nobody.setVisibility(View.GONE);
                // Create adapter passing in the sample user data
                WorkmatesViewAdapter adapter = new WorkmatesViewAdapter(context, list_workmates_joining, TYPE_DISPLAY_WORKMATES_BY_RESTO);
                // Attach the adapter to the recyclerview to populate items
                list_workmates_recycler.setAdapter(adapter);
                // Set layout manager to position the items
                list_workmates_recycler.setLayoutManager(new LinearLayoutManager(context));
            } else
                text_nobody.setVisibility(View.VISIBLE);
        } else
            text_nobody.setVisibility(View.VISIBLE);
    }

    // ----------------------------------------------------------------------------------------------
    // ---------------------------------------- CONFIGURE VIEWS -------------------------------------
    // ----------------------------------------------------------------------------------------------

    public void configure_views_with_resto(){

        // Create views
        if(resto!=null){
            apply_picture_restaurant();                         // PICTURE RESTO

            if(resto.getName_restaurant()!=null) {
                if (resto.getName_restaurant().length() >= 15) {
                    String name = resto.getName_restaurant().substring(0, 15) + "...";
                    name_resto.setText(name); // NAME RESTO
                } else
                    name_resto.setText(resto.getName_restaurant()); // NAME RESTO
            }

            address_resto.setText(resto.getAddress());          // ADDRESS RESTO
            getRating(resto.getRating());                       // RATING RESTO

            // Configure recyclerView and buttons
            configure_recycler_view();
            configure_buttons_call_and_website();
            configure_button_like();
            configure_button_choose_resto();
        }
    }

    // ----------------------------------------------------------------------------------------------
    // ---------------------------------- PICTURE RESTO & RATING ------------------------------------
    // ----------------------------------------------------------------------------------------------

    private void apply_picture_restaurant() {

        String link = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + resto.getPhoto_reference()
                + "&key=" + api_key;

        // Load the image using Glide
        if(getActivity()!=null)
        Glide.with(getActivity().getApplicationContext())
                .load(link)
                .into(picture_resto);

    }

    private void getRating(Double rating) {

        int numStars;

        if (rating != null)
            numStars = Math.round(rating.floatValue()); // round the value of rating
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

    // ----------------------------------------------------------------------------------------------
    // ---------------------------------- CONFIG BUTTONS CALL & WEBSITE------------------------------
    // ----------------------------------------------------------------------------------------------

    private void configure_buttons_call_and_website(){
        // Button CALL
        setColorButton(call_button,R.color.colorIconSelected);
        setOnClickListenerButtonCall();

        // Button WEBSITE
        setColorButton(website_button,R.color.colorIconSelected);
        setOnClickListenerButtonWebsite();
    }

    private void setOnClickListenerButtonWebsite() {
        website_button.setOnClickListener(v -> {
            if(resto.getWebsite()!=null){ // if the user clicks on button website, a webview opens
                Intent intent = new Intent(context,WebActivity.class);
                intent.putExtra(EXTRA_LINK,resto.getWebsite());
                context.startActivity(intent);
            } else {
                Toast toast = Toast.makeText(context,context.getResources().getString(R.string.website_unavailable),Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    private void setColorButton(Button button, int color){

        button.setTextColor(getResources().getColor(color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            button.setCompoundDrawableTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity().getApplicationContext(),(color))));
        else {
            Drawable[] wrapDrawable = button.getCompoundDrawables();
            if(wrapDrawable[0]!=null)
            DrawableCompat.setTint(wrapDrawable[0], context.getResources().getColor(color));
        }
    }

    private void setOnClickListenerButtonCall(){

        call_button.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(
                    context,android.Manifest.permission.CALL_PHONE) !=
                    PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(), new
                        String[]{android.Manifest.permission.CALL_PHONE}, 0);

                Toast toast = Toast.makeText(context,context.getResources().getString(R.string.phone_number_unavailable),Toast.LENGTH_LONG);
                toast.show();

            } else {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + resto.getPhone_number())));
            }
        });
    }

    // ----------------------------------------------------------------------------------------------
    // ---------------------------------- CONFIG BUTTON LIKE ----------------------------------------
    // ----------------------------------------------------------------------------------------------

    private void configure_button_like() {

        // Button choose resto
        if(did_I_like_resto(list_workmates_liked)) {
            setColorButton(like_button, R.color.colorStars);
            like_button.setEnabled(false);
        } else {
            setColorButton(like_button, R.color.colorIconSelected);
            setOnClickListenerButtonLike();
        }
    }

    private boolean did_I_like_resto(List<Workmate> list_workmates) {

        Boolean answer = false;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && list_workmates!=null) {
            // User is signed in

            // Search user id among workmates who want to lunch at this resto
            for(Workmate workmates : list_workmates){

                if(workmates!=null){
                    if(workmates.getId()!=null){
                        if(workmates.getId().equals(user.getUid())) { // when user is found

                            if(workmates.getList_resto_liked()!=null){

                                for(String id_liked : workmates.getList_resto_liked()){
                                    if(id_liked!=null){
                                        if(id_liked.equals(resto.getPlaceId()))
                                            answer = true;
                                    }
                                }

                                break;
                            }
                        }
                    }
                }
            }
        }

        return answer;
    }

    private void setOnClickListenerButtonLike(){
        like_button.setOnClickListener(v -> {
            if(mCurrentUser!=null)
                mFirebase_update.update_like_status_workmates(mCurrentUser.getUid(), resto);
        });
    }

    public void modify_state_button_like() {
        setColorButton(like_button, R.color.colorStars);
        like_button.setEnabled(false);
        renew_list_workmates();
    }

    // ----------------------------------------------------------------------------------------------
    // ---------------------------------- CONFIG BUTTON CHOOSE RESTO --------------------------------
    // ----------------------------------------------------------------------------------------------

    private void configure_button_choose_resto() {

        // Button choose resto
        if(did_I_choose_resto(list_workmates_joining)) {

            // save in sharedpreferences the resto chosen in Json format
            SharedPreferences sharedPreferences = context.getSharedPreferences(EXTRA_PREFERENCES,Context.MODE_PRIVATE);
            Gson gson = new Gson();
            String resto_json = gson.toJson(resto,PlaceNearby.class);
            sharedPreferences.edit().putString(EXTRA_RESTO_JSON,resto_json).apply();

            setRestoChosen(true);
            button_valid.setEnabled(false);
        } else {
            setRestoChosen(false);
            setOnClickListenerButtonRestoValid();
        }
    }

    private Boolean did_I_choose_resto(List<Workmate> list_workmates){

        Boolean answer = false;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && list_workmates!=null) {
            // User is signed in

            // Search user id among workmates who want to lunch at this resto
            for(Workmate workmates : list_workmates){

                if(workmates!=null){
                    if(workmates.getId()!=null){
                        if(workmates.getId().equals(user.getUid())) {

                            if(workmates.getResto_id()!=null){
                                if(workmates.getResto_id().equals(resto.getPlaceId()))
                                    answer = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return answer;
    }

    private void setRestoChosen(Boolean select){

        if(getActivity().getApplicationContext()!=null){
            if(select)
                button_valid.setImageDrawable(getResources().getDrawable(R.drawable.baseline_check_circle_green_24));
            else
                button_valid.setImageDrawable(getResources().getDrawable(R.drawable.baseline_check_circle_black_24));
        }
    }

    private void setOnClickListenerButtonRestoValid(){
        button_valid.setOnClickListener(v -> {
            if(mCurrentUser!=null)
                mFirebase_update.update_chosen_status_workmate(mCurrentUser.getUid(), resto);
        });
    }

    public void modify_state_button_choose() {
        setRestoChosen(true);
        button_valid.setEnabled(false);
        renew_list_workmates();
    }
}
