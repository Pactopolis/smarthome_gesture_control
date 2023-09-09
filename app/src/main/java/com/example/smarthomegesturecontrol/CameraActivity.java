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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class CameraActivity extends AppCompatActivity {
    Recording recording = null;
    VideoCapture<Recorder> videoCapture = null;
    private PreviewView previewView;
    private Button recordButton;
    private String fileName;
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

        fileName = new SimpleDateFormat(
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

                        // get video uri
                        String[] fileList;
                        String finalPath = "";
                        final String videosPath = Environment.getExternalStorageDirectory() + "/Movies/CameraX-Video";
                        File videoFiles = new File(videosPath);
                        if (videoFiles.isDirectory()) {
                            fileList = videoFiles.list();
                            for (int i = 0; i < fileList.length; i++) {
                                if (0 == fileList[i].compareTo((fileName + ".mp4"))) {
                                    finalPath = videosPath + "/" + fileList[i];
                                    break;
                                }
                            }
                        }

                        new UploadVideo().execute(finalPath, "http://10.0.2.2:5000/api/v1/upload");
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
            String videoUri = params[0];
            String serverUrl = params[1];

            File sourceFile = new File(videoUri);
            if (!sourceFile.exists()) {
                System.out.println("captureVideo(): file does not exist");
                // TODO: what to do?
            }

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
                conn.setRequestProperty("uploaded_file", videoUri);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ videoUri + "\"" + lineEnd);
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
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}