package com.g.laurent.go4lunch;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.request.transition.Transition;
import com.g.laurent.go4lunch.Views.ListViewAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListRestoFragment extends Fragment {

    private FirebaseDatabase database;
    private DatabaseReference ref;
    final Transition.ViewAdapter adapter;

    public ListRestoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        /*database = FirebaseDatabase.getInstance();
        ref = database.getReferenceFromUrl("https://go4lunch-203512.firebaseio.com/");*/
        adapter = new ListViewAdapter( // create the ListViewAdapter for displaying the list of feelings
                getContext(), chrono_texts,mFeelingsChronology, colors,screen_width,screen_height);

        mListView.setAdapter(adapter);
        return inflater.inflate(R.layout.fragment_list_resto, container, false);
    }



}
