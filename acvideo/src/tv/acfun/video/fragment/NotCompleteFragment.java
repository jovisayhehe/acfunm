/*
 * Copyright (C) 2014 YROM.NET
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

import com.umeng.analytics.MobclickAgent;

import tv.ac.fun.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * @author Yrom
 *
 */
public class NotCompleteFragment extends Fragment implements OnClickListener {
    private int mId;
    
    public static Fragment newInstance(int id){
        Fragment f= new NotCompleteFragment();
        
        Bundle args = new Bundle();
        args.putInt("id", id);
        f.setArguments(args);
        return f;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getArguments().getInt("id");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_not_complete, container,false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.rating).setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        MobclickAgent.onEvent(getActivity(), "rating", String.valueOf(mId));
    }
}
