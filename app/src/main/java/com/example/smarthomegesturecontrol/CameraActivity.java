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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    Bundle bundle;
    String gestureName;
    String userName;

    Recording recording = null;
    VideoCapture<Recorder> videoCapture = null;
    private PreviewView previewView;
    private Button recordButton;
    private Button uploadButton;
    private TextView countdownTextView;
    private String fileName;
    private String filePath;
    private final String serverPath = "http://10.0.2.2:5000/api/v1/upload";
    final int cameraFacing = CameraSelector.LENS_FACING_FRONT;

    private final int maxRecordingInterval = 5000; // 5 seconds
    private final int recordingInterval = 1000;
    private int recordingCount;
    private Handler recordingHandle = new Handler();
    private Runnable recordingRunnable = new Runnable() {
        @Override
        public void run() {
            if (recordingCount >= maxRecordingInterval) {
                System.out.println("captureVideo(): DONE RECORDING!");
                countdownTextView.setVisibility(TextView.INVISIBLE);
                recording.stop();
            }
            else {
                System.out.println("captureVideo(): IN RUNNABLE: " + Integer.toString(recordingCount));
                countdownTextView.setText(Integer.toString((maxRecordingInterval - recordingCount) / 1000));
                recordingCount += recordingInterval;
                recordingHandle.postDelayed(recordingRunnable, recordingInterval);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("cam_act: onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        bundle = getIntent().getExtras();
        gestureName = bundle.getString("gesture");
        userName = bundle.getString("name");

        previewView = findViewById(R.id.previewView);

        countdownTextView = findViewById(R.id.counterTextView);
        countdownTextView.setVisibility(Button.INVISIBLE);
        countdownTextView.setText(Integer.toString(maxRecordingInterval / 1000));

        uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setVisibility(Button.INVISIBLE);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("cam_act: upload OnClick()");

                new UploadVideo().execute();

                Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        recordButton = (Button) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("cam_act: record OnClick()");

                recordButton.setVisibility(Button.INVISIBLE);
                countdownTextView.setVisibility(Button.VISIBLE);
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
        System.out.println("cam_act: startCamera()");

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
        System.out.println("cam_act: captureVideo()");

        Recording rec = recording;
        if (rec != null) {
            rec.stop();
            recording = null;
            System.out.println("captureVideo(): Recording obj is null");
            return;
        }

        // increment cache
        int currentCount = this.getSharedPreferences(
                "constraint_cache",
                this.MODE_PRIVATE
        ).getInt(gestureName, 1);

        SharedPreferences.Editor editor = this.getSharedPreferences(
                "constraint_cache",
                this.MODE_PRIVATE
        ).edit();
        editor.putInt(gestureName, currentCount + 1);
        editor.commit();

        fileName = gestureName + "_PRACTICE_" + currentCount + "_" + userName;

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

                        // get video uri
                        String[] fileList;
                        final String videosPath = Environment.getExternalStorageDirectory() + "/Movies/CameraX-Video";
                        File videoFiles = new File(videosPath);
                        if (videoFiles.isDirectory()) {
                            fileList = videoFiles.list();
                            for (int i = 0; i < fileList.length; i++) {
                                if (0 == fileList[i].compareTo((fileName + ".mp4"))) {
                                    filePath = videosPath + "/" + fileList[i];
                                    break;
                                }
                            }
                        }
                        uploadButton.setVisibility(View.VISIBLE);
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

    public class UploadVideo extends AsyncTask<String, Void, Void> {
        @Override
        public Void doInBackground(String ...params) {
            System.out.println("cam_act: UploadVideo doInBackground()");

            String videoUri = filePath;
            String serverUrl = serverPath;
            String fullFileName = fileName + ".mp4";

            File sourceFile = new File(videoUri);
            if (!sourceFile.exists()) {
                System.out.println("captureVideo(): file does not exist");
            }
            else {
                try {
                    HttpURLConnection conn = null;
                    DataOutputStream dos = null;
                    DataInputStream inStream = null;

                    String lineEnd = "\r\n";
                    String twoHyphens = "--";
                    String boundary = "*****";

                    int bytesRead, bytesAvailable, bufferSize;
                    byte[] buffer;
                    final int maxBufferSize = 1 * 1024 * 1024;
                    String responseFromServer = "";

                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(serverUrl);

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fullFileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fullFileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();
                    System.out.println("captureVideo(): serverResponseCode --> " + serverResponseCode);
                    System.out.println("captureVideo(): serverResponseMessage --> " + serverResponseMessage);

                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        System.out.println("captureVideo(): line read --> " + line);
                    }
                    rd.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }
}