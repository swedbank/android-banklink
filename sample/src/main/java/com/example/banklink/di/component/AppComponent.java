package com.example.banklink.di.component;

import com.example.banklink.MainActivity;
import com.example.banklink.di.module.ApplicationModule;
import com.example.banklink.di.module.NetworkModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
    ApplicationModule.class,
    NetworkModule.class,
})
public interface AppComponent {
  void inject(MainActivity activity);
}
