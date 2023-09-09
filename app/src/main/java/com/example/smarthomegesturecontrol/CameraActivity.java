package com.example.smarthomegesturecontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class CameraActivity extends AppCompatActivity {
    ExecutorService service;
    Recording recording = null;
    VideoCapture<Recorder> videoCapture = null;
    private PreviewView previewView;
    private Button recordButton;
    final int cameraFacing = CameraSelector.LENS_FACING_FRONT;

    // TODO: we should probably make this a separate service with its own class
    private final int maxRecordingInterval = 5000; // 5 seconds
    private final int recordingInterval = 1100;
    private int recordingCount;
    private Handler recordingHandle = new Handler();
    private Runnable recordingRunnable = new Runnable() {
        @Override
        public void run() {
            if (recordingCount >= maxRecordingInterval) {
                System.out.println("captureVideo(): DONE RECORDING!");
                recording.stop();
            }
            else {
                System.out.println("captureVideo(): IN RUNNABLE: " + Integer.toString(recordingCount));
                recordingCount += recordingInterval;
                recordingHandle.postDelayed(recordingRunnable, recordingInterval);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);

        recordButton = (Button) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // record
                captureVideo();

                // Begin video capture
                recordingCount = 0;
                recordingHandle.postDelayed(recordingRunnable, 0);
            }
        });

        startCamera();
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> processCameraProvider
                = ProcessCameraProvider.getInstance(CameraActivity.this);

        processCameraProvider.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = processCameraProvider.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                cameraProvider.unbindAll();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        videoCapture
                );
            }
            catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(CameraActivity.this));
    }

    public void captureVideo() {
        Recording rec = recording;
        if (rec != null) {
            rec.stop();
            recording = null;
            System.out.println("captureVideo(): Recording obj is null");
            return;
        }

        String fileName = new SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS",
                Locale.getDefault()).format(System.currentTimeMillis());

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(
                getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();

        recording = videoCapture.getOutput()
                .prepareRecording(CameraActivity.this, options)
                .start(ContextCompat.getMainExecutor(CameraActivity.this), videoRecordEvent -> {

                if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                    System.out.println("captureVideo(): Video finalized!");
                    if (false == ((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                        System.out.println("captureVideo(): Video captured!");
                    }
                    else {
                        System.out.println("captureVideo(): Recording obj is null");
                        recording.close();
                        recording = null;
                        String msg = "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                        System.out.println("captureVideo(): " + msg);
                    }
                }
        });
    }
}