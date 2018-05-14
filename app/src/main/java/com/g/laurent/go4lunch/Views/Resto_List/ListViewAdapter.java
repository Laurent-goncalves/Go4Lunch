package com.g.laurent.go4lunch.Views.Resto_List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class ListViewAdapter extends RecyclerView.Adapter<RestoViewHolder> {

    private Context context;
    private List<Place_Nearby> list_search_nearby;
    private LatLng current_loc;
    private final Listener callback;

    public ListViewAdapter(Context context, List<Place_Nearby> list_search_nearby, LatLng current_loc, Listener callback){
        this.context=context;
        this.list_search_nearby=list_search_nearby;
        this.current_loc=current_loc;
        this.callback=callback;
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
            holder.configure_restaurant(current_loc,list_search_nearby.get(position),callback);
        }
    }

    @Override
    public int getItemCount() {
        return list_search_nearby.size();
    }

    public interface Listener {
        void onClickShowRestoDetails(String placeId);
    }
}
