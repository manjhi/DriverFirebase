package com.omninos.firstmobapplication;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Manjinder Singh on 15 , November , 2019
 */
public class Singlton {
    LatLng latLng;

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
