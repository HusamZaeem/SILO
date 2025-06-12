package com.silo.translation;

import android.content.Context;

import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;

public class CameraModule {
    public interface FrameListener {
        void onFrame(ImageProxy image, int rotationDegrees);
    }

    private final Context context;
    private final PreviewView previewView;
    private final Executor executor;
    private FrameListener frameListener;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

    public CameraModule(Context context, PreviewView previewView, Executor executor) {
        this.context = context;
        this.previewView = previewView;
        this.executor = executor;
    }

    public void setFrameListener(FrameListener listener) {
        this.frameListener = listener;
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    public void switchCamera() {
        cameraSelector = (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
                ? CameraSelector.DEFAULT_BACK_CAMERA
                : CameraSelector.DEFAULT_FRONT_CAMERA;
        bindCamera();
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    public void start() {
        bindCamera();
    }

    public void stop() {
        try {
            ProcessCameraProvider.getInstance(context).get().unbindAll();
        } catch (Exception ignored) {}
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void bindCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(context);

        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();
                cameraProvider.unbindAll();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(executor, image -> {
                    if (frameListener != null && image.getImage() != null) {
                        frameListener.onFrame(image, image.getImageInfo().getRotationDegrees());
                        // NOTE: Do NOT close image here; LandmarkExtractor handles it
                    } else {
                        image.close(); // Safe close if not processed
                    }
                });

                LifecycleOwner lifecycleOwner = (LifecycleOwner) previewView.getContext();

                cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        analysis
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }
}
