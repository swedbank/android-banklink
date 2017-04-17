package com.example.banklink.di.module;

import android.app.Application;

import com.example.banklink.BuildConfig;
import com.example.banklink.network.SignService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.schedulers.Schedulers;

import static com.example.banklink.BuildConfig.SIGN_ENDPOINT;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BODY;

@Module
public final class NetworkModule {
  @Provides
  @Singleton
  OkHttpClient provideOkHttp() {
    final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
    if (BuildConfig.DEBUG) {
      final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
      logging.setLevel(BODY);
      clientBuilder.addInterceptor(logging);
    }
    return clientBuilder.build();
  }

  @Provides
  @Singleton
  Retrofit provideRetrofit(OkHttpClient client) {
    return new Retrofit.Builder()
        .client(client)
        .baseUrl(SIGN_ENDPOINT)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
        .build();
  }

  @Provides
  @Singleton
  SignService provideSignService(Retrofit retrofit) {
    return retrofit.create(SignService.class);
  }
}
