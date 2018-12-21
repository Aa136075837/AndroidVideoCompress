package com.mac.maccommon_java.module;

import com.mac.maccommon_java.contract.MacDaggerContract;

import dagger.Module;
import dagger.Provides;

/**
 * @author ex-yangjb001
 * @date 2018/12/21.
 */
@Module
public class MacDaggerModule {
    private MacDaggerContract.MacDaggerView mMacDaggerView;
    private String moduleString;

    public MacDaggerModule(MacDaggerContract.MacDaggerView macDaggerView, String s) {
        mMacDaggerView = macDaggerView;
        moduleString = s;
    }

    @Provides
    MacDaggerContract.MacDaggerView providerMacDaggerView() {
        return mMacDaggerView;
    }

    @Provides
    String providerString() {
        return moduleString;
    }
}
