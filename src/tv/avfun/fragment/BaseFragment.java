package tv.avfun.fragment;

import android.app.Activity;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;


public abstract class BaseFragment extends SherlockFragment {
    public abstract void onShow();
    protected Activity activity;
    protected boolean isShow;
    private void show(){
        if(!isShow){
            onShow();
            isShow = true;
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        show();
    }
    
    
    public abstract void onSwitch(ActionBar bar);
}
