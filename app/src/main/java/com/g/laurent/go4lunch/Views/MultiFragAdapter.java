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
import com.google.android.gms.maps.model.LatLng;

import java.util.List;


public class MultiFragAdapter extends FragmentPagerAdapter {

    private Context context;
    private MapsFragment mapsFragment;
    private ListRestoFragment listRestoFragment;
    private List<Place_Nearby> list_restos;
    private LatLng current_place;

    public MultiFragAdapter(FragmentManager fm, Context context, List<Place_Nearby> list_restos, LatLng current_place) {
        super(fm);
        this.context=context;
        this.current_place=current_place;
        this.list_restos=list_restos;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: //Page number 1
                mapsFragment = MapsFragment.newInstance(list_restos, current_place);
                return mapsFragment;
            case 1: //Page number 2
                listRestoFragment = ListRestoFragment.newInstance(list_restos);
                return listRestoFragment;
            case 2: //Page number 3
                return ListMatesFragment.newInstance();
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

        if(context.getResources()!=null) {
            switch (position) {
                case 0: //Page number 1
                    return context.getResources().getString(R.string.map_view_tab);
                case 1: //Page number 2
                    return context.getResources().getString(R.string.list_view_tab);
                case 2: //Page number 3
                    return context.getResources().getString(R.string.workmates_tab);
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public MapsFragment getMapsFragment() {
        return mapsFragment;
    }

    public ListRestoFragment getListRestoFragment() {
        return listRestoFragment;
    }
}
