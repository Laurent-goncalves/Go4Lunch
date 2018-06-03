package com.g.laurent.go4lunch.Controllers.Activities;

import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.g.laurent.go4lunch.Controllers.Fragments.RestoFragment;
import com.g.laurent.go4lunch.R;

public class RestoActivity extends AppCompatActivity {

    private final static String EXTRA_PLACE_ID = "placeId_resto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resto);

        String placeId = getIntent().getStringExtra(EXTRA_PLACE_ID);
        configure_and_show_restofragment(placeId);
    }

    public void configure_and_show_restofragment(String placeId) {

        // Create new bundle
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_PLACE_ID,placeId);

        // Create new fragment and transaction
        RestoFragment restoFragment = new RestoFragment();
        restoFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        fragmentTransaction.replace(R.id.fragment_resto_area, restoFragment);

        // Commit the transaction
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}
