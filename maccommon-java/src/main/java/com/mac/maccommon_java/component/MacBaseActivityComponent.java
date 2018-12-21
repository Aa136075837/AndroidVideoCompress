package com.mac.maccommon_java.component;

import com.mac.maccommon_java.MacBaseActivity;
import com.mac.maccommon_java.module.MacBaseActivityModule;
import javax.inject.Singleton;
import dagger.Component;

/**
 * @author ex-yangjb001
 * @date 2018/12/21.
 */
@Singleton
@Component(modules = {MacBaseActivityModule.class})
public interface MacBaseActivityComponent {
    MacBaseActivity getContext();
}
