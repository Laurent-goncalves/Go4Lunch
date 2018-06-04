package com.g.laurent.go4lunch.Views.Resto_Details;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import java.util.List;

public class WorkmatesViewAdapter extends RecyclerView.Adapter<WorkmatesViewHolder> {

    private Context context;
    private List<Workmate> list_workmates;
    private String TYPE_DISPLAY;

    public WorkmatesViewAdapter(Context context, List<Workmate> list_workmates, String type_display) {
        this.context=context;
        this.list_workmates=list_workmates;
        this.TYPE_DISPLAY = type_display;
    }

    @NonNull
    @Override
    public WorkmatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.workmates_item, parent, false);
        return new WorkmatesViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmatesViewHolder holder, int position) {
        if(list_workmates!=null){
            holder.show_workmates(context,list_workmates.get(position), TYPE_DISPLAY);
        }
    }

    @Override
    public int getItemCount() {
        if(list_workmates!=null)
            return list_workmates.size();
        else
            return 0;
    }

}
