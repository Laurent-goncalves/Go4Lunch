package com.g.laurent.go4lunch.Controllers.Activities;

import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.g.laurent.go4lunch.Controllers.Fragments.RestoFragment;
import com.g.laurent.go4lunch.R;

public class RestoActivity extends AppCompatActivity {

    private final static String EXTRA_RESTO_DETAILS = "resto_details";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resto);

        // Recover resto details and show restoFragment
        String resto_details = getIntent().getStringExtra(EXTRA_RESTO_DETAILS);
        configure_and_show_restofragment(resto_details);
    }

    public void configure_and_show_restofragment(String resto_details) {

        // Create new bundle
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_RESTO_DETAILS,resto_details);

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
