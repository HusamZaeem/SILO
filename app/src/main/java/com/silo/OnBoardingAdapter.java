package com.silo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OnBoardingAdapter extends FragmentStateAdapter {

    public OnBoardingAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new OnBoardingFragment_1();
            case 1:
                return new OnBoardingFragment_2();
            case 2:
                return new OnBoardingFragment_3();
            case 3:
                return new OnBoardingFragment_4();
                case 4:
                return new OnBoardingFragment_5();
            default:
                return new OnBoardingFragment_1();
        }
    }



    @Override
    public int getItemCount() {
        return 5;
    }
}
