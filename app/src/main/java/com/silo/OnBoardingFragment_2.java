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
import com.silo.databinding.FragmentOnBoarding2Binding;

public class OnBoardingFragment_2 extends Fragment {

    private FragmentOnBoarding2Binding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TypingAnimator typingAnimator = new TypingAnimator();

    private ViewPager2 viewPager;
    private boolean transitionHandled = false;

    private String fullText = "";

    public OnBoardingFragment_2() {

    }

    public static OnBoardingFragment_2 newInstance() {
        return new OnBoardingFragment_2();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOnBoarding2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = requireActivity().findViewById(R.id.viewPagerOnboarding);

        boolean isNightMode = ThemeManager.isDarkMode(requireContext());

        LottieAnimationView bgLottie = binding.lottieOnBoarding2Bg;

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

        binding.imageLogo.setVisibility(View.INVISIBLE);
        binding.imageLogo.setAlpha(0f);

        binding.ivOnBoarding.setVisibility(View.INVISIBLE);
        binding.ivOnBoarding.setAlpha(0f);

        fullText = getString(R.string.onboarding_text_1);
        binding.tvSecondFragment.setText("");

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
            binding.tvSecondFragment.setText("");

            typingAnimator.startTyping(binding.tvSecondFragment, fullText, 1300, 100);
            handler.postDelayed(() -> fadeIn(binding.imageLogo), 1300);
            handler.postDelayed(() -> fadeIn(binding.ivOnBoarding), 1300);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
        typingAnimator.cancel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        typingAnimator.cancel();
        binding = null;
    }

    private void fadeIn(View view) {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0f);
            view.animate().alpha(1f).setDuration(500).start();
        }
    }




}
