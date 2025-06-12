package com.silo;

import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.silo.databinding.ActivityOnBoardingBinding;

public class OnBoarding extends BaseActivity {

    private ActivityOnBoardingBinding binding;
    private OnBoardingAdapter adapter;

    private float startY = 0f;
    private final float SWIPE_THRESHOLD = 100f;
    private boolean isAnimating = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityOnBoardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onBoarding), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        boolean isNightMode = ThemeManager.isDarkMode(this);

        LottieAnimationView bgLottie = binding.lottieTransition;
        if (isNightMode) {
            bgLottie.setAnimation(R.raw.on_boarding_transition_dark);
        } else {
            bgLottie.setAnimation(R.raw.on_boarding_transition);
        }


        adapter = new OnBoardingAdapter(this);
        binding.viewPagerOnboarding.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        binding.viewPagerOnboarding.setAdapter(adapter);

        // Disable default ViewPager swiping
        binding.viewPagerOnboarding.setUserInputEnabled(false);

        // Handle manual swipe gestures
        binding.viewPagerOnboarding.setOnTouchListener((v, event) -> {
            int currentItem = binding.viewPagerOnboarding.getCurrentItem();
            int lastIndex = adapter.getItemCount() - 1;

            if (currentItem == lastIndex) {
                return true; // fully block all gestures on last page
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = event.getY();
                    return true;

                case MotionEvent.ACTION_UP:
                    float diffY = startY - event.getY();

                    if (diffY > SWIPE_THRESHOLD && !isAnimating) {
                        if (currentItem >= 1 && currentItem <= 3) {
                            // Animate and move to next
                            isAnimating = true;
                            playExitLottieThenAdvance(currentItem, binding.lottieTransition);
                        } else {
                            // Just move to next
                            binding.viewPagerOnboarding.setCurrentItem(currentItem + 1, true);
                        }
                    }

                    return true;

                default:
                    return true;
            }
        });
    }

    private void playExitLottieThenAdvance(int currentPosition, LottieAnimationView lottieView) {
        if (lottieView == null) return;

        lottieView.setRepeatCount(0);
        lottieView.setSpeed(2.8f);
        lottieView.setProgress(0f);
        lottieView.setVisibility(View.VISIBLE);

        lottieView.removeAllAnimatorListeners();
        lottieView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                binding.viewPagerOnboarding.setCurrentItem(currentPosition + 1, false);
                lottieView.setVisibility(View.GONE);
                isAnimating = false;
                lottieView.removeAnimatorListener(this);
            }
        });

        lottieView.playAnimation();
    }






}



