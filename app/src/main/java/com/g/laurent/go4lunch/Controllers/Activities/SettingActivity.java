package com.g.laurent.go4lunch.Controllers.Activities;

import android.app.FragmentTransaction;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        configure_and_show_settings_fragment();
        configureToolBar("Settings",true);
    }

    public void configure_and_show_settings_fragment(){

        SettingsFragment settingsFragment = new SettingsFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.setting_activity_main, settingsFragment);
        fragmentTransaction.commit();
    }

    private void configureToolBar(String title, Boolean bar_display){

        runOnUiThread(() -> {
            if(bar_display){
                toolbar.setVisibility(View.VISIBLE);
                setSupportActionBar(toolbar);
                android.support.v7.app.ActionBar actionBar = getSupportActionBar();
                if(actionBar!=null) {
                    actionBar.setTitle(title);
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }
            } else
                toolbar.setVisibility(View.GONE);
        });
    }
}
