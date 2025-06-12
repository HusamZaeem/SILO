package com.silo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.silo.databinding.FragmentAslToTextBinding;
import com.silo.translation.LandmarkExtractor;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        b = FragmentAslToTextBinding.inflate(inflater, container, false);

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
        b.recordPreviewContainer.setVisibility(View.GONE);
        b.fullCameraContainer.setVisibility(View.VISIBLE);
        b.videoView.setVisibility(View.GONE);

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(requireContext());
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                provider.unbindAll();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(b.previewView.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HD))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), image -> {
                    Bitmap frame = imageProxyToBitmap(image);
                    if (frame != null) {
                        JSONObject json = extractor.detectSync(frame);
                        boolean handsVisible = json.optJSONArray("hands") != null
                                && json.optJSONArray("hands").length() > 0;
                        boolean facesVisible = json.optJSONArray("faces") != null
                                && json.optJSONArray("faces").length() > 0;

                        requireActivity().runOnUiThread(() -> {
                            if (!handsVisible || !facesVisible) {
                                b.tvAslToText.setText("Make sure your hands and face are visible in the camera.");
                            } else {
                                b.tvAslToText.setText(""); // Clear message
                            }
                        });
                    }
                    image.close();
                });

                provider.bindToLifecycle(this,
                        lensFacing,
                        preview,
                        videoCapture,
                        analysis);

                setupControls();

            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void setupControls() {
        b.btnStart.setBackground(
                getResources().getDrawable(R.drawable.record_circle, null));
        b.btnStart.setImageDrawable(null);

        b.btnStart.setOnClickListener(v -> {
            if (activeRecording == null) startRecording();
            else stopRecording();
        });

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

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {


            return;
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

        b.btnStart.setBackground(
                getResources().getDrawable(R.drawable.stop_square, null));
        b.btnStart.setImageDrawable(null);

        timer = new CountDownTimer(60_000, 1_000) {
            @Override
            public void onTick(long millisUntilFinished) {}
            @Override
            public void onFinish() { stopRecording(); }
        }.start();
    }

    private void stopRecording() {
        if (activeRecording != null) activeRecording.stop();
        if (timer != null) timer.cancel();
    }

    private void onRecordingFinished() {
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

                for (long t = 0; t < durationMs; t += 100) {
                    Bitmap frame = retr.getFrameAtTime(
                            t * 1000,
                            MediaMetadataRetriever.OPTION_CLOSEST);
                    if (frame == null) continue;

                    JSONObject json = extractor.detectSync(frame);
                    Log.d(TAG, "Frame at " + t + "ms:\n" + json.toString(2)); // full pretty print

                    final String previewText = json.optString("hands") + " | "
                            + json.optString("faces");
                    requireActivity().runOnUiThread(() ->
                            b.tvAslToText.setText(previewText));

                    Thread.sleep(100);
                }

                retr.release();

            } catch (Exception e) {
                e.printStackTrace();
            }

            requireActivity().runOnUiThread(() -> {
                root.removeView(spinner);
                b.fullCameraContainer.setVisibility(View.GONE);
                b.videoView.setVisibility(View.VISIBLE);
                b.videoView.setVideoURI(Uri.fromFile(outFile));
                b.videoView.start();
            });
        }).start();
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
