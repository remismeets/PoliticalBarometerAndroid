package be.kdg.androidbarometer.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import be.kdg.androidbarometer.R;

public class AboutUsFragment extends Fragment {
    public AboutUsFragment() {
        //Required empty public constructor
    }

    /**
     * Creates the Fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about_us, container, false);
    }
}
