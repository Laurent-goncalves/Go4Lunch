package com.g.laurent.go4lunch.Controllers.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.g.laurent.go4lunch.R;

import java.util.Collections;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Intent intent;
    private Configuration conf;
    private Resources res;
    private static final int RC_SIGN_IN = 123;
    private static final String GOOGLE_SIGN_IN = "google";
    private static final String FACEBOOK_SIGN_IN = "facebook";
    private static final String EXTRA_PREFERENCES = "preferences";
    private static final String EXTRA_PREF_LANG = "language_preferences";
    @BindView(R.id.window_sign_in) CoordinatorLayout window_sign_in;
  //  @BindView(R.id.image_main_page) ImageView mImageView;
    @BindView(R.id.main_activity_button_login_google) Button button_google;
    @BindView(R.id.main_activity_button_login_facebook) Button button_facebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        set_language_button();

    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        if (searchManager != null)
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }*/

    @OnClick(R.id.main_activity_button_login_google)
    public void onClickLoginButtonGoogle() {
       this.startSignInActivity(GOOGLE_SIGN_IN);
    }

    @OnClick(R.id.main_activity_button_login_facebook)
    public void onClickLoginButtonFacebook() {
        this.startSignInActivity(FACEBOOK_SIGN_IN);
    }

    private void startSignInActivity(String sign_in){

        switch(sign_in){

            case GOOGLE_SIGN_IN:

                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setTheme(R.style.LoginTheme)
                                .setAvailableProviders(
                                        Collections.singletonList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build())) // SUPPORT GOOGLE
                                .setIsSmartLockEnabled(false, true)
                                .setLogo(R.drawable.ic_google)
                                .build(),
                        RC_SIGN_IN);
                break;

            case FACEBOOK_SIGN_IN:

                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setTheme(R.style.LoginTheme)
                                .setAvailableProviders(
                                        Collections.singletonList(new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build())) // FACEBOOK
                                .setIsSmartLockEnabled(false, true)
                                .setLogo(R.drawable.ic_facebook)
                                .build(),
                        RC_SIGN_IN);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    private void showSnackBar(CoordinatorLayout coordinatorLayout, String message){
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    public void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                showSnackBar(this.window_sign_in, getString(R.string.connection_succeed));

                // Launch MultiActivity
                Intent intent = new Intent(this,MultiActivity.class);
                startActivity(intent);

            } else { // ERRORS
                if (response == null) {
                    showSnackBar(this.window_sign_in, getString(R.string.error_authentication_canceled));
                } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(this.window_sign_in, getString(R.string.error_no_internet));
                } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(this.window_sign_in, getString(R.string.error_unknown_error));
                }
            }
        }
    }

    private void set_language_button(){

        SharedPreferences sharedPreferences = getSharedPreferences(EXTRA_PREFERENCES,MODE_PRIVATE);
        res = getApplicationContext().getResources();
        conf = res.getConfiguration();

        if(sharedPreferences!=null){

            String lang = sharedPreferences.getString(EXTRA_PREF_LANG,"En");

            switch (lang) {
                case "Fr":
                    conf.locale = Locale.FRANCE;
                    break;
                case "En":
                    conf.locale = Locale.FRANCE;
                    break;
                default:
                    sharedPreferences.edit().putString(EXTRA_PREF_LANG, "En").apply();
                    conf.locale = Locale.FRANCE;
                    break;
            }

            res.updateConfiguration(conf, res.getDisplayMetrics());
            button_google.setText(getApplicationContext().createConfigurationContext(conf).getResources().getString(R.string.connect_with_google));
            button_facebook.setText(getApplicationContext().createConfigurationContext(conf).getResources().getString(R.string.connect_with_facebook));
        }
    }

}



/* protected void configureToolbar(String title){
        // Assign toolbar_old
        toolbar_old = findViewById(R.id.activity_main_toolbar);
        // Sets the Toolbar
        setSupportActionBar(toolbar_old);

        this.title_tb = title;

        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    if (toolbar_old != null && title_toolbar != null) {
                        title_toolbar.setText(title_tb);

                        if (getSupportActionBar() != null) {
                            switch (title_tb) {

                                case "MyNews":
                                    icon_search.setVisibility(View.VISIBLE);
                                    icon_menu.setVisibility(View.VISIBLE);
                                    icon_notif.setVisibility(View.VISIBLE);
                                    setIconOnClickListener();
                                    // Disable the Up button
                                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                                    break;
                                case "Search Articles":
                                    icon_search.setVisibility(View.GONE);
                                    icon_menu.setVisibility(View.GONE);
                                    icon_notif.setVisibility(View.GONE);
                                    // Enable the Up button
                                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                                    break;
                                case "Notifications":
                                    icon_search.setVisibility(View.GONE);
                                    icon_menu.setVisibility(View.GONE);
                                    icon_notif.setVisibility(View.GONE);
                                    // Enable the Up button
                                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                                    break;
                            }
                        }
                    }

                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }*/