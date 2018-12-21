package com.mac.maccommon_java;

import android.os.Bundle;
import android.widget.TextView;

import com.mac.maccommon_java.component.DaggerMacDaggerComponent;
import com.mac.maccommon_java.contract.MacDaggerContract;
import com.mac.maccommon_java.module.MacDaggerModule;

import javax.inject.Inject;

/**
 * @author ex-yangjb001
 */
public class MacDaggerActivity extends MacBaseActivity implements MacDaggerContract.MacDaggerView {

    @Inject
    MacDaggerContract.MacDaggerPresenter mMacDaggerPresenter;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mac_dagger);
        mTextView = findViewById(R.id.mac_dagger_tv);
        DaggerMacDaggerComponent.builder().macBaseActivityComponent(getBaseActivityComponent())
                .macDaggerModule(new MacDaggerModule(this,"chang argument"))
                .build().inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMacDaggerPresenter.showText();
    }

    @Override
    public void showText(String text) {
        mTextView.setText(text);
    }
}
