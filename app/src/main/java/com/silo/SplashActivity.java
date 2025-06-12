package com.silo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.activity.ComponentActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.silo.R;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView lottie = findViewById(R.id.lottieSplash);

        // Choose animation based on current UI mode
        boolean isNightMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        if (isNightMode) {
            lottie.setAnimation(R.raw.silo_dark_splash);
        } else {
            lottie.setAnimation(R.raw.silo_light_splash);
        }

        lottie.playAnimation();

        lottie.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                navigateToOnboarding();
            }
        });
    }

    private void navigateToOnboarding() {
        startActivity(new Intent(this, OnBoarding.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
