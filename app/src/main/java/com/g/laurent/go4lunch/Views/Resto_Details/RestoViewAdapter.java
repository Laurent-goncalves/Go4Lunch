package com.g.laurent.go4lunch.Views.Resto_Details;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;


import com.g.laurent.go4lunch.Views.Resto_List.RestoViewHolder;

import java.util.List;

public class RestoViewAdapter extends RecyclerView.Adapter<RestoViewHolder> {


    private final Context context;
    private final List<String> list_workmates_id;

    public RestoViewAdapter(Context context, List<String> list_workmates_id){
        this.context=context;
        this.list_workmates_id=list_workmates_id;
    }


    @NonNull
    @Override
    public RestoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RestoViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
