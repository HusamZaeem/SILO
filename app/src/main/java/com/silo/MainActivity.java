package com.silo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.navigation.NavigationBarView;
import com.silo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    private Fragment homeFragment;
    private Fragment contactsFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    private int currentSelectedItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ThemeManager.isDarkMode(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.themeSwitch.setChecked(ThemeManager.isDarkMode(this));
        binding.themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.setDarkMode(this, isChecked);
            Intent intent = getIntent();
            finish();
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Initializing fragments
        homeFragment = new HomeFragment();
        contactsFragment = new ContactsFragment();
        profileFragment = new ProfileFragment();

        activeFragment = homeFragment;

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.MainFragmentContainer, profileFragment, "profile").hide(profileFragment)
                .add(R.id.MainFragmentContainer, contactsFragment, "contacts").hide(contactsFragment)
                .add(R.id.MainFragmentContainer, homeFragment, "home")
                .commit();

        currentSelectedItemId = R.id.nav_home; // default selected

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == currentSelectedItemId) {
                // If clicking again on selected item, recreate that fragment
                recreateFragment(itemId);
                return true;
            }

            // Switch fragments without recreating
            switchToFragment(itemId);
            currentSelectedItemId = itemId;
            return true;
        });



        binding.cvUserPhoto.setOnClickListener(v -> {
            // Simulate clicking the profile item in the bottom nav
            binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
        });








    }

    private void switchToFragment(int itemId) {
        FragmentManager fm = getSupportFragmentManager();

        if (itemId == R.id.nav_home && activeFragment != homeFragment) {
            fm.beginTransaction().hide(activeFragment).show(homeFragment).commit();
            activeFragment = homeFragment;
        } else if (itemId == R.id.nav_contacts && activeFragment != contactsFragment) {
            fm.beginTransaction().hide(activeFragment).show(contactsFragment).commit();
            activeFragment = contactsFragment;
        } else if (itemId == R.id.nav_profile && activeFragment != profileFragment) {
            fm.beginTransaction().hide(activeFragment).show(profileFragment).commit();
            activeFragment = profileFragment;
        }
    }

    private void recreateFragment(int itemId) {
        FragmentManager fm = getSupportFragmentManager();

        Fragment newFragment = null;
        String tag = "";

        if (itemId == R.id.nav_home) {
            newFragment = new HomeFragment();
            tag = "home";
            homeFragment = newFragment;
        } else if (itemId == R.id.nav_contacts) {
            newFragment = new ContactsFragment();
            tag = "contacts";
            contactsFragment = newFragment;
        } else if (itemId == R.id.nav_profile) {
            newFragment = new ProfileFragment();
            tag = "profile";
            profileFragment = newFragment;
        }

        if (newFragment != null) {
            fm.beginTransaction()
                    .remove(activeFragment)
                    .add(R.id.MainFragmentContainer, newFragment, tag)
                    .commit();
            activeFragment = newFragment;
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatusBar();
    }

    private void updateStatusBar() {
        Window window = getWindow();
        if (ThemeManager.isDarkMode(this)) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar));
            window.getDecorView().setSystemUiVisibility(0);
        } else {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}
