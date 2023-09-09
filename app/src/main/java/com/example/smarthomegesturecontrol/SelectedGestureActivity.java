package com.example.smarthomegesturecontrol;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.smarthomegesturecontrol.databinding.ActivitySelectedGestureBinding;

import com.example.smarthomegesturecontrol.GesturesTracker;

public class SelectedGestureActivity extends AppCompatActivity {
    private ActivitySelectedGestureBinding binding;

    static GesturesTracker gesturesTracker = new GesturesTracker("android.resource://com.example.smarthomegesturecontrol/raw/");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: what is this?
        binding = ActivitySelectedGestureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle bundle = getIntent().getExtras();
        String gesture = bundle.getString("gesture");

        // set up video view
        VideoView videoView = (VideoView) findViewById(R.id.videoView);
        String path = gesturesTracker.getUrl(gesture);
        videoView.setVideoPath(gesturesTracker.getUrl(gesture));

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
                bundle.putString("gesture", gesturesTracker.getName(gesture));
                // TODO: don't hardcode the name
                bundle.putString("name", "Palmasani");
                gesturesTracker.addCount(gesture);
                bundle.putInt("count", gesturesTracker.getCount(gesture));

                Intent intent = new Intent(SelectedGestureActivity.this, PracticeActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        videoView.requestFocus();
        videoView.start();
    }
}