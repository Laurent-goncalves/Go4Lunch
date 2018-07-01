package com.g.laurent.go4lunch.Models;

import java.util.List;

public interface CallbackMultiActivity {

    void message_error_API_request(String error);

    void configureViewPagerAndTabs(List<PlaceNearby> list_restos);

}
