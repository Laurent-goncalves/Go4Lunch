package com.g.laurent.go4lunch;

import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import java.util.Collections;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String GOOGLE_SIGN_IN = "google";
    private static final String FACEBOOK_SIGN_IN = "facebook";
    @BindView(R.id.window_sign_in) CoordinatorLayout window_sign_in;

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);

    }

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

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                showSnackBar(this.window_sign_in, getString(R.string.connection_succeed));

                // Launch MapsActivity
                Intent intent = new Intent(this,MapsActivity.class);
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

}
