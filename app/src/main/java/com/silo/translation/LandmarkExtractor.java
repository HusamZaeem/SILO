package com.silo.translation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LandmarkExtractor {
    private static final String HAND_MODEL = "hand_landmarker.task";
    private static final String FACE_MODEL = "face_landmarker.task";

    private final HandLandmarker handLandmarker;
    private final FaceLandmarker faceLandmarker;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LandmarkExtractor(Context context) {
        try {
            // 1) Hand Landmarker
            BaseOptions handBase = BaseOptions.builder()
                    .setModelAssetPath(HAND_MODEL)
                    .build();
            HandLandmarker.HandLandmarkerOptions handOpts = HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(handBase)
                    .setRunningMode(RunningMode.IMAGE)     // IMAGE for sync; switch to VIDEO or LIVE_STREAM as needed
                    .build();
            handLandmarker = HandLandmarker.createFromOptions(context, handOpts);

            // 2) Face Landmarker
            BaseOptions faceBase = BaseOptions.builder()
                    .setModelAssetPath(FACE_MODEL)
                    .build();
            FaceLandmarker.FaceLandmarkerOptions faceOpts = FaceLandmarker.FaceLandmarkerOptions.builder()
                    .setBaseOptions(faceBase)
                    .setRunningMode(RunningMode.IMAGE)
                    .build();
            faceLandmarker = FaceLandmarker.createFromOptions(context, faceOpts);

        } catch (Exception e) {
            throw new RuntimeException("Landmarker initialization failed", e);
        }
    }

    /**
     * Asynchronous per-frame extraction from CameraX ImageProxy.
     * You MUST close the ImageProxy yourself in the callback (we do it here).
     */
    @OptIn(markerClass = ExperimentalGetImage.class)
    public void detectAsync(@NonNull ImageProxy image, int rotationDegrees, ResultCallback cb) {
        executor.execute(() -> {
            JSONObject out = null;
            try {
                if (image.getImage() == null) {
                    cb.onResult(null);
                    return;
                }
                Bitmap bmp = toBitmap(image);
                Matrix m = new Matrix();
                m.postRotate(rotationDegrees);
                Bitmap rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);

                MPImage mpImg = new BitmapImageBuilder(rotated).build();

                HandLandmarkerResult hr = handLandmarker.detect(mpImg);
                FaceLandmarkerResult fr = faceLandmarker.detect(mpImg);

                out = new JSONObject();
                out.put("hands",  toJson(hr.landmarks()));
                out.put("faces", toJson(fr.faceLandmarks()));

            } catch (Exception e) {
                cb.onError(e);
            } finally {
                image.close();
                cb.onResult(out);
            }
        });
    }

    /**
     * Synchronous detection on a standalone Bitmap (e.g. video frames).
     */
    public JSONObject detectSync(@NonNull Bitmap bitmap) {
        try {
            MPImage mpImg = new BitmapImageBuilder(bitmap).build();
            HandLandmarkerResult hr = handLandmarker.detect(mpImg);
            FaceLandmarkerResult fr = faceLandmarker.detect(mpImg);

            JSONObject out = new JSONObject();
            out.put("hands",  toJson(hr.landmarks()));
            out.put("faces", toJson(fr.faceLandmarks()));
            return out;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ─── UTILITY ─────────────────────────────────────────────────────────────────

    private static JSONArray toJson(List<List<NormalizedLandmark>> all) throws JSONException {
        JSONArray outer = new JSONArray();
        for (List<NormalizedLandmark> list : all) {
            JSONArray arr = new JSONArray();
            for (NormalizedLandmark lm : list) {
                JSONObject pt = new JSONObject();
                pt.put("x", lm.x());
                pt.put("y", lm.y());
                pt.put("z", lm.z());
                arr.put(pt);
            }
            outer.put(arr);
        }
        return outer;
    }

    /** Converts ImageProxy to Bitmap (YUV → JPEG → Bitmap). */
    private Bitmap toBitmap(ImageProxy ip) {
        byte[] nv21 = yuvToNv21(ip);
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, ip.getWidth(), ip.getHeight(), null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, ip.getWidth(), ip.getHeight()), 100, baos);
        byte[] jpeg = baos.toByteArray();
        return BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
    }

    /** Pulls out Y, U, V planes and re-interleaves into NV21 byte[]. */
    private static byte[] yuvToNv21(ImageProxy ip) {
        ImageProxy.PlaneProxy[] p = ip.getPlanes();
        ByteBuffer yBuf = p[0].getBuffer(), uBuf = p[1].getBuffer(), vBuf = p[2].getBuffer();
        int ySz = yBuf.remaining(), uSz = uBuf.remaining(), vSz = vBuf.remaining();
        byte[] out = new byte[ySz + uSz + vSz];
        yBuf.get(out, 0, ySz);
        vBuf.get(out, ySz, vSz);
        uBuf.get(out, ySz + vSz, uSz);
        return out;
    }

    /** Callback for async results. */
    public interface ResultCallback {
        void onResult(JSONObject landmarks);
        default void onError(Exception e) { e.printStackTrace(); }
    }
}
