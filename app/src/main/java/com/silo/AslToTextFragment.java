package com.silo;


import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.silo.databinding.FragmentAslToTextBinding;

import java.io.File;

public class AslToTextFragment extends Fragment {
    private FragmentAslToTextBinding b;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String json = result.getData().getStringExtra("json_landmarks");
                    String path = result.getData().getStringExtra("video_path");

                    b.tvAslToText.setText("Translation coming soon...");
                    Log.d(TAG, "Landmarks JSON:\n" + json);

                    b.videoView.setVisibility(View.VISIBLE);
                    b.videoView.setVideoURI(Uri.fromFile(new File(path)));
                    b.videoView.start();
                }
            });



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentAslToTextBinding.inflate(inflater, container, false);

        b.btnLaunchCamera.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CameraActivity.class);
            cameraLauncher.launch(intent);
        });

        return b.getRoot();
    }
}
