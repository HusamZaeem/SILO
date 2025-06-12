package com.silo.translation;

//import android.content.Context;
//import android.graphics.Bitmap;
//import android.os.Handler;
//import android.os.Looper;
//
//import androidx.annotation.MainThread;
//import androidx.camera.core.ImageProxy;
//import androidx.camera.view.PreviewView;
//
//import com.google.mediapipe.formats.proto.LandmarkProto;
//
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * Coordinates Camera → MediaPipe → JSON → AI → UI-callback,
// * all off the UI thread except the final onTranslation().
// */
public class AslTranslationEngine {
//    public interface Listener {
//        void onTranslation(String text);
//        void onLightingIssue(float lum);
//        void onNoLandmarks();
//    }
//
//    private final CameraModule camera;
//    private final MediaPipeModule mp;
////    private final AiTranslator ai;
//    private final Listener listener;
//
//    public AslTranslationEngine(
//            Context ctx,
//            PreviewView preview,
//            Listener listener) {
//        this.listener = listener;
//        ExecutorService camEx = Executors.newSingleThreadExecutor();
//        camera = new CameraModule(ctx, preview, camEx);
//        mp = new MediaPipeModule(ctx);
////        ai = new AiTranslator(ctx);
//
//        camera.setFrameListener(this::onFrame);
//        mp.setListener(this::onLandmarks);
//    }
//
//    /** Start capturing & translating */
//    public void start() {
//        camera.start();
//    }
//
//    /** Stop everything */
//    public void stop() {
//        camera.stop();
//        mp.shutdown();
////        ai.shutdown();
//    }
//
//    /** Switch front/back */
//    public void switchCamera() {
//        camera.switchCamera();
//    }
//
//    /** Called on camera-analysis thread */
//    private void onFrame(ImageProxy img, int rot) {
//        // 1) quick luminance check
//        float lum = ImageUtils.computeLuminance(img);
//        if (lum < 0.2f || lum > 0.8f) {
//            runOnMain(() -> listener.onLightingIssue(lum));
//            img.close();
//            return;
//        }
//        // 2) to Bitmap
//        Bitmap bmp = ImageUtils.imageProxyToBitmap(img);
//        img.close();
//
//        // 3) feed into MediaPipe
//        mp.processFrame(bmp);
//    }
//
//    /** Called when MP has new landmarks (MP thread) */
//    private void onLandmarks(
//            List<LandmarkProto.NormalizedLandmarkList> hands,
//            List<LandmarkProto.NormalizedLandmarkList> faces) {
//
//        if (hands.isEmpty() && faces.isEmpty()) {
//            runOnMain(listener::onNoLandmarks);
//            return;
//        }
//        // 4) to JSON + AI
//        String json = LandmarkJsonConverter.toJson(hands, faces);
////        ai.translate(json, text -> runOnMain(() -> listener.onTranslation(text)));
//    }
//
//    private void runOnMain(Runnable r) {
//        new Handler(Looper.getMainLooper()).post(r);
//    }
}