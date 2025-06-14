package com.silo;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.reflect.Method;

public class UnityUtils {


    public static void attachUnityView(Activity hostActivity, FrameLayout container) {
        try {
            Class<?> unityPlayerClass = Class.forName("com.unity3d.player.UnityPlayer");
            Object unityActivity = unityPlayerClass.getField("currentActivity").get(null);

            if (!(unityActivity instanceof Activity)) return;

            // Try to get UnityPlayer instance (optional, if view is available)
            Object unityPlayer = unityPlayerClass.getDeclaredField("mUnityPlayer").get(unityActivity);
            Method getViewMethod = unityPlayer.getClass().getMethod("getView");
            View unityView = (View) getViewMethod.invoke(unityPlayer);

            if (unityView == null) return;

            // Detach if needed
            ViewGroup parent = (ViewGroup) unityView.getParent();
            if (parent != null && parent != container) {
                parent.removeView(unityView);
            }

            if (unityView.getParent() == null) {
                container.addView(unityView, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /**
     * Detaches the Unity view from its parent view, if needed.
     * Useful when navigating away or resetting the layout.
     */
    public static void detachUnityView(View unityView) {
        if (unityView != null && unityView.getParent() instanceof ViewGroup) {
            ((ViewGroup) unityView.getParent()).removeView(unityView);
        }
    }
}