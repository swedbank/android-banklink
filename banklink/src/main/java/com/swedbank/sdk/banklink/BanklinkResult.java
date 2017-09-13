package com.swedbank.sdk.banklink;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.Map;

/**
 * Parsed banklink transaction result from the Swedbank app.
 */
public final class BanklinkResult {
  private final boolean success;
  private final boolean canceled;
  @NonNull
  private final Map<String, String> responsePacket;

  BanklinkResult(boolean success,
                 boolean canceled,
                 @NonNull Map<String, String> responsePacket) {
    this.success = success;
    this.canceled = canceled;
    this.responsePacket = responsePacket;
  }

  /**
   * @return {@code true} when banklink transaction was successful. {@code false} when
   * error has occurred or user canceled the process.
   */
  @CheckResult
  public final boolean success() {
    return success;
  }

  /**
   * @return {@code true} when user canceled banklink process, {@code false} in any other case.
   */
  @CheckResult
  public final boolean canceled() {
    return canceled;
  }

  /**
   * @return a map containing banklink process result when {@link #success()} == true.
   * Empty map in any other case.
   */
  @NonNull
  @CheckResult
  public final Map<String, String> responsePacket() {
    return responsePacket;
  }
}
