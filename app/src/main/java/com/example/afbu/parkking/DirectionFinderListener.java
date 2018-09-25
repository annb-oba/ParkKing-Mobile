package com.example.afbu.parkking;

import java.util.List;

interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}