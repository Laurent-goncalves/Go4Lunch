package com.g.laurent.go4lunch.Views.RestoListViews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.g.laurent.go4lunch.Models.PlaceNearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;

public class ListViewAdapter extends RecyclerView.Adapter<RestoViewHolder> {

    private Context context;
    private List<PlaceNearby> list_search_nearby;
    private List<Workmate> list_workmates;
    private LatLng current_loc;

    public ListViewAdapter(Context context, List<PlaceNearby> list_search_nearby, List<Workmate> list_workmates, LatLng current_loc){
        this.context=context;
        this.list_search_nearby=list_search_nearby;
        this.list_workmates=list_workmates;
        this.current_loc=current_loc;
    }

    @NonNull
    @Override
    public RestoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.resto_item, parent, false);
        return new RestoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestoViewHolder holder, int position) {
        if(list_search_nearby!=null){
            holder.configure_restaurant(current_loc,list_search_nearby.get(position),list_workmates,context);
        }
    }

    @Override
    public int getItemCount() {
        if(list_search_nearby!=null)
            return list_search_nearby.size();
        else
            return 0;
    }
}
