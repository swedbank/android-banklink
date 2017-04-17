package com.example.banklink.di.module;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.inputmethod.InputMethodManager;

import com.swedbank.sdk.banklink.BanklinkApi;
import com.swedbank.sdk.banklink.BanklinkClient;
import com.swedbank.sdk.banklink.BanklinkSdk;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class ApplicationModule {
  @NonNull
  private final Application app;

  public ApplicationModule(@NonNull Application app) {
    this.app = app;
  }

  @Provides
  @Singleton
  Application provideApplication() {
    return app;
  }

  @Provides
  @Singleton
  InputMethodManager provideInputMethodManager(Application app) {
    return (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);
  }

  @Provides
  @Singleton
  BanklinkClient provideEstoniaBanklinkClient(Application app) {
    return BanklinkSdk.createClient(app, BanklinkApi.Country.EE);
  }
}
