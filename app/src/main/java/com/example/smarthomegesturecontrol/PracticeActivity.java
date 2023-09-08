package com.example.smarthomegesturecontrol;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smarthomegesturecontrol.databinding.ActivityPracticeBinding;

public class PracticeActivity extends AppCompatActivity {
    private ActivityPracticeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPracticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // check for camera
        if (false == checkForCamera(this)) {
            Toast.makeText(this, "NO CAMERA", Toast.LENGTH_LONG);
        }

        // check for permissions
        if (false == hasCameraPermission()) {
            requestCameraPermission();
        }

    }

    private boolean checkForCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            return true;
        else
            return false;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[] {Manifest.permission.CAMERA},
                100
        );
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }
}