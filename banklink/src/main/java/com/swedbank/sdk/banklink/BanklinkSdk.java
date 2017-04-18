package com.swedbank.sdk.banklink;

import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import static com.swedbank.sdk.banklink.Extensions.nonNull;

/**
 * SDK for implementing banklink app-to-app transactions.
 */
public final class BanklinkSdk {
  private BanklinkSdk() {
    throw new AssertionError("no instances");
  }

  /**
   * Creates a new instance of {@link BanklinkClient} that can then be used to create banklink
   * intents.
   *
   * @param context Any {@link Context} will work. It is safe to pass in an activity context, as
   *                the {@link BanklinkClient} instance will only hold on to the result from {@link
   *                Context#getApplicationContext()}.
   * @param country  Country of which Swedbank app to compile this client against.
   * @return a unique {@link BanklinkClient} instance.
   * @throws NullPointerException if {@code context} or {@code country} are null.
   */
  @NonNull
  @CheckResult
  public static BanklinkClient createClient(@NonNull Context context,
                                            @NonNull BanklinkApi.Country country) {
    final Context appContext = nonNull(context, "context").getApplicationContext();
    nonNull(country, "country");
    return new BanklinkClientImpl(appContext, country);
  }
}
