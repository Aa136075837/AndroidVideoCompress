package com.mac.maccommon_java.component;

import com.mac.maccommon_java.ActivityScoped;
import com.mac.maccommon_java.MacDaggerActivity;
import com.mac.maccommon_java.module.MacDaggerModule;

import dagger.Component;

/**
 * @author ex-yangjb001
 * @date 2018/12/21.
 */
@ActivityScoped
@Component(modules = MacDaggerModule.class, dependencies = MacBaseActivityComponent.class)
public interface MacDaggerComponent {
    void inject(MacDaggerActivity macDaggerActivity);
}
