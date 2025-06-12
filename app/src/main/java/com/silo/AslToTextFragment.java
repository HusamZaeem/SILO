package com.silo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.silo.databinding.FragmentAslToTextBinding;
import com.silo.translation.LandmarkExtractor;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class AslToTextFragment extends Fragment {
    private static final String TAG = "AslToText";

    private FragmentAslToTextBinding b;
    private final String[] PERMS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private VideoCapture<Recorder> videoCapture;
    private Recording activeRecording;
    private CameraSelector lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
    private CountDownTimer timer;
    private File outFile;

    private LandmarkExtractor extractor;
    private ProgressBar spinner;

    private final ActivityResultLauncher<String[]> reqPerms =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    results -> {
                        boolean all = results.values().stream().allMatch(v -> v);
                        if (all) bindCameraAndPreview();
                        else Toast.makeText(requireContext(),
                                "Permissions denied",
                                Toast.LENGTH_SHORT).show();
                    });

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        b = FragmentAslToTextBinding.inflate(inflater, container, false);

        // Make edge‐to‐edge full‐screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().getWindow().setDecorFitsSystemWindows(false);
            requireActivity().getWindow().getInsetsController()
                    .hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        }

        extractor = new LandmarkExtractor(requireContext());

        b.btnLaunchCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), PERMS[0])
                    != PackageManager.PERMISSION_GRANTED) {
                reqPerms.launch(PERMS);
            } else {
                bindCameraAndPreview();
            }
        });

        return b.getRoot();
    }

    private void bindCameraAndPreview() {
        // Hide the launch icon, show live preview container
        b.recordPreviewContainer.setVisibility(View.GONE);
        b.fullCameraContainer.setVisibility(View.VISIBLE);
        b.videoView.setVisibility(View.GONE);

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(requireContext());
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                provider.unbindAll();

                // 1) Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(b.previewView.getSurfaceProvider());

                // 2) VideoCapture
                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HD))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                provider.bindToLifecycle(this,
                        lensFacing,
                        preview,
                        videoCapture);

                setupControls();

            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void setupControls() {
        // Record button: large red circle (your drawable)
        b.btnStart.setBackground(
                getResources().getDrawable(R.drawable.record_circle, null));
        b.btnStart.setImageDrawable(null);

        // Toggle start/stop
        b.btnStart.setOnClickListener(v -> {
            if (activeRecording == null) startRecording();
            else stopRecording();
        });

        // Flip camera
        b.btnSwitchCamera.setOnClickListener(v -> {
            lensFacing = (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA)
                    ? CameraSelector.DEFAULT_BACK_CAMERA
                    : CameraSelector.DEFAULT_FRONT_CAMERA;
            bindCameraAndPreview();
        });
    }

    private void startRecording() {
        outFile = new File(requireContext()
                .getExternalFilesDir(null),
                "rec_" + System.currentTimeMillis() + ".mp4");

        FileOutputOptions opts =
                new FileOutputOptions.Builder(outFile).build();

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Audio denied: record video-only
        }

        activeRecording = videoCapture.getOutput()
                .prepareRecording(requireContext(), opts)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(requireContext()), event -> {
                    if (event instanceof VideoRecordEvent.Finalize
                            && !((VideoRecordEvent.Finalize) event).hasError()) {
                        onRecordingFinished();
                    }
                });

        // Switch to “stop” square UI
        b.btnStart.setBackground(
                getResources().getDrawable(R.drawable.stop_square, null));
        b.btnStart.setImageDrawable(null);

        // Auto‐stop at 60s
        timer = new CountDownTimer(60_000, 1_000) {
            @Override public void onTick(long millis) {}
            @Override public void onFinish() { stopRecording(); }
        }.start();
    }

    private void stopRecording() {
        if (activeRecording != null) activeRecording.stop();
        if (timer != null) timer.cancel();
    }

    private void onRecordingFinished() {
        // Show a centered spinner
        spinner = new ProgressBar(requireContext());
        ViewGroup root = b.fullCameraContainer;
        root.addView(spinner);
        spinner.setIndeterminate(true);
        spinner.setX(root.getWidth() / 2f - 50);
        spinner.setY(root.getHeight() / 2f - 50);

        new Thread(() -> {
            try {
                MediaMetadataRetriever retr = new MediaMetadataRetriever();
                retr.setDataSource(outFile.getAbsolutePath());

                long durationMs = Long.parseLong(
                        Objects.requireNonNull(
                                retr.extractMetadata(
                                        MediaMetadataRetriever.METADATA_KEY_DURATION)));

                // Extract every 100ms
                for (long t = 0; t < durationMs; t += 100) {
                    Bitmap frame = retr.getFrameAtTime(
                            t * 1000,
                            MediaMetadataRetriever.OPTION_CLOSEST);
                    if (frame == null) continue;

                    // **SYNC** extraction + log
                    JSONObject json = extractor.detectSync(frame);
                    Log.d(TAG, "t=" + t + "ms → " + json.toString());

                    // Update partial text if you like
                    final String previewText = json.optString("hands") + " | "
                            + json.optString("faces");
                    requireActivity().runOnUiThread(() ->
                            b.tvAslToText.setText(previewText));

                    // (Optional) simulate some delay:
                    Thread.sleep(100);
                }

                retr.release();

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Back to UI: hide spinner, show playback
            requireActivity().runOnUiThread(() -> {
                root.removeView(spinner);
                b.fullCameraContainer.setVisibility(View.GONE);
                b.videoView.setVisibility(View.VISIBLE);
                b.videoView.setVideoURI(Uri.fromFile(outFile));
                b.videoView.start();
            });
        }).start();
    }
}
