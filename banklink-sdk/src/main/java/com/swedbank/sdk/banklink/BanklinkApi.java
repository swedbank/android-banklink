package com.swedbank.sdk.banklink;

import android.support.annotation.NonNull;

public interface BanklinkApi {
  String INTENT_ACTION_BANKLINK = "com.swedbank.action.BANKLINK";
  String EXTRA_PACKET = "com.swedbank.EXTRA_PACKET";
  int MIN_SUPPORTED_VERSION = 14680;

  enum Country {
    EE("com.swedbank", "1F:05:D1:FF:DA:14:75:7E:2B:E9:C9:11:0E:D0:60:16:19:4A:D4:B2"),
    LV("lv.swedbank.mobile", "87:E3:16:A2:E6:F0:AC:B0:1E:F0:00:48:13:B0:86:21:7F:67:43:94"),
    LT("lt.swedbank.mobile", "1F:05:D1:FF:DA:14:75:7E:2B:E9:C9:11:0E:D0:60:16:19:4A:D4:B2");

    public final String packageName;
    public final String fingerprint;

    Country(@NonNull String packageName, @NonNull String fingerprint) {
      this.packageName = packageName;
      this.fingerprint = fingerprint;
    }
  }
}
