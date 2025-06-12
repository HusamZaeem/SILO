package com.silo.translation;

import com.google.gson.Gson;
import com.google.mediapipe.formats.proto.LandmarkProto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LandmarkJsonConverter {
    private static final Gson GSON = new Gson();

    public static String toJson(
            List<LandmarkProto.NormalizedLandmarkList> hands,
            List<LandmarkProto.NormalizedLandmarkList> faces) {

        Map<String,Object> map = new HashMap<>();
        map.put("hands", hands);
        map.put("faces", faces);
        return GSON.toJson(map);
    }
}