package com.silo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.silo.databinding.FragmentOnBoarding1Binding;

public class OnBoardingFragment_1 extends Fragment {

    private FragmentOnBoarding1Binding binding;
    private TypingAnimator typingAnimator = new TypingAnimator();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String fullText = "";

    private final Runnable showSwipeRunnable = () -> {
        if (binding.lottieSwipe != null) {
            binding.lottieSwipe.setVisibility(View.VISIBLE);
            binding.lottieSwipe.setProgress(0f);
            binding.lottieSwipe.playAnimation();
        }
    };

    private final Runnable showImageRunnable = () -> {
        if (binding.imageLogo != null) {
            binding.imageLogo.setVisibility(View.VISIBLE);
            binding.imageLogo.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start();
        }
    };

    public OnBoardingFragment_1() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOnBoarding1Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.lottieSwipe.setVisibility(View.INVISIBLE);
        binding.imageLogo.setVisibility(View.INVISIBLE);

        fullText = getString(R.string.on_boarding_1_text);

        LottieAnimationView bgLottie = view.findViewById(R.id.lottie_onBoarding_1_bg);
        boolean isNightMode = ThemeManager.isDarkMode(requireContext());

        if (isNightMode) {
            bgLottie.setAnimation(R.raw.first_on_boarding_dark);
            binding.imageLogo.setImageResource(R.drawable.silo_dark);
            binding.lottieSwipe.setAnimation(R.raw.swipe_up_light);
        } else {
            bgLottie.setAnimation(R.raw.first_on_boarding_light);
            binding.imageLogo.setImageResource(R.drawable.silo_dark);
            binding.lottieSwipe.setAnimation(R.raw.swipe_up_light);
        }

        bgLottie.playAnimation();
        typingAnimator.startTyping(binding.textTyping, fullText, 2000, 100);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Use UnityUtils to attach Unity view
        if (binding.unityContainer.getChildCount() == 0) {
            UnityUtils.attachUnityView(requireActivity(), binding.unityContainer);
        }

        if (binding.lottieSwipe != null) {
            binding.lottieSwipe.setVisibility(View.INVISIBLE);
            handler.postDelayed(showSwipeRunnable, 2000);
        }

        if (binding.imageLogo != null) {
            binding.imageLogo.setVisibility(View.INVISIBLE);
            binding.imageLogo.setAlpha(0f);
            handler.postDelayed(showImageRunnable, 2000);
        }

        typingAnimator.startTyping(binding.textTyping, fullText, 2000, 100);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(showSwipeRunnable);
        handler.removeCallbacks(showImageRunnable);
        typingAnimator.cancel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(showSwipeRunnable);
        handler.removeCallbacks(showImageRunnable);
        typingAnimator.cancel();
        binding = null;
    }
}