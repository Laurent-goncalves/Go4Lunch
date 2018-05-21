package com.g.laurent.go4lunch;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.g.laurent.go4lunch.Models.Workmates;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Views.Resto_Details.WorkmatesViewAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
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
    private List<Workmates> list_workmates;
    private Context context;

    public ListMatesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        ButterKnife.bind(this,view);
        context = getActivity().getApplicationContext();

        Firebase_recover firebase_tool = new Firebase_recover(getActivity().getApplicationContext(),null,null,null,this);

        // Get list of workmates on firebase
        firebase_tool.recover_list_workmates();

        return view;
    }

    private void configure_recycler_view(){

        if(adapter == null) {
            // Create adapter passing in the sample user data
            adapter = new WorkmatesViewAdapter(context,list_workmates,TYPE_DISPLAY_WORKMATES_LIST);
            // Attach the adapter to the recyclerview to populate items
            list_workmates_recycler.setAdapter(adapter);
            // Set layout manager to position the items
            list_workmates_recycler.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        } else
            adapter.notifyDataSetChanged();
    }

    public void set_list_of_workmates(List<Workmates> list_workmates){
        this.list_workmates=list_workmates;
        configure_recycler_view();
    }
}
