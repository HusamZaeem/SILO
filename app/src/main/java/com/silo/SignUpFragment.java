package com.silo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.silo.databinding.FragmentSignUpBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;

    public SignUpFragment() {
        // Required empty public constructor
    }

    public static SignUpFragment newInstance(String param1, String param2) {
        return new SignUpFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);


        boolean isNightMode = ThemeManager.isDarkMode(requireContext());

        if (isNightMode) {
            binding.ivLogo.setImageResource(R.drawable.silo_dark);
        } else {
            binding.ivLogo.setImageResource(R.drawable.silo_white);
        }


        binding.tvSignIn.setOnClickListener(v ->
                ((AuthActivity) requireActivity()).replaceFragment(new SignInFragment())
        );

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}