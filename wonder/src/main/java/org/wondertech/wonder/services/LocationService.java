package org.wondertech.wonder.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;


public class LocationService extends IntentService {
    static private LinkedList<Location> lqueue = new LinkedList<Location>();
    static private ArrayList<Double> speeds = new ArrayList<Double>();
    static public int count = 0;
    static public double meanSpeed = 0;
    static public int res = 0;

    public LocationService(){
        super("LocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Location loc = (Location)intent.getExtras().get(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
        if (count >= 10){
            lqueue.remove();
        }
        else{
            count++;
        }
        double temp = getSpeed(loc);
        speeds.add(temp);
        Log.v("speed", Double.toString(temp));
        lqueue.add(loc);
        double total = 0;
        res = 0;
        for (double spd : speeds)
        {
            total += spd;
            if (spd > 5){
                res++;
            }

        }
        Log.v("res", Double.toString(res));
        meanSpeed = total / speeds.size();
        Log.v("meanSpeed", Double.toString(meanSpeed));
    }

    private double getSpeed(Location loc)
    {
        ListIterator<Location> listIterator = lqueue.listIterator();
        ArrayList<Double> sds = new ArrayList<Double>();
        while (listIterator.hasNext()) {
           sds.add(speedFrom(loc, listIterator.next()));
        }
        Collections.sort(sds);
        if (sds.size() > 2)
        {
            double totalSpeed = 0;
            for (int i = 1; i < sds.size() - 1; ++i)
            {
                totalSpeed += sds.get(i);
            }
            return totalSpeed / (sds.size() - 2);
        }
        return 0;
    }

    private double speedFrom(Location l1, Location l2){
       return 1000 * distFrom(l1, l2) / (l1.getTime() - l2.getTime());
    }


    private static double distFrom(Location l1, Location l2) {
        double lat1 = l1.getLatitude();
        double lng1 = l1.getLongitude();
        double lat2 = l2.getLatitude();
        double lng2 = l2.getLongitude();
        double earthRadius = 6371; //kilometers
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c * 1000;

        return dist;
    }
}
