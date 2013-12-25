/*
 * Copyright (C) 2013 YROM.NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.acfun.video.fragment;

import tv.acfun.video.HomeActivity;
import tv.acfun.video.R;
import tv.acfun.video.api.API;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

/**
 * @author Yrom
 * 
 */
public class ChannelFragment extends Fragment {
    private static final String TAG = "ChannelFragment";
    private int[] mCatIds;
    private ViewPager mPager;
    private Activity mActivity;

    public ChannelFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCatIds = getArguments().getIntArray(API.EXTRAS_CATEGORY_IDS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pager, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setAdapter(new ChannelPagerAdapter(getChildFragmentManager()));
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        tabs.setViewPager(mPager);
    }

    private String getChannelName(int id) {
        if (mActivity instanceof HomeActivity) { return ((HomeActivity) mActivity).findChannelNameById(id); }
        return null;
    }

    private class ChannelPagerAdapter extends FragmentPagerAdapter {
        public ChannelPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getChannelName(mCatIds[position]);
        }

        @Override
        public Fragment getItem(int position) {
            return VideosFragment.newInstance(mCatIds[position]);
        }

        @Override
        public int getCount() {
            return mCatIds.length;
        }
    }
}
