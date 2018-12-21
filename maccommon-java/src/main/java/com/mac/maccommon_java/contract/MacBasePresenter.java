package com.mac.maccommon_java.contract;

import com.mac.maccommon_java.MacBaseActivity;

/**
 * @author ex-yangjb001
 * @date 2018/12/21.
 */
public class MacBasePresenter<T extends MacBaseView> {
    protected MacBaseActivity mActivity;
    protected T mView;

    public MacBasePresenter(MacBaseActivity activity, T view) {
        mActivity = activity;
        mView = view;
    }
}
