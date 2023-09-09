package com.example.smarthomegesturecontrol;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

public class GesturesTracker {
    private static GestureMeta[] gestureObjList;
    
    GesturesTracker(String url) {
        gestureObjList = new GestureMeta[] {
            new GestureMeta(
                    "Turn On Lights",
                    url + "h_light_on",
                    "LightOn"),
            new GestureMeta(
                    "Turn Off Lights",
                    url + "h_light_off",
                    "LightOff"),
            new GestureMeta(
                    "Turn On Fan",
                    url + "h_fan_on",
                    "FanOn"),
            new GestureMeta(
                    "Turn Off Fan",
                    url + "h_fan_off",
                    "FanOff"),
            new GestureMeta(
                    "Increase Fan Speed",
                    url  + "h_increase_fan_speed"
                    ,"FanUp"),
            new GestureMeta(
                    "Decrease Fan Speed",
                    url  + "h_decrease_fan_speed"
                    ,"FanDown"),
            new GestureMeta(
                    "Set Thermostat",
                    url  + "h_set_thermostat"
                    ,"SetThermo"),
            new GestureMeta(
                    "Number \'0\'",
                    url  + "h0"
                    ,"Num0"),
            new GestureMeta(
                    "Number \'1\'",
                    url  + "h1"
                    ,"Num1"),
            new GestureMeta(
                    "Number \'2\'",
                    url  + "h2"
                    ,"Num2"),
            new GestureMeta(
                    "Number \'3\'",
                    url  + "h3"
                    ,"Num3"),
            new GestureMeta(
                    "Number \'4\'",
                    url  + "h4"
                    ,"Num4"),
            new GestureMeta(
                    "Number \'5\'",
                    url  + "h5"
                    ,"Num5"),
            new GestureMeta(
                    "Number \'6\'",
                    url  + "h6"
                    ,"Num6"),
            new GestureMeta(
                    "Number \'7\'",
                    url  + "h7"
                    ,"Num7"),
            new GestureMeta(
                    "Number \'8\'",
                    url  + "h8"
                    ,"Num8"),
            new GestureMeta(
                    "Number \'9\'",
                    url  + "h9"
                    ,"Num9")
        };
    }

    private class GestureMeta {
        public String key;
        public String url;
        public String name;
        public int count;

        GestureMeta(String key, String url, String name) {
            this.key = key;
            this.url = url;
            this.name = name;
            count = 0;
        }
    };

    private GestureMeta findObj(String key) {
        for (GestureMeta obj : gestureObjList) {
            if (0 == key.compareTo(obj.key)) return obj;
        }
        return null;
    }

    public String getName(String key) {
        GestureMeta obj = findObj(key);
        if (obj != null) return obj.name;
        else return "";
    }

    public String getUrl(String key) {
        GestureMeta obj = findObj(key);
        if (obj != null) return obj.url;
        else return "";
    }

    public int getCount(String key) {
        GestureMeta obj = findObj(key);
        if (obj != null) return obj.count;
        else return -1;
    }

    public void addCount(String key) {
        for (GestureMeta obj : gestureObjList) {
            if (0 == key.compareTo(obj.key)) {
                obj.count += 1;
            }
        }
    }
}
