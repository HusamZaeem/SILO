package com.silo.translation;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.media.Image;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ImageUtils {

    // Compute average luminance (brightness) from the Y plane
    @OptIn(markerClass = ExperimentalGetImage.class)
    public static float computeLuminance(ImageProxy imageProxy) {
        Image image = imageProxy.getImage();
        if (image == null || image.getPlanes().length == 0) return 0f;

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        int sum = 0;
        int count = buffer.remaining();
        for (int i = 0; i < count; i++) {
            sum += buffer.get(i) & 0xFF;
        }
        return sum / (float) count / 255f;
    }

    // Convert ImageProxy to Bitmap for MediaPipe
    public static Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(
                nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, out);
        byte[] imageBytes = out.toByteArray();

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
