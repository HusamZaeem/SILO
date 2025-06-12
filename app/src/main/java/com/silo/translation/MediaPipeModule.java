package com.silo.translation;

//import android.content.Context;
//import android.graphics.Bitmap;
//import android.util.Log;
//
//import com.google.mediapipe.formats.proto.LandmarkProto;
//import com.google.mediapipe.solutioncore.ResultListener;
//import com.google.mediapipe.solutions.facemesh.FaceMesh;
//import com.google.mediapipe.solutions.facemesh.FaceMeshResult;
//import com.google.mediapipe.solutions.facemesh.FaceMeshOptions;
//import com.google.mediapipe.solutions.hands.Hands;
//import com.google.mediapipe.solutions.hands.HandsOptions;
//import com.google.mediapipe.solutions.hands.HandsResult;
//
//import java.util.ArrayList;
//import java.util.List;

public class MediaPipeModule {
//    public interface LandmarksListener {
//        void onLandmarks(
//                List<LandmarkProto.NormalizedLandmarkList> hands,
//                List<LandmarkProto.NormalizedLandmarkList> faces);
//    }
//
//    private final Hands hands;
//    private final FaceMesh faceMesh;
//    private LandmarksListener listener;
//
//    private List<LandmarkProto.NormalizedLandmarkList> latestHands = new ArrayList<>();
//    private List<LandmarkProto.NormalizedLandmarkList> latestFaces = new ArrayList<>();
//
//    public MediaPipeModule(Context ctx) {
//        hands = new Hands(
//                ctx,
//                HandsOptions.builder()
//                        .setStaticImageMode(false)
//                        .setMaxNumHands(2)
//                        .setRunOnGpu(true)
//                        .build()
//        );
//        faceMesh = new FaceMesh(
//                ctx,
//                FaceMeshOptions.builder()
//                        .setStaticImageMode(false)
//                        .setRunOnGpu(true)
//                        .build()
//        );
//
//        hands.setResultListener(new ResultListener<HandsResult>() {
//            @Override
//            public void run(HandsResult result) {
//                latestHands = result.multiHandLandmarks();
//                dispatchIfReady();
//            }
//        });
//
//        faceMesh.setResultListener(new ResultListener<FaceMeshResult>() {
//            @Override
//            public void run(FaceMeshResult result) {
//                latestFaces = result.multiFaceLandmarks();
//                dispatchIfReady();
//            }
//        });
//
//        hands.setErrorListener((msg, e) ->
//                Log.e("MediaPipeModule", "Hands Error: " + msg, e));
//        faceMesh.setErrorListener((msg, e) ->
//                Log.e("MediaPipeModule", "FaceMesh Error: " + msg, e));
//    }
//
//    public void setListener(LandmarksListener l) {
//        this.listener = l;
//    }
//
//    /** Process one frame for both hands and face */
//    public void processFrame(Bitmap bmp) {
//        // Send to both processors
//        hands.send(bmp);
//        faceMesh.send(bmp);
//    }
//
//    private void dispatchIfReady() {
//        if (listener != null) {
//            listener.onLandmarks(latestHands, latestFaces);
//        }
//    }
//
//    public void shutdown() {
//        hands.close();
//        faceMesh.close();
//    }
}
