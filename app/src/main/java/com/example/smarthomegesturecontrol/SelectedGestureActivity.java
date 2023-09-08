package com.example.smarthomegesturecontrol;

import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.smarthomegesturecontrol.databinding.ActivitySelectedGestureBinding;

public class SelectedGestureActivity extends AppCompatActivity {
    private ActivitySelectedGestureBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: what is this?
        binding = ActivitySelectedGestureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle bundle = getIntent().getExtras();
        String gesture = bundle.getString("gesture");

        TextView text = (TextView) findViewById(R.id.textView2);
        text.setText(gesture);
    }
}