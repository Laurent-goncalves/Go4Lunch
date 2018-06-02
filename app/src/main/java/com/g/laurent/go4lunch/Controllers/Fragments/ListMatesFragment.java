package com.g.laurent.go4lunch.Controllers.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Views.Resto_Details.WorkmatesViewAdapter;

import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListMatesFragment extends Fragment {

    @BindView(R.id.list_workmates) RecyclerView list_workmates_recycler;
    private final static String TYPE_DISPLAY_WORKMATES_LIST = "list_of_workmates";
    private WorkmatesViewAdapter adapter;
    private List<Workmate> list_workmates;
    private Context context;

    public ListMatesFragment() {
        // Required empty public constructor
    }

    public static ListMatesFragment newInstance(String api_key) {

        // Create new fragment
        ListMatesFragment frag = new ListMatesFragment();
        String EXTRA_API_KEY = "api_key";

        // Create bundle and add it some data
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_API_KEY, api_key);
        frag.setArguments(bundle);

        return(frag);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        ButterKnife.bind(this,view);
        context = getActivity().getApplicationContext();

        // Get list of workmates on firebase
        Firebase_recover firebase_recover = new Firebase_recover(context,this);
        firebase_recover.recover_list_workmates();

        return view;
    }

    private void configure_recycler_view(){

        // Create adapter passing in the sample user data
        adapter = new WorkmatesViewAdapter(context,list_workmates,TYPE_DISPLAY_WORKMATES_LIST);
        // Attach the adapter to the recyclerview to populate items
        list_workmates_recycler.setAdapter(adapter);
        // Set layout manager to position the items
        list_workmates_recycler.setLayoutManager(new LinearLayoutManager(context));
    }

    public void set_list_of_workmates(List<Workmate> list_workmates){
        this.list_workmates=list_workmates;
        configure_recycler_view();
    }
}
