package com.g.laurent.go4lunch.Controllers.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.g.laurent.go4lunch.Controllers.Fragments.WebFragment;
import com.g.laurent.go4lunch.R;

public class WebActivity extends AppCompatActivity {

    private String link;
    public static final String EXTRA_LINK = "linkaddress";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        // Recover the URL for the website to show
        link=getIntent().getStringExtra(EXTRA_LINK);

        // configure and show webfragment
        configureAndShowWebFragment();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast toast = Toast.makeText(getApplicationContext(),
                getApplicationContext().getResources().getString(R.string.press_twice),Toast.LENGTH_LONG);
        toast.show();
        finish();
    }

    private void configureAndShowWebFragment(){

        // Create a new bundle to send the link to the WebFragment
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_LINK,link);

        // Configure and show webFragment
        WebFragment webFragment = (WebFragment) getSupportFragmentManager().findFragmentById(R.id.activity_web_frame_layout);
        if (webFragment == null) {
            webFragment = new WebFragment();
            webFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_web_frame_layout, webFragment)
                    .commit();
        }
    }
}
