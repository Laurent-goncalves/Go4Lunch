package com.g.laurent.go4lunch;

import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.g.laurent.go4lunch.Models.Workmates;
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

    public ListMatesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        ButterKnife.bind(this,view);

        launch_search_restaurant_firebase();
        return view;
    }

    private void configure_recycler_view(){

        if(adapter == null) {
            // Create adapter passing in the sample user data
            adapter = new WorkmatesViewAdapter(getActivity().getApplicationContext(),list_workmates,TYPE_DISPLAY_WORKMATES_LIST);
            // Attach the adapter to the recyclerview to populate items
            list_workmates_recycler.setAdapter(adapter);
            // Set layout manager to position the items
            list_workmates_recycler.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        } else
            adapter.notifyDataSetChanged();
    }

    private void launch_search_restaurant_firebase() {

        list_workmates = new ArrayList<>();
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("workmates");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot datas) {
                if(datas!=null) {
                    for (DataSnapshot data : datas.getChildren()) {

                        Workmates workmates = new Workmates(
                                (String) data.child("name").getValue(),
                                (String) data.child("id").getValue(),
                                (String) data.child("photoUrl").getValue(),
                                (Boolean) data.child("chosen").getValue(),
                                (String) data.child("resto_id").getValue(),
                                (String) data.child("resto_name").getValue(),
                                (String) data.child("resto_type").getValue());

                        list_workmates.add(workmates);
                    }
                    // Create views
                    configure_recycler_view();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

}
