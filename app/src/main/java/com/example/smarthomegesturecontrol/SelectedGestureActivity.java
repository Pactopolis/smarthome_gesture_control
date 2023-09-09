package com.example.smarthomegesturecontrol;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.smarthomegesturecontrol.databinding.ActivitySelectedGestureBinding;

import java.util.HashMap;
import java.util.Map;

public class SelectedGestureActivity extends AppCompatActivity {
    private ActivitySelectedGestureBinding binding;


    private class GestureMeta {
         public String url;
         public String name;
         public int count;

         GestureMeta(String url, String name) {
             this.url = url;
             this.name = name;
             count = 0;
         }
    };
    Map<String, GestureMeta> gestureMap = new HashMap<String, GestureMeta>();
    String gestureUrlBase = "android.resource://com.example.smarthomegesturecontrol/raw/";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: what is this?
        binding = ActivitySelectedGestureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TODO: this should be a static map instead. not sure what the best way to do that is in android
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[1],
                       new GestureMeta(gestureUrlBase + "h_light_on","LightOn"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[2],
                       new GestureMeta(gestureUrlBase + "h_light_off","LightOff"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[3],
                       new GestureMeta(gestureUrlBase + "h_fan_on","FanOn"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[4],
                       new GestureMeta(gestureUrlBase + "h_fan_off","FanOff"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[5],
                       new GestureMeta(gestureUrlBase + "h_increase_fan_speed","FanUp"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[6],
                       new GestureMeta(gestureUrlBase + "h_decrease_fan_speed","FanDown"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[7],
                       new GestureMeta(gestureUrlBase + "h_set_thermostat","SetThermo"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[8],
                       new GestureMeta(gestureUrlBase + "h0","Num0"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[9],
                       new GestureMeta(gestureUrlBase + "h1","Num1"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[10],
                       new GestureMeta(gestureUrlBase + "h2","Num2"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[11],
                       new GestureMeta(gestureUrlBase + "h3","Num3"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[12],
                       new GestureMeta(gestureUrlBase + "h4","Num4"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[13],
                       new GestureMeta(gestureUrlBase + "h5","Num5"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[14],
                       new GestureMeta(gestureUrlBase + "h6","Num6"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[15],
                       new GestureMeta(gestureUrlBase + "h7","Num7"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[16],
                       new GestureMeta(gestureUrlBase + "h8","Num8"));
        gestureMap.put(getResources().getStringArray(R.array.gesture_list)[17],
                       new GestureMeta(gestureUrlBase + "h9","Num9"));

        Bundle bundle = getIntent().getExtras();
        String gesture = bundle.getString("gesture");

        // set up video view
        VideoView videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVideoPath(gestureMap.get(gesture).url);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.start();
            }
        });

        // set up buttons
        Button practiceButton = (Button) findViewById(R.id.button2);
        practiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoView.stopPlayback();

                Bundle bundle = new Bundle();
                bundle.putString("gesture", gestureMap.get(gesture).name);
                // TODO: don't hardcode the name
                bundle.putString("name", "Palmasani");
                gestureMap.get(gesture).count++;
                bundle.putInt("count", gestureMap.get(gesture).count);

                Intent intent = new Intent(SelectedGestureActivity.this, PracticeActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        videoView.requestFocus();
        videoView.start();
    }
}