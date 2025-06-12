package com.silo;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.silo.databinding.FragmentSignInBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignInFragment extends Fragment {

    private FragmentSignInBinding binding;

    public SignInFragment() {
        // Required empty public constructor
    }

    public static SignInFragment newInstance(String param1, String param2) {
        return new SignInFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(inflater, container, false);


        boolean isNightMode = ThemeManager.isDarkMode(requireContext());

        if (isNightMode) {
            binding.ivLogo.setImageResource(R.drawable.silo_dark);
        } else {
            binding.ivLogo.setImageResource(R.drawable.silo_white);
        }


        binding.tvSignUp.setOnClickListener(v ->
                ((AuthActivity) requireActivity()).replaceFragment(new SignUpFragment())
        );




        binding.btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            startActivity(intent);
        });


        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // avoid memory leaks
    }
}