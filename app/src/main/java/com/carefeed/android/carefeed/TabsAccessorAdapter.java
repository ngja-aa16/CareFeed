package com.carefeed.android.carefeed;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                FeedsFragment feedsFragment = new FeedsFragment();
                return feedsFragment;
            case 1:
                PostsFragment postsFragment = new PostsFragment();
                return postsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Feeds";
            case 1:
                return "Posts";
            default:
                return null;
        }
    }
}
