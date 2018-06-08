package com.g.laurent.go4lunch.Views;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import com.g.laurent.go4lunch.Controllers.Fragments.ListMatesFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.R;

import java.util.List;

public class MultiFragAdapter extends FragmentPagerAdapter {

    private String api_key;
    private Context context;
    private MapsFragment mapsFragment;
    private ListRestoFragment listRestoFragment;
    private ListMatesFragment listMatesFragment;

    public MultiFragAdapter(FragmentManager fm, String api_key, Context context) {
        super(fm);
        this.api_key=api_key;
        this.context=context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: //Page number 1
                mapsFragment = MapsFragment.newInstance(api_key);
                return mapsFragment;
            case 1: //Page number 2
                listRestoFragment = ListRestoFragment.newInstance(api_key);
                return listRestoFragment;
            case 2: //Page number 3
                listMatesFragment = ListMatesFragment.newInstance(api_key);
                return listMatesFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0: //Page number 1
                return context.getResources().getString(R.string.map_view);
            case 1: //Page number 2
                return context.getResources().getString(R.string.list_view);
            case 2: //Page number 3
                return context.getResources().getString(R.string.workmates);
            default:
                return null;
        }
    }

    public MapsFragment getMapsFragment() {
        return mapsFragment;
    }

    public ListRestoFragment getListRestoFragment() {
        return listRestoFragment;
    }

    public ListMatesFragment getListMatesFragment() {
        return listMatesFragment;
    }
}
