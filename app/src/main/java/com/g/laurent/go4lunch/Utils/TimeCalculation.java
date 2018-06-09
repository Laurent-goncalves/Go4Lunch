package com.g.laurent.go4lunch.Utils;

import android.content.Context;

import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Period;
import com.g.laurent.go4lunch.Utils.DetailsPlace.OpeningHours;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimeCalculation {

    private Place_Nearby place_nearby;
    private int current_day;
    private Calendar calendar;
    private String current_hour;
    private String current_minute;
    private int current_time;
    private Context context;
    public TimeCalculation(Context context) {

        this.context=context;
        calendar = Calendar.getInstance();
        current_day = calendar.get(Calendar.DAY_OF_WEEK);
        current_hour= String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        current_minute= String.valueOf(calendar.get(Calendar.MINUTE));
        current_time = Integer.parseInt(current_hour + current_minute);
    }

    public String getTextOpeningHours(Place_Nearby place_nearby) {

        this.place_nearby=place_nearby;
        String text = null;

        if(place_nearby!=null){
            if(place_nearby.getOpeningHours()!=null){
                if(place_nearby.getOpeningHours().getPeriods()!=null){

                    List<Period> periodList = place_nearby.getOpeningHours().getPeriods();
                    text = getInformationAboutOpeningAndClosure(periodList,current_time,current_day-1);

                }
            }
        }
        return text;
    }

    public String getInformationAboutOpeningAndClosure(List<Period> periodList, int current_time, int current_day){

        String text = null;

        if(periodList!=null){

            List<Integer> Closure_timings = get_Closure_timings(current_day,periodList);
            List<Integer> Opening_timings = get_Opening_timings(current_day,periodList);

            // COMPARE TIMINGS WITH CURRENT TIME
            if(Closure_timings.size()>0){
                if(Closure_timings.size()==Opening_timings.size()){

                    Boolean during_opening_hours = false;

                    // check if the current_time is during the opening hours
                    for(int i = 0; i< Closure_timings.size(); i++) {
                        if(current_time < Closure_timings.get(i) && current_time >= Opening_timings.get(i))
                            during_opening_hours= true;
                    }

                    // if current time is not during opening hours
                    if(!during_opening_hours)
                        text = set_text_if_not_during_opening_hours(current_time,Closure_timings,Opening_timings);
                    else  // if during opening hours
                        text = set_text_if_during_opening_hours(current_time,Closure_timings);
                }
            }

        }

        return text;
    }

    private List<Integer> get_Opening_timings(int current_day, List<Period> periodList) {

        List<Integer> Opening_timings = new ArrayList<>();

        for(Period period : periodList) {
            if (period != null) {

                // GET CLOSURE TIMINGS
                if (period.getOpen() != null) {
                    if (period.getOpen().getDay() == current_day)
                        Opening_timings.add(Integer.parseInt(period.getOpen().getTime()));
                }
            }
        }

        return Opening_timings;
    }

    private List<Integer> get_Closure_timings(int current_day,List<Period> periodList){
        List<Integer> Closure_timings = new ArrayList<>();

        for(Period period : periodList) {
            if (period != null) {

                // GET CLOSURE TIMINGS
                if (period.getClose() != null) {
                    if (period.getClose().getDay() == current_day)
                        Closure_timings.add(Integer.parseInt(period.getClose().getTime()));
                }
            }
        }

        return Closure_timings;
    }

    private String set_text_if_not_during_opening_hours(int current_time, List<Integer> Closure_timings, List<Integer> Opening_timings){

        String text;

        Boolean after_last_closure = true;
        for(int i = 0; i< Closure_timings.size(); i++){
            if(current_time < Closure_timings.get(i))
                after_last_closure= false;
        }

        // if the current time is after last closure...
        if(after_last_closure)
            text = context.getResources().getString(R.string.closed_now);
        else { // if not, check what is the next time of opening

            // define the next opening
            int next_time_opening = Opening_timings.get(0);
            int shortest_timing = -1;

            for(int i = 0; i< Opening_timings.size(); i++){
                if(shortest_timing==-1){ // if not initialized
                    if(current_time <= Opening_timings.get(i)) {
                        next_time_opening = Opening_timings.get(i);
                        shortest_timing = Opening_timings.get(i) - current_time;
                    }
                } else if (current_time <= Opening_timings.get(i) && shortest_timing > Opening_timings.get(i) - current_time){
                    next_time_opening = Opening_timings.get(i);
                    shortest_timing = Opening_timings.get(i) - current_time;
                }
            }

            if(shortest_timing<30){
                text = context.getResources().getString(R.string.opening_in) + shortest_timing + " min";
            } else {
                text = context.getResources().getString(R.string.open_at) + " " + get_time_hour(next_time_opening);
            }
        }

        return text;
    }

    private String set_text_if_during_opening_hours(int current_time, List<Integer> Closure_timings){

        String text;

        // define the next closure
        int next_time_closing = Closure_timings.get(0);
        int shortest_timing = -1;

        for(int i = 0; i< Closure_timings.size(); i++){

            if(shortest_timing==-1){ // if not initialized
                if(current_time <= Closure_timings.get(i)) {
                    next_time_closing = Closure_timings.get(i);
                    shortest_timing = Closure_timings.get(i) - current_time;
                }
            } else if (current_time <= Closure_timings.get(i) && shortest_timing > Closure_timings.get(i) - current_time){
                next_time_closing = Closure_timings.get(i);
                shortest_timing = Closure_timings.get(i) - current_time;
            }
        }

        if(shortest_timing < 30){
            text = context.getResources().getString(R.string.closed_soon) + " " + shortest_timing + " min)";
        } else {
            text = context.getResources().getString(R.string.open_until) + " " + get_time_hour(next_time_closing);
        }

        return text;
    }

    private String get_time_hour(int time_to_convert){

        String text;
        if(time_to_convert>=1000){
            // set hour
            text = String.valueOf(time_to_convert).substring(0,2) + "h";

            // set minutes
            if(!String.valueOf(time_to_convert).substring(2,4).equals("00"))
                text=text+String.valueOf(time_to_convert).substring(2,4);

        } else {
            // set hour
            text = String.valueOf(time_to_convert).substring(0,1) + "h";

            // set minutes
            if(!String.valueOf(time_to_convert).substring(1,3).equals("00"))
                text=text+String.valueOf(time_to_convert).substring(1,3);
        }

        return text;
    }

    public void setCurrent_day(int current_day) {
        this.current_day = current_day;
    }

    public void setCurrent_time(int current_time) {
        this.current_time = current_time;
    }
}
