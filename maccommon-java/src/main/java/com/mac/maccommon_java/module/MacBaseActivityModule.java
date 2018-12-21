package com.mac.maccommon_java.module;

import com.mac.maccommon_java.MacBaseActivity;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;

/**
 * @author ex-yangjb001
 * @date 2018/12/21.
 */
@Module
public class MacBaseActivityModule {
    private MacBaseActivity mMacBaseActivity;

    public MacBaseActivityModule(MacBaseActivity macBaseActivity) {
        mMacBaseActivity = macBaseActivity;
    }

    @Provides
    @Singleton
    MacBaseActivity provideContext() {
        return mMacBaseActivity;
    }
}
