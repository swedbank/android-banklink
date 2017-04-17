package com.swedbank.sdk.banklink;

import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import java.util.Locale;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Internal utility and helper functions.
 */
final class Extensions {
  static final String NPE_MESSAGE_END = " must not be null";
  static final String EMPTY_MESSAGE_END = " must not be null or empty";

  static <T> T nonNull(T o, @NonNull String name) {
    if (o == null) {
      throw new NullPointerException(name + NPE_MESSAGE_END);
    }
    return o;
  }

  static void notEmptyArgument(Map<?, ?> map, @NonNull String name) {
    if (map == null || map.isEmpty()) {
      throw new IllegalArgumentException(name + EMPTY_MESSAGE_END);
    }
  }

  static String bytesToHexString(byte[] bytes) {
    // Capacity should be (bytes.length * 3 - 1), but this avoids negative case.
    StringBuilder hex = new StringBuilder(bytes.length * 3);
    int len = bytes.length;
    for (int i = 0; i < len; i++) {
      int lo = bytes[i] & 0xff;
      if (lo < 0x10) {
        hex.append('0');
      }
      hex.append(Integer.toHexString(lo).toUpperCase(Locale.US));
      if (i != len - 1) {
        hex.append(':');
      }
    }
    return hex.toString();
  }

  @NonNull
  @CheckResult
  static Bundle mapToBundle(@NonNull Map<String, String> map) {
    final int mapSize = map.size();
    if (mapSize == 0) {
      return Bundle.EMPTY;
    }
    final Bundle bundle = new Bundle(mapSize);
    for (String key : map.keySet()) {
      bundle.putString(key, map.get(key));
    }
    return bundle;
  }

  @NonNull
  @CheckResult
  static Map<String, String> bundleToMap(@Nullable Bundle bundle) {
    if (bundle == null) {
      return emptyMap();
    }
    final int bundleSize = bundle.size();
    if (bundleSize == 0) {
      return emptyMap();
    }
    final ArrayMap<String, String> map = new ArrayMap<>(bundleSize);
    for (String key : bundle.keySet()) {
      map.put(key, bundle.getString(key));
    }
    return map;
  }
}
