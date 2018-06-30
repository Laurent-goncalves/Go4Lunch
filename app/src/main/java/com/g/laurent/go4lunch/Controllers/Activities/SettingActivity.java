package com.g.laurent.go4lunch.Controllers.Activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.g.laurent.go4lunch.Controllers.Fragments.SettingsFragment;
import com.g.laurent.go4lunch.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.activity_setting_toolbar) Toolbar toolbar;
    SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        configure_and_show_settings_fragment();
        configureToolBar();
    }

    public void configure_and_show_settings_fragment(){

        settingsFragment = new SettingsFragment();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.setting_activity_main, settingsFragment);
        fragmentTransaction.commit();
    }

    private void configureToolBar(){

        runOnUiThread(() -> {
            toolbar.setVisibility(View.VISIBLE);
            setSupportActionBar(toolbar);
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            if(actionBar!=null) {
                actionBar.setTitle("Settings");
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void send_result_enable_notification(int result) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",result);
        if(result==1)
            setResult(Activity.RESULT_FIRST_USER,returnIntent);
        else
            setResult(Activity.RESULT_CANCELED,returnIntent);

        finish();
    }

    public SettingsFragment getSettingsFragment() {
        return settingsFragment;
    }
}
