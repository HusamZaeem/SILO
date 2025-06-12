package com.silo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.silo.databinding.FragmentOnBoarding3Binding;

public class OnBoardingFragment_3 extends Fragment {

    private FragmentOnBoarding3Binding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TypingAnimator typingAnimator = new TypingAnimator();
    private String fullText = "";

    private ViewPager2 viewPager;
    private boolean transitionHandled = false;

    private final Runnable showImageLogoRunnable = () -> {
        if (binding != null && binding.imageLogo != null) {
            binding.imageLogo.setVisibility(View.VISIBLE);
            binding.imageLogo.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start();
        }
    };

    private final Runnable showIllustrationRunnable = () -> {
        if (binding != null && binding.ivOnBoarding != null) {
            binding.ivOnBoarding.setVisibility(View.VISIBLE);
            binding.ivOnBoarding.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start();
        }
    };

    public OnBoardingFragment_3() {}

    public static OnBoardingFragment_3 newInstance() {
        return new OnBoardingFragment_3();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOnBoarding3Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = requireActivity().findViewById(R.id.viewPagerOnboarding);



        boolean isNightMode = ThemeManager.isDarkMode(requireContext());

        LottieAnimationView bgLottie = binding.lottieOnBoarding1Bg;

        if (isNightMode) {
            bgLottie.setAnimation(R.raw.second_on_boarding_dark);
            binding.imageLogo.setImageResource(R.drawable.silo_dark);
        } else {
            bgLottie.setAnimation(R.raw.second_on_boarding_light);
            binding.imageLogo.setImageResource(R.drawable.silo_white);
        }


        bgLottie.setSpeed(0.8f);

        if (!bgLottie.isAnimating()) {
            bgLottie.playAnimation();
        }

        fullText = getString(R.string.onboarding_text_2);

        binding.imageLogo.setVisibility(View.INVISIBLE);
        binding.imageLogo.setAlpha(0f);

        binding.ivOnBoarding.setVisibility(View.INVISIBLE);
        binding.ivOnBoarding.setAlpha(0f);

        binding.tvOnBoarding3.setText("");

    }

    @Override
    public void onResume() {
        super.onResume();

//        transitionHandled = false;

        if (binding != null) {
            binding.imageLogo.setVisibility(View.INVISIBLE);
            binding.imageLogo.setAlpha(0f);
            binding.ivOnBoarding.setVisibility(View.INVISIBLE);
            binding.ivOnBoarding.setAlpha(0f);
            binding.tvOnBoarding3.setText("");

            typingAnimator.startTyping(binding.tvOnBoarding3, fullText, 1300, 100);
            handler.postDelayed(showImageLogoRunnable, 1300);
            handler.postDelayed(showIllustrationRunnable, 1300);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(showImageLogoRunnable);
        handler.removeCallbacks(showIllustrationRunnable);
        typingAnimator.cancel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(showImageLogoRunnable);
        handler.removeCallbacks(showIllustrationRunnable);
        typingAnimator.cancel();
        binding = null;
    }


}
