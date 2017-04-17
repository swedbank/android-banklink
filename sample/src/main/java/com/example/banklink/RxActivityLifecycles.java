package com.example.banklink;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class RxActivityLifecycles {
  @NonNull
  private final Application app;
  @NonNull
  private final CompositeSubscription lifeSubscriptions = new CompositeSubscription();
  @Nullable
  private CompositeSubscription visibilitySubscriptions = null;

  private final Application.ActivityLifecycleCallbacks callbacks = new Application.ActivityLifecycleCallbacks() {
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
      if (visibilitySubscriptions != null) {
        throw new IllegalStateException("Visibility subscriptions have entered illegal state");
      }
      visibilitySubscriptions = new CompositeSubscription();
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
      if (visibilitySubscriptions != null) {
        visibilitySubscriptions.unsubscribe();
        visibilitySubscriptions = null;
      }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
      app.unregisterActivityLifecycleCallbacks(this);
      lifeSubscriptions.unsubscribe();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }
  };

  private RxActivityLifecycles(@NonNull Application app) {
    this.app = app;
    app.registerActivityLifecycleCallbacks(callbacks);
  }

  public static RxActivityLifecycles create(@NonNull Context context) {
    final Application app = (Application) context.getApplicationContext();
    return new RxActivityLifecycles(app);
  }

  public final void addSubscriptionForLife(@NonNull Subscription subscription) {
    if (lifeSubscriptions.isUnsubscribed()) {
      throw new IllegalStateException("Tried to leak subscription for life but activity is already dead. Check your code");
    }
    lifeSubscriptions.add(subscription);
  }

  public final void removeSubscriptionForLife(@NonNull Subscription subscription) {
    subscription.unsubscribe();
    lifeSubscriptions.remove(subscription);
  }

  public final void addSubscriptionForVisibility(@NonNull Subscription subscription) {
    if (visibilitySubscriptions == null) {
      throw new NullPointerException("Tried to add subscription when view is not visible. Check your code");
    }
    visibilitySubscriptions.add(subscription);
  }

  public final void removeSubscriptionForVisibility(@NonNull Subscription subscription) {
    subscription.unsubscribe();
    if (visibilitySubscriptions != null) {
      visibilitySubscriptions.remove(subscription);
    }
  }
}
