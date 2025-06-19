package com.silo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.silo.databinding.ActivityCameraBinding;
import com.silo.translation.LandmarkExtractor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.File;
import java.util.Arrays;

public class CameraActivity extends AppCompatActivity {
    private ActivityCameraBinding b;
    private VideoCapture<Recorder> videoCapture;
    private Recording activeRecording;
    private File outFile;
    private CountDownTimer timer;
    private LandmarkExtractor extractor;
    private ProgressBar spinner;
    private CameraSelector lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        extractor = new LandmarkExtractor(this);
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 10);

        b.btnSwitchCamera.setOnClickListener(v -> {
            if (activeRecording != null) return; // prevent switching while recording

            lensFacing = (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA)
                    ? CameraSelector.DEFAULT_BACK_CAMERA
                    : CameraSelector.DEFAULT_FRONT_CAMERA;

            startCamera();
        });


    }

    @Override
    public void onRequestPermissionsResult(int r, String[] perms, int[] res) {
        super.onRequestPermissionsResult(r, perms, res);
        if (res.length > 0 && Arrays.stream(res).allMatch(p -> p == PackageManager.PERMISSION_GRANTED)) {
            startCamera();
        } else {
            Toast.makeText(this, "Permissions required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider provider = ProcessCameraProvider.getInstance(this).get();
                provider.unbindAll();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(b.previewView.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                // ✅ Use current lensFacing selection
                provider.bindToLifecycle(this, lensFacing, preview, videoCapture);
                setupControls();
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void setupControls() {
        b.btnStart.setOnClickListener(v -> {
            if (activeRecording == null) startRecording();
            else stopRecording();
        });
    }

    private void startRecording() {
        outFile = new File(getExternalFilesDir(null), "rec_" + System.currentTimeMillis() + ".mp4");

        FileOutputOptions opts = new FileOutputOptions.Builder(outFile).build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        activeRecording = videoCapture.getOutput()
                .prepareRecording(this, opts)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), event -> {
                    if (event instanceof VideoRecordEvent.Finalize
                            && !((VideoRecordEvent.Finalize) event).hasError()) {
                        analyzeVideo();
                    }
                });

        b.btnStart.setImageResource(R.drawable.stop_square);

        timer = new CountDownTimer(60_000, 1_000) {
            public void onTick(long ms) {}
            public void onFinish() { stopRecording(); }
        }.start();
    }

    private void stopRecording() {
        if (activeRecording != null) activeRecording.stop();
        if (timer != null) timer.cancel();
    }

    private void analyzeVideo() {
        runOnUiThread(() -> {
            spinner = new ProgressBar(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(200, 200, Gravity.CENTER);
            b.getRoot().addView(spinner, params);
        });

        new Thread(() -> {
            JSONArray allFrames = new JSONArray();

            try {
                MediaMetadataRetriever retr = new MediaMetadataRetriever();
                retr.setDataSource(outFile.getAbsolutePath());

                long durationMs = Long.parseLong(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                for (long t = 0; t < durationMs; t += 100) {
                    Bitmap frame = retr.getFrameAtTime(t * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
                    if (frame == null) continue;

                    JSONObject landmarks = extractor.detectSync(frame);  // returns JSON with face, pose, hands
                    if (landmarks != null) {
                        allFrames.put(landmarks);
                    }
                }

                retr.release();
            } catch (Exception e) {
                e.printStackTrace();
            }



            if (allFrames.length() > 0) {
                try {
                    File jsonFile = new File(getExternalFilesDir(null), "landmarks_" + System.currentTimeMillis() + ".txt");
                    FileWriter writer = new FileWriter(jsonFile);
                    writer.write(allFrames.toString(2)); // Pretty format
                    writer.close();

                    Log.d("CameraActivity", "Saved landmarks JSON to " + jsonFile.getAbsolutePath());
                } catch (Exception e) {
                    Log.e("CameraActivity", "Error saving landmarks JSON", e);
                }
            } else {
                Log.d("CameraActivity", "No landmarks extracted.");
            }


            runOnUiThread(() -> {
                b.getRoot().removeView(spinner);

                if (allFrames.length() > 0) {
                    Intent resultIntent = new Intent();
                    // Only send the video path, no JSON landmarks in Intent extras
                    resultIntent.putExtra("video_path", outFile.getAbsolutePath());
                    setResult(Activity.RESULT_OK, resultIntent);
                } else {
                    Toast.makeText(this, "Failed to extract landmarks", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_CANCELED);
                }

                finish();
            });
        }).start();
    }


}
