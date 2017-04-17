package com.swedbank.sdk.banklink;

import android.os.Bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static com.swedbank.sdk.banklink.Extensions.bundleToMap;
import static com.swedbank.sdk.banklink.Extensions.mapToBundle;
import static com.swedbank.sdk.banklink.TestData.SIGNED_PACKET_BUNDLE;
import static com.swedbank.sdk.banklink.TestData.SIGNED_PACKET_MAP;

@Config(constants = BuildConfig.class, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ExtensionsTest {
  @Test
  public void mapToBundleAcceptsEmpty() {
    final Bundle bundle = mapToBundle(Collections.<String, String>emptyMap());

    assertThat(bundle).isEqualTo(Bundle.EMPTY);
  }

  @Test
  public void mapToBundleHasCorrectValues() {
    final Bundle bundle = mapToBundle(SIGNED_PACKET_MAP);

    assertThat(bundle.keySet()).isEqualTo(SIGNED_PACKET_BUNDLE.keySet());
  }

  @Test
  public void bundleToMapAcceptsNull() {
    final Map<String, String> map = bundleToMap(null);

    assertThat(map).isNotNull();
    assertThat(map).isEmpty();
  }

  @Test
  public void bundleToMapAcceptsEmpty() {
    final Map<String, String> map = bundleToMap(new Bundle());

    assertThat(map).isNotNull();
    assertThat(map).isEmpty();
  }

  @Test
  public void bundleToMapHasCorrectValues() {
    final Map<String, String> map = bundleToMap(SIGNED_PACKET_BUNDLE);

    assertThat(map).isNotNull();
    assertThat(map).isEqualTo(SIGNED_PACKET_MAP);
  }
}