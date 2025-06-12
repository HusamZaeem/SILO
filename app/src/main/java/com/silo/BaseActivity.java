package com.silo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

public abstract class BaseActivity extends AppCompatActivity {

    private int currentNightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.setDarkMode(this, ThemeManager.isDarkMode(this));
        currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (ThemeManager.isDarkMode(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        updateStatusBar();



    }

    @Override
    protected void onResume() {
        super.onResume();
        int newNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (newNightMode != currentNightMode) {
            recreate(); // Recreate if theme changed
        }

        updateStatusBar();
    }

    protected void updateStatusBar() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar));

        View decor = window.getDecorView();
        int flags = decor.getSystemUiVisibility();

        if (ThemeManager.isDarkMode(this)) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        decor.setSystemUiVisibility(flags);
    }

}
