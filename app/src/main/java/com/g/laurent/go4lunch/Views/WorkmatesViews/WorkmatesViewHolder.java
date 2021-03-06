package com.g.laurent.go4lunch.Views.WorkmatesViews;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkmatesViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.workmates_image) ImageView picture_workmate;
    @BindView(R.id.workmates_text) TextView workmates_text;
    @BindView(R.id.workmates_line_separator) View line_separator;
    private final static String TYPE_DISPLAY_WORKMATES_LIST = "list_of_workmates";
    private final static String TYPE_DISPLAY_WORKMATES_BY_RESTO = "list_of_workmates_by_resto";
    private Context context;

    WorkmatesViewHolder(View itemView, Context context) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.context=context;
    }

    public void show_workmates(Context context, Workmate workmates, String type_display) {
        configure_picture_workmates(context,workmates);
        configure_workmates_text(workmates, type_display);
        configure_line_separator(type_display);
    }

    private void configure_line_separator(String type_display){
        switch(type_display){
            case TYPE_DISPLAY_WORKMATES_LIST:
                line_separator.setVisibility(View.VISIBLE);
                break;

            case TYPE_DISPLAY_WORKMATES_BY_RESTO:
                line_separator.setVisibility(View.GONE);
                break;
        }
    }

    private void configure_workmates_text(Workmate workmates, String type_display){
        String text;

        switch(type_display){

            case TYPE_DISPLAY_WORKMATES_LIST:
                    if(workmates!=null){
                        if(workmates.getChosen()!=null) {
                            if (workmates.getChosen()) {
                                text = workmates.getName() + " " + context.getResources().getString(R.string.is_eating_at) + " " + workmates.getResto_name();
                                workmates_text.setText(text);
                                workmates_text.setTextColor(Color.BLACK);
                                workmates_text.setTypeface(null, Typeface.NORMAL);
                            } else {
                                text = workmates.getName() + " " + context.getResources().getString(R.string.has_not_decided);
                                workmates_text.setText(text);
                                workmates_text.setTextColor(Color.DKGRAY);
                                workmates_text.setTypeface(null, Typeface.ITALIC);
                            }
                        }
                    }
                break;

            case TYPE_DISPLAY_WORKMATES_BY_RESTO:
                if(workmates!=null){
                    if(workmates.getChosen()!=null){
                        if(workmates.getChosen()) {
                            text = workmates.getName() + " " + context.getResources().getString(R.string.joining);
                            workmates_text.setText(text);
                        }
                    }
                }
                break;
        }
    }

    private void configure_picture_workmates(Context context,Workmate workmates){

        String photoUrl = workmates.getPhotoUrl();

        Glide.with(context)
                    .load(photoUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.round_person_black_24))
                    .apply(RequestOptions.circleCropTransform())
                    .into(picture_workmate);

    }
}
