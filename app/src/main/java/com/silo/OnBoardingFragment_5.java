package com.silo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.silo.databinding.FragmentOnBoarding5Binding;

public class OnBoardingFragment_5 extends Fragment {

    private FragmentOnBoarding5Binding binding;
    private ViewPager2 viewPager;
    private String selectedUserType = null;

    public OnBoardingFragment_5() {}

    public static OnBoardingFragment_5 newInstance() {
        return new OnBoardingFragment_5();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOnBoarding5Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = requireActivity().findViewById(R.id.viewPagerOnboarding);
        viewPager.setUserInputEnabled(false);

        // Apply theme-based assets
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
        bgLottie.playAnimation();

        // Initially set visibility to invisible
        binding.imageLogo.setVisibility(View.INVISIBLE);
        binding.btnAslUser.setVisibility(View.INVISIBLE);
        binding.btnNonAslUser.setVisibility(View.INVISIBLE);
        binding.btnNext.setVisibility(View.INVISIBLE);

        // Delay and fade in UI after 1.5s
        view.postDelayed(() -> {
            fadeIn(binding.imageLogo);
            fadeIn(binding.btnAslUser);
            fadeIn(binding.btnNonAslUser);
            fadeIn(binding.btnNext);
        }, 1500);

        // Button click logic
        binding.btnAslUser.setOnClickListener(v -> {
            selectedUserType = "ASL";
            highlightSelectedButton();
        });

        binding.btnNonAslUser.setOnClickListener(v -> {
            selectedUserType = "Non-ASL";
            highlightSelectedButton();
        });

        binding.btnNext.setOnClickListener(v -> {
            if (selectedUserType == null) {
                Toast.makeText(getContext(), "Please select a user type first", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(requireContext(), AuthActivity.class);
                intent.putExtra("USER_TYPE", selectedUserType);
                startActivity(intent);
                requireActivity().finish();
            }
        });
    }

    private void fadeIn(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(500)
                .start();
    }

    private void highlightSelectedButton() {
        binding.btnAslUser.setAlpha(0.3f);
        binding.btnNonAslUser.setAlpha(0.3f);

        if ("ASL".equals(selectedUserType)) {
            binding.btnAslUser.setAlpha(1f);
        } else if ("Non-ASL".equals(selectedUserType)) {
            binding.btnNonAslUser.setAlpha(1f);
        }
    }
}
