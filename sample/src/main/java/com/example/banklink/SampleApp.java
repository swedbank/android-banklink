package com.example.banklink;

import android.app.Application;
import android.support.annotation.NonNull;

import com.example.banklink.di.component.AppComponent;
import com.example.banklink.di.component.DaggerAppComponent;
import com.example.banklink.di.module.ApplicationModule;

public class SampleApp extends Application {
  private AppComponent appComponent;

  @Override
  public void onCreate() {
    super.onCreate();
    this.appComponent = DaggerAppComponent.builder()
        .applicationModule(new ApplicationModule(this))
        .build();
  }

  @NonNull
  public AppComponent appComponent() {
    return appComponent;
  }
}
