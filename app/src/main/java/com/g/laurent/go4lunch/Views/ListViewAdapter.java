package com.g.laurent.go4lunch.Views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.R;
import com.google.android.gms.maps.model.LatLng;

public class ListViewAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    private Context context;
    private List_Search_Nearby list_search_nearby;
    private LatLng current_loc;

    public ListViewAdapter(Context context, List_Search_Nearby list_search_nearby, LatLng current_loc){
        this.context=context;
        this.list_search_nearby=list_search_nearby;
        this.current_loc=current_loc;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.resto_item, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        if(list_search_nearby!=null){
            if(list_search_nearby.getList_places_nearby()!=null)
                holder.configure_restaurant(current_loc,list_search_nearby.getList_places_nearby().get(position));
        }
    }

    @Override
    public int getItemCount() {
        return list_search_nearby.getList_places_nearby().size();
    }
}
