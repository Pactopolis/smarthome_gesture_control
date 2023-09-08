package com.example.smarthomegesturecontrol;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smarthomegesturecontrol.databinding.ActivityPracticeBinding;

public class PracticeActivity extends AppCompatActivity {
    private ActivityPracticeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPracticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}