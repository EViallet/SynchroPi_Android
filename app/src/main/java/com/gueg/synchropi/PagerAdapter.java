package com.gueg.synchropi;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> _fragments;

    public PagerAdapter(FragmentManager fm, List<Fragment> frags) {
        super(fm);
        _fragments = frags;
    }

    @Override
    public Fragment getItem(int pos) {
        return _fragments.get(pos);
    }

    @Override
    public int getCount() {
        return _fragments.size();
    }

}
