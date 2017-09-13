package com.swedbank.sdk.banklink;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

/**
 * Provides methods for interacting with the Swedbank app, such as by generating
 * {@code Intent}s that initiate banklink transactions.
 */
public interface BanklinkClient {
  /**
   * @return {@code true} if a version of the Swedbank app that supports the Banklink API
   * is installed, {@code false} otherwise.
   */
  @CheckResult
  boolean isSupportedSwedbankAppInstalled();

  /**
   * Creates an {@code Intent} that can be used to initiate a Swedbank banklink
   * transaction. Provide the created {@code Intent} to the
   * {@link android.app.Activity#startActivityForResult(Intent, int)} to
   * initiate the transaction.
   *
   * @param packet packet data.
   * @throws ActivityNotFoundException if the Swedbank app is not installed.
   * @throws NullPointerException      if packet is null.
   */
  @NonNull
  @CheckResult
  Intent createBanklinkIntent(@NonNull Map<String, String> packet) throws ActivityNotFoundException;

  /**
   * Opens the Swedbank app install page in the Google Play Store according to provided locale.
   * The Play Store activity is started using the application context.
   *
   * @throws ActivityNotFoundException if there is no app that can open Play Store listing.
   */
  void openSwedbankAppPlayStoreListing() throws ActivityNotFoundException;

  /**
   * Parse the result from the Swedbank app for initiated banklink transaction.
   * <p>
   * This method must be called in {@link android.app.Activity#onActivityResult(int, int, Intent) Activity#onActivityResult(requestCode, resultCode, data)}.
   *
   * @param resultCode result code in the {@link android.app.Activity#onActivityResult(int, int, Intent) Activity#onActivityResult(requestCode, resultCode, data)}
   * @param data       data in the {@link android.app.Activity#onActivityResult(int, int, Intent) Activity#onActivityResult(requestCode, resultCode, data)}
   * @return Parsed transaction result.
   */
  @NonNull
  @CheckResult
  BanklinkResult parseResult(int resultCode, @Nullable Intent data);
}
