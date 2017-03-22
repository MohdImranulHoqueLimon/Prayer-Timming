package com.limon.PrayerTiming.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.limon.PrayerTiming.R;

import butterknife.ButterKnife;

/**
 * Created by Limon on 3/23/2017.
 */

public class QiblaFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_time, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }
}
