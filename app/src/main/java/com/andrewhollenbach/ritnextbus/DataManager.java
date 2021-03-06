package com.andrewhollenbach.RITNextBus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DataManager {

    private static JSONObject data;

    public static String curRouteName = "The Province";
    public static ArrayList<String> routeNames;
    public static SimpleDateFormat printTimeFormat = new SimpleDateFormat("h:mm");

    public static void setData(JSONObject d) {
        data = d;
        try {
            setRouteNames();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getVersion() throws JSONException {
        return data.getString("version");
    }
    public static String getTimeFormat() {
        try {
            return data.getString("timeformat");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static JSONObject getDayMapping() throws JSONException {
        return data.getJSONObject("days");
    }
    public static JSONArray getRoutes() throws JSONException {
        return data.getJSONArray("routes");
    }

    private static void setRouteNames() throws JSONException {
        ArrayList<String> names = new ArrayList<String>();
        JSONArray routes = getRoutes();
        for(int i=0;i<routes.length();i++) {
            JSONObject route = routes.getJSONObject(i);
            names.add(route.getString("title"));
        }
        routeNames = names;
    }

    // TODO: is there a cleaner way to do this?
    public static ArrayList<String> getRoute(String direction, int today) throws JSONException {
        JSONArray routes = getRoutes();
        for(int i=0;i<routes.length();i++) {
            JSONObject route = routes.getJSONObject(i);
            if(route.getString("title").equals(curRouteName)) {
                JSONArray days = route.getJSONArray(direction);
                for(int j=0;j<days.length();j++) {
                    if(days.getJSONObject(j).getString("day").contains(Integer.toString(today))) {
                        return convertJSONArraytoArray(days.getJSONObject(j).getJSONArray("times"));
                    }
                }
            }
        }
        return null;
    }

    private static ArrayList<String> convertJSONArraytoArray(JSONArray jsonArray) throws JSONException {
        ArrayList<String> list = new ArrayList<String>();
        if (jsonArray != null) {
            int len = jsonArray.length();
            for (int i=0;i<len;i++){
                list.add(jsonArray.get(i).toString());
            }
        }
        return list;
    }

    public static Date getNext(String direction) throws JSONException, ParseException {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        ArrayList<String> stopTimes = getRoute(direction, today);

        Date curTime = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(getTimeFormat());
        for(int i=0;i<stopTimes.size();i++) {
            Date stopTime = format.parse(stopTimes.get(i));
            if(compareTimes(curTime,stopTime) < 0) {
                return stopTime;
            }
        }
        return format.parse("0001");
    }

    public static ArrayList<Date> getNextI(String direction, int iElems) throws JSONException, ParseException {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        ArrayList<String> stopTimes = getRoute(direction, today);

        Date curTime = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(getTimeFormat());
        for (int i = 0; i < stopTimes.size(); i++) {
            Date stopTime = format.parse(stopTimes.get(i));
            if (compareTimes(curTime, stopTime) < 0) {
                ArrayList<Date> stops = new ArrayList<Date>();
                for(int st=0;st<iElems;st++) {
                    if(i+st >= stopTimes.size()) return stops;

                    stops.add(format.parse(stopTimes.get(i + st)));
                }
                return stops;
            }
        }
        return null;
    }

    // Syntactic Sugar
    public static ArrayList<Date> getNext4(String direction) {
        try {
            return getNextI(direction,4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Author: http://stackoverflow.com/a/7676307/1227632
    public static int compareTimes(Date d1, Date d2)
    {
        int     t1;
        int     t2;

        t1 = (int) (d1.getTime() % (24*60*60*1000L));
        t2 = (int) (d2.getTime() % (24*60*60*1000L));
        return (t1 - t2);
    }

    public static Date getNextResidential() {
        try {
            return getNext("residentialRoutes");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date getNextAcademic() {
        try {
            return getNext("academicRoutes");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateRoutes() {
        // TODO: Ask API if up to date
        // TODO: If not, request latest version
        // TODO: Store latest version to data.json
    }

}
