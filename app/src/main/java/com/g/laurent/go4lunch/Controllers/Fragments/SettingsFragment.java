package com.g.laurent.go4lunch.Controllers.Fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.g.laurent.go4lunch.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.switch_french_english) Switch switch_fr_eng;
    @BindView(R.id.switch_text_french) TextView text_switch_fr;
    @BindView(R.id.switch_text_english) TextView text_switch_eng;
    @BindView(R.id.simpleSeekBar) SeekBar radius;
    @BindView(R.id.list_type_place) Spinner list_type_places;
    @BindView(R.id.text_setting_radius) TextView radius_value;
    @BindView(R.id.done_button) Button button_done;
    private SharedPreferences sharedPreferences;
    private static final String EXTRA_PREFERENCES = "preferences";
    private static final String EXTRA_PREF_LANG = "language_preferences";
    private static final String EXTRA_PREF_RADIUS = "radius_preferences";
    private static final String EXTRA_PREF_TYPE_PLACE = "type_place_preferences";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this,view);
        sharedPreferences = getActivity().getSharedPreferences(EXTRA_PREFERENCES,MODE_PRIVATE);
        configure_settings_areas();
        configureOnClickListener();
        return view;
    }

    // ----------------------------------------------------------------------------------------------
    // ------------------------------------ CONFIGURATION -------------------------------------------
    // ----------------------------------------------------------------------------------------------

    private void configure_settings_areas(){

        // Configure switch button (language FR / ENG)
        configure_language_switch();

        // Configure radius seekbar
        configure_radius_seekbar();

        // Configure list of type of place
        configure_type_of_place_list();

        // Configure button "DONE"
        configure_button_done();
    }

    private void configure_button_done() {

        button_done.setOnClickListener(v -> {
            // Save language preferences
            if(switch_fr_eng.isChecked())
                sharedPreferences.edit().putString(EXTRA_PREF_LANG,"En").apply();
            else
                sharedPreferences.edit().putString(EXTRA_PREF_LANG,"Fr").apply();

            // Save radius
            sharedPreferences.edit().putString(EXTRA_PREF_RADIUS,String.valueOf(radius.getProgress())).apply();

            // Save type of place
            sharedPreferences.edit().putString(EXTRA_PREF_TYPE_PLACE,list_type_places.getSelectedItem().toString()).apply();

            // Go back to previous fragment
            getActivity().finish();
        });
    }

    private void configure_language_switch(){

        String lang = sharedPreferences.getString(EXTRA_PREF_LANG,"En");
        switch (lang) {
            case "Fr":
                text_switch_fr.setTextColor(getResources().getColor(R.color.colorIconSelected));
                text_switch_eng.setTextColor(getResources().getColor(R.color.colorIconNotSelected));
                switch_fr_eng.setChecked(false);
                break;
            case "En":
                text_switch_fr.setTextColor(getResources().getColor(R.color.colorIconNotSelected));
                text_switch_eng.setTextColor(getResources().getColor(R.color.colorIconSelected));
                switch_fr_eng.setChecked(true);
                break;
            default:
                sharedPreferences.edit().putString(EXTRA_PREF_LANG, "En").apply();
                text_switch_fr.setTextColor(getResources().getColor(R.color.colorIconNotSelected));
                text_switch_eng.setTextColor(getResources().getColor(R.color.colorIconSelected));
                switch_fr_eng.setChecked(true);
                break;
        }
    }

    private void configure_radius_seekbar(){
        String text = "0 m";
        radius_value.setText(text);
    }

    private void configure_type_of_place_list(){

        // Spinner click listener
        list_type_places.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("restaurant");
        categories.add("bar");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        // attaching data adapter to spinner
        list_type_places.setAdapter(dataAdapter);
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
            } else { // switch was on Fr
                text_switch_fr.setTextColor(getResources().getColor(R.color.colorIconSelected));
                text_switch_eng.setTextColor(getResources().getColor(R.color.colorIconNotSelected));
            }
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
}
