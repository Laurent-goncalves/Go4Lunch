package com.g.laurent.go4lunch.Controllers.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.g.laurent.go4lunch.Controllers.Activities.SettingActivity;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.Firebase_update;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;
import static android.content.Context.MODE_PRIVATE;


public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.switch_french_english) Switch switch_fr_eng;
    @BindView(R.id.switch_text_french) TextView text_switch_fr;
    @BindView(R.id.switch_text_english) TextView text_switch_eng;
    @BindView(R.id.simpleSeekBar) SeekBar radius;
    @BindView(R.id.list_type_place) Spinner list_type_places;
    @BindView(R.id.text_setting_radius) TextView radius_value;
    @BindView(R.id.done_button) Button button_done;
    @BindView(R.id.initialize_liked_resto) Button button_reset_liked;
    @BindView(R.id.initialize_chosen_resto) Button button_reset_chosen;
    @BindView(R.id.framelayout_setting_frag) FrameLayout global_view;
    @BindView(R.id.switch_enable_notif) Switch enable_notif;
    @BindView(R.id.title_settings) TextView title_settings_textview;
    @BindView(R.id.enable_notif) TextView enable_notification_textview;
    @BindView(R.id.setting_radius) TextView radius_textview;
    @BindView(R.id.setting_type_place) TextView type_place__textview;



    private SharedPreferences sharedPreferences;
    private Context context;
    private Firebase_update firebase_update;
    private FirebaseUser mCurrentUser;
    private static final String EXTRA_PREFERENCES = "preferences";
    private static final String EXTRA_PREF_LANG = "language_preferences";
    private static final String EXTRA_PREF_RADIUS = "radius_preferences";
    private static final String EXTRA_PREF_TYPE_PLACE = "type_place_preferences";
    private static final String EXTRA_ENABLE_NOTIF = "enable_notif";
    private static final String EXTRA_RESTO_JSON = "resto_to_json";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        sharedPreferences = getActivity().getSharedPreferences(EXTRA_PREFERENCES,MODE_PRIVATE);
        firebase_update = new Firebase_update(context);
        configure_settings_areas();
        configureOnClickListener();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        context = getActivity().getApplicationContext();
        return view;
    }

    // ----------------------------------------------------------------------------------------------
    // ------------------------------------ CONFIGURATION -------------------------------------------
    // ----------------------------------------------------------------------------------------------

    private void configure_settings_areas(){

        // Configure switch button (language FR / ENG)
        configure_language_switch();

        // Configure permission notification
        configure_notification_switch();

        // Configure radius seekbar
        configure_radius_seekbar();

        // Configure list of type of place
        configure_type_of_place_list();

        // Configure button reset liked restaurants
        configure_button_reset_liked();

        // Configure button reset chosen restaurants
        configure_button_reset_chosen();

        // Configure button "DONE"
        configure_button_done();
    }

    private void configure_button_reset_chosen() {
        button_reset_chosen.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true);
            builder.setTitle(context.getResources().getString(R.string.title_reset_chosen));
            builder.setMessage(context.getResources().getString(R.string.confirmation_reset_chosen_resto));
            builder.setPositiveButton(context.getResources().getString(R.string.confirm),
                    (dialog, which) -> {
                        if(mCurrentUser!=null) {
                            firebase_update.initialize_chosen_restaurant(mCurrentUser.getUid());
                            sharedPreferences.edit().putString(EXTRA_RESTO_JSON, null).apply();
                            message_to_display(true);
                        } else
                            message_to_display(false);
                    });
            builder.setNegativeButton(context.getResources().getString(R.string.cancel), (dialog, which) -> {
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void configure_button_reset_liked() {
        button_reset_liked.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true);
            builder.setTitle(context.getResources().getString(R.string.title_reset_liked));
            builder.setMessage(context.getResources().getString(R.string.confirmation_reset_liked_resto));
            builder.setPositiveButton(context.getResources().getString(R.string.confirm),
                    (dialog, which) -> {
                        if(mCurrentUser!=null) {
                            firebase_update.initialize_like_list_restaurant(mCurrentUser.getUid());
                            message_to_display(true);
                        } else
                            message_to_display(false);
                    });
            builder.setNegativeButton(context.getResources().getString(R.string.cancel), (dialog, which) -> {
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void message_to_display(Boolean success){
        Toast toast;

        if(success)
            toast = Toast.makeText(context,context.getResources().getString(R.string.confirm_message_reset),Toast.LENGTH_SHORT);
        else
            toast = Toast.makeText(context,context.getResources().getString(R.string.alert_message_not_reset),Toast.LENGTH_SHORT);

        toast.show();
    }

    private void configure_notification_switch() {

        Boolean enable = sharedPreferences.getBoolean(EXTRA_ENABLE_NOTIF,false);

        if(enable)
            enable_notif.setChecked(true);
        else
            enable_notif.setChecked(false);
    }

    private void configure_button_done() {

        button_done.setOnClickListener(v -> {

            int requestCode;
            // Save language preferences
            if(switch_fr_eng.isChecked())
                sharedPreferences.edit().putString(EXTRA_PREF_LANG,"en").apply();
            else
                sharedPreferences.edit().putString(EXTRA_PREF_LANG,"fr").apply();

            // Save the permission to send notifications
            if(enable_notif.isChecked()) {
                requestCode = 1;
                sharedPreferences.edit().putBoolean(EXTRA_ENABLE_NOTIF, true).apply();
            } else {
                requestCode = 0;
                sharedPreferences.edit().putBoolean(EXTRA_ENABLE_NOTIF, false).apply();
            }

            // Save radius
            sharedPreferences.edit().putInt(EXTRA_PREF_RADIUS,radius.getProgress()).apply();

            // Save type of place
            sharedPreferences.edit().putString(EXTRA_PREF_TYPE_PLACE,list_type_places.getSelectedItem().toString()).apply();

            // Go back to previous fragment
            ((SettingActivity)getActivity()).send_result_enable_notification(requestCode);
        });
    }

    private void configure_language_switch(){

        String lang = sharedPreferences.getString(EXTRA_PREF_LANG,"en");
        switch (lang) {
            case "fr":
                text_switch_fr.setTextColor(getResources().getColor(R.color.colorIconSelected));
                text_switch_eng.setTextColor(getResources().getColor(R.color.colorIconNotSelected));
                switch_fr_eng.setChecked(false);
                break;
            case "en":
                text_switch_fr.setTextColor(getResources().getColor(R.color.colorIconNotSelected));
                text_switch_eng.setTextColor(getResources().getColor(R.color.colorIconSelected));
                switch_fr_eng.setChecked(true);
                break;
            default:
                sharedPreferences.edit().putString(EXTRA_PREF_LANG, "en").apply();
                text_switch_fr.setTextColor(getResources().getColor(R.color.colorIconNotSelected));
                text_switch_eng.setTextColor(getResources().getColor(R.color.colorIconSelected));
                switch_fr_eng.setChecked(true);
                break;
        }
    }

    private void configure_radius_seekbar(){

        int radius = sharedPreferences.getInt(EXTRA_PREF_RADIUS,500);
        String text = String.valueOf(radius) + " m";
        this.radius.setProgress(radius);
        radius_value.setText(text);
    }

    private void configure_type_of_place_list(){

        // Spinner click listener
        list_type_places.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<>();
        categories.add("restaurant");
        categories.add("bar");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        // attaching data adapter to spinner
        list_type_places.setAdapter(dataAdapter);

        String selected_item = sharedPreferences.getString(EXTRA_PREF_TYPE_PLACE,null);

        if(selected_item!=null){

            switch(selected_item){
                case "restaurant":
                    list_type_places.setSelection(0);
                    break;
                case "bar":
                    list_type_places.setSelection(1);
                    break;
            }
        }
    }

    // ----------------------------------------------------------------------------------------------
    // ------------------------------------ ON CLICK LISTENER ---------------------------------------
    // ----------------------------------------------------------------------------------------------

    private void configureOnClickListener(){
        setOnClickListenerSwitchButton();
        setOnRadiusChangeListener();
    }

    private void setOnClickListenerSwitchButton(){

        switch_fr_eng.setOnClickListener(v -> {
            if(switch_fr_eng.isChecked()){ // switch was on Eng
                text_switch_fr.setTextColor(getResources().getColor(R.color.colorIconNotSelected));
                text_switch_eng.setTextColor(getResources().getColor(R.color.colorIconSelected));
                sharedPreferences.edit().putString(EXTRA_PREF_LANG, "en").apply();
            } else { // switch was on Fr
                text_switch_fr.setTextColor(getResources().getColor(R.color.colorIconSelected));
                text_switch_eng.setTextColor(getResources().getColor(R.color.colorIconNotSelected));
                sharedPreferences.edit().putString(EXTRA_PREF_LANG, "fr").apply();
            }
            setLanguageForApp();

        });
    }

    private void setOnRadiusChangeListener(){

        radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int distance = radius.getProgress();
                String text = String.valueOf(distance) + " m";
                radius_value.setText(text);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void setLanguageForApp(){

        String lang = sharedPreferences.getString(EXTRA_PREF_LANG,"en");

        Locale locale = new Locale(lang);

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        configuration.setLocale(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            context.createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration,displayMetrics);
        }
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

        refresh_text_areas();
    }

    private void refresh_text_areas(){

        title_settings_textview.setText(context.getResources().getString(R.string.title_settings));
        enable_notification_textview.setText(context.getResources().getString(R.string.enable_notif));
        radius_textview.setText(context.getResources().getString(R.string.setting_radius));
        type_place__textview.setText(context.getResources().getString(R.string.type_of_place));
        button_done.setText(context.getResources().getString(R.string.done));
        button_reset_chosen.setText(context.getResources().getString(R.string.button_reset_chosen));
        button_reset_liked.setText(context.getResources().getString(R.string.button_reset_liked));
    }

}
