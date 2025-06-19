package com.silo.translation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import androidx.annotation.NonNull;
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
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

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
    private static final String POSE_MODEL = "pose_landmarker_lite.task";

    private final HandLandmarker handLandmarker;
    private final FaceLandmarker faceLandmarker;
    private final PoseLandmarker poseLandmarker;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LandmarkExtractor(Context context) {
        try {
            // Hand
            BaseOptions handBase = BaseOptions.builder().setModelAssetPath(HAND_MODEL).build();
            HandLandmarker.HandLandmarkerOptions handOpts = HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(handBase)
                    .setRunningMode(RunningMode.IMAGE)
                    .build();
            handLandmarker = HandLandmarker.createFromOptions(context, handOpts);

            // Face
            BaseOptions faceBase = BaseOptions.builder().setModelAssetPath(FACE_MODEL).build();
            FaceLandmarker.FaceLandmarkerOptions faceOpts = FaceLandmarker.FaceLandmarkerOptions.builder()
                    .setBaseOptions(faceBase)
                    .setRunningMode(RunningMode.IMAGE)
                    .build();
            faceLandmarker = FaceLandmarker.createFromOptions(context, faceOpts);

            // Pose
            BaseOptions poseBase = BaseOptions.builder().setModelAssetPath(POSE_MODEL).build();
            PoseLandmarker.PoseLandmarkerOptions poseOpts = PoseLandmarker.PoseLandmarkerOptions.builder()
                    .setBaseOptions(poseBase)
                    .setRunningMode(RunningMode.IMAGE)
                    .build();
            poseLandmarker = PoseLandmarker.createFromOptions(context, poseOpts);

        } catch (Exception e) {
            throw new RuntimeException("Landmarker initialization failed", e);
        }
    }

    /** Synchronous detection on a Bitmap. */
    public JSONObject detectSync(@NonNull Bitmap bitmap) {
        try {

            // Ensure bitmap is in ARGB_8888 format
            if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            }

            MPImage mpImg = new BitmapImageBuilder(bitmap).build();

            HandLandmarkerResult handRes = handLandmarker.detect(mpImg);
            FaceLandmarkerResult faceRes = faceLandmarker.detect(mpImg);
            PoseLandmarkerResult poseRes = poseLandmarker.detect(mpImg);

            JSONObject frame = new JSONObject();

            List<List<NormalizedLandmark>> hands = handRes.landmarks();
            if (hands.size() > 0)
                frame.put("right_hand", landmarksToArray(hands.get(0)));
            if (hands.size() > 1)
                frame.put("left_hand", landmarksToArray(hands.get(1)));

            List<List<NormalizedLandmark>> faces = faceRes.faceLandmarks();
            if (!faces.isEmpty())
                frame.put("face", landmarksToArray(faces.get(0)));

            List<NormalizedLandmark> poses = poseRes.landmarks().isEmpty() ? null : poseRes.landmarks().get(0);
            if (poses != null)
                frame.put("pose", landmarksToArray(poses));

//            JSONArray frames = new JSONArray();
//            frames.put(frame);
//            JSONObject out = new JSONObject();
//            out.put("frames", frames);
            return frame;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Converts a list of landmarks into a JSON array of [x, y, z] arrays. */
    private static JSONArray landmarksToArray(List<NormalizedLandmark> list) throws JSONException {
        JSONArray arr = new JSONArray();
        for (NormalizedLandmark lm : list) {
            JSONArray point = new JSONArray();
            point.put(lm.x());
            point.put(lm.y());
            point.put(lm.z());
            arr.put(point);
        }
        return arr;
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
