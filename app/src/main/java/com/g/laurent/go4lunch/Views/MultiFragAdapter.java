package com.g.laurent.go4lunch.Views;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import com.g.laurent.go4lunch.Controllers.Fragments.ListMatesFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.R;

public class MultiFragAdapter extends FragmentPagerAdapter {

    private String api_key;
    private Context context;

    public MultiFragAdapter(FragmentManager fm, String api_key, Context context) {
        super(fm);
        this.api_key=api_key;
        this.context=context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: //Page number 1
                return MapsFragment.newInstance(api_key);
            case 1: //Page number 2
                return ListRestoFragment.newInstance(api_key);
            case 2: //Page number 3
                return ListMatesFragment.newInstance(api_key);
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
}
