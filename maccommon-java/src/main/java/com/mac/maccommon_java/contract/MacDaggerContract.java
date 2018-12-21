package com.mac.maccommon_java.contract;

import com.mac.maccommon_java.MacBaseActivity;
import com.mac.maccommon_java.component.DaggerPresenterComponent;
import com.mac.maccommon_java.module.MacBaseActivityModule;

import javax.inject.Inject;

/**
 * @author ex-yangjb001
 * @date 2018/12/21.
 */
public interface MacDaggerContract {
    interface MacDaggerView extends MacBaseView {
        void showText(String text);
    }

    class MacDaggerPresenter extends MacBasePresenter<MacDaggerView> {
        private String mString;

        @Inject
        MacDaggerPresenter(MacBaseActivity activity, MacDaggerView view, String s) {
            super(activity, view);
            mString = s;
            DaggerPresenterComponent.builder().macBaseActivityModule(new MacBaseActivityModule(activity))
                    .build().inject(this);
        }

        public void showText() {
            mView.showText(mString);
        }
    }
}
