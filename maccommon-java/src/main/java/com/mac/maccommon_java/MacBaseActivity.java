package com.mac.maccommon_java;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mac.maccommon_java.component.DaggerMacBaseActivityComponent;
import com.mac.maccommon_java.component.MacBaseActivityComponent;
import com.mac.maccommon_java.module.MacBaseActivityModule;

/**
 * @author ex-yangjb001
 * @date 2018/12/21.
 */
public class MacBaseActivity extends AppCompatActivity {

    private MacBaseActivityComponent mBaseActivityComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaseActivityComponent = DaggerMacBaseActivityComponent.builder().macBaseActivityModule(new MacBaseActivityModule(this)).build();
    }

    public MacBaseActivityComponent getBaseActivityComponent() {
        return mBaseActivityComponent;
    }
}
