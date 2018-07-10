package com.g.laurent.go4lunch.Controllers.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.FirebaseRecover;
import com.g.laurent.go4lunch.Views.RestoListViews.ListViewAdapter;
import com.g.laurent.go4lunch.Views.WorkmatesViews.WorkmatesViewAdapter;
import java.util.List;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListMatesFragment extends BaseRestoFragment {

    @BindView(R.id.list_workmates) RecyclerView list_workmates_recycler;
    private final static String TYPE_DISPLAY_WORKMATES_LIST = "list_of_workmates";
    private List<Workmate> list_workmates;
    private Context context;

    public ListMatesFragment() {
        // Required empty public constructor
    }

    public static ListMatesFragment newInstance() {
        return new ListMatesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        ButterKnife.bind(this,view);
        context = Objects.requireNonNull(getActivity()).getApplicationContext();

        // Get list of workmates on firebase
        mFirebase_recover = new FirebaseRecover(context,this);
        mFirebase_recover.recover_list_workmates();

        return view;
    }

    private void configure_recycler_view(){
        try {
            runOnUiThread(() -> {
                if (context != null) {
                    // Create adapter passing in the sample user data
                    WorkmatesViewAdapter adapter = new WorkmatesViewAdapter(context, list_workmates, TYPE_DISPLAY_WORKMATES_LIST);
                    // Attach the adapter to the recyclerview to populate items
                    list_workmates_recycler.setAdapter(adapter);
                    // Set layout manager to position the items
                    list_workmates_recycler.setLayoutManager(new LinearLayoutManager(context));
                }
            });
        } catch(Throwable ignored){}
    }

    public void set_list_of_workmates(List<Workmate> list_workmates){
        this.list_workmates=list_workmates;
        configure_recycler_view();
    }
}
