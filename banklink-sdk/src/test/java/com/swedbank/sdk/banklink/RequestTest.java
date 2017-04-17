package com.swedbank.sdk.banklink;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Intent.ACTION_VIEW;
import static com.google.common.truth.Truth.assertThat;
import static com.swedbank.sdk.banklink.BanklinkApi.EXTRA_PACKET;
import static com.swedbank.sdk.banklink.BanklinkApi.MIN_SUPPORTED_VERSION;
import static com.swedbank.sdk.banklink.Extensions.EMPTY_MESSAGE_END;
import static com.swedbank.sdk.banklink.Extensions.bundleToMap;
import static com.swedbank.sdk.banklink.TestData.INVALID_SIGNATURE;
import static com.swedbank.sdk.banklink.TestData.SIGNED_PACKET_MAP;
import static com.swedbank.sdk.banklink.TestData.SWEDBANK_SIGNATURE;
import static junit.framework.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SuppressWarnings("deprecation")
@Config(constants = BuildConfig.class, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public final class RequestTest {
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  Context context;

  List<ResolveInfo> banklinkActivities;
  BanklinkClient eeClient;

  @Before
  public void setUp() {
    initMocks(this);

    banklinkActivities = new ArrayList<>();
    when(context.getPackageManager()
        .queryIntentActivities(new Intent(BanklinkApi.INTENT_ACTION_BANKLINK), 0))
        .thenReturn(banklinkActivities);
    when(context.getApplicationContext()).thenReturn(context);
    eeClient = BanklinkSdk.createClient(context, BanklinkApi.Country.EE);
  }

  @Test
  public void opensPlayStoreWhenInstalled() throws Exception {
    eeClient.openSwedbankAppPlayStoreListing();

    final Intent expectedIntent = new Intent(ACTION_VIEW, Uri.parse("market://details?id=com.swedbank"));
    expectedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    verify(context).startActivity(expectedIntent);
  }

  @Test
  public void opensUrlWhenPlayStoreNotInstalled() throws Exception {
    when(context.getPackageManager().getPackageInfo("com.android.vending", 0)).thenThrow(
        new PackageManager.NameNotFoundException());

    eeClient.openSwedbankAppPlayStoreListing();

    Intent expectedIntent = new Intent(ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=com.swedbank"));
    expectedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    verify(context).startActivity(expectedIntent);
  }

  @Test
  public void swedbankIsInstalled() {
    installApp("com.swedbank", MIN_SUPPORTED_VERSION, SWEDBANK_SIGNATURE);

    assertThat(eeClient.isSupportedSwedbankAppInstalled()).isTrue();
  }

  @Test
  public void swedbankNotInstalled() {
    uninstallAllApps();

    assertThat(eeClient.isSupportedSwedbankAppInstalled()).isFalse();
  }

  @Test
  public void oldSwedbankInstalled() {
    installApp("com.swedbank", 1, SWEDBANK_SIGNATURE);

    assertThat(eeClient.isSupportedSwedbankAppInstalled()).isFalse();
  }

  @Test
  public void ltInstalled() {
    installApp("lt.swedbank.mobile", MIN_SUPPORTED_VERSION, SWEDBANK_SIGNATURE);
    final BanklinkClient client = BanklinkSdk.createClient(context, BanklinkApi.Country.LT);

    assertThat(client.isSupportedSwedbankAppInstalled()).isTrue();
  }

  @Test
  public void invalidPackagePrefix() {
    installApp("com.seb", MIN_SUPPORTED_VERSION + 2, SWEDBANK_SIGNATURE);

    assertThat(eeClient.isSupportedSwedbankAppInstalled()).isFalse();
  }

  @Test
  public void invalidSignature() {
    installApp("com.swedbank", MIN_SUPPORTED_VERSION + 2, INVALID_SIGNATURE);

    assertThat(eeClient.isSupportedSwedbankAppInstalled()).isFalse();
  }

  @Test
  public void multipleLocalesInstalled() {
    installApp("com.swedbank", MIN_SUPPORTED_VERSION + 2, SWEDBANK_SIGNATURE);
    installApp("lt.swedbank.mobile", MIN_SUPPORTED_VERSION + 3, SWEDBANK_SIGNATURE);

    final BanklinkClient ltClient = BanklinkSdk.createClient(context, BanklinkApi.Country.LT);
    assertThat(ltClient.isSupportedSwedbankAppInstalled()).isTrue();
    assertThat(eeClient.isSupportedSwedbankAppInstalled()).isTrue();
  }

  @Test
  public void intentCreatedWithCorrectParams() {
    installApp("com.swedbank", MIN_SUPPORTED_VERSION + 2, SWEDBANK_SIGNATURE);
    final Intent banklinkIntent = eeClient.createBanklinkIntent(SIGNED_PACKET_MAP);

    assertThat(bundleToMap(banklinkIntent.getBundleExtra(EXTRA_PACKET))).isEqualTo(SIGNED_PACKET_MAP);
    assertThat(banklinkIntent.getPackage()).isEqualTo("com.swedbank");
    assertThat(banklinkIntent.getAction()).isEqualTo("com.swedbank.action.BANKLINK");
  }

  @Test
  public void createIntentWithNullInputThrows() {
    installApp("com.swedbank", MIN_SUPPORTED_VERSION + 2, SWEDBANK_SIGNATURE);
    try {
      final Intent banklinkIntent = eeClient.createBanklinkIntent(null);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("packet" + EMPTY_MESSAGE_END);
    }
  }

  @Test
  public void createIntentWithEmptyInputThrows() {
    installApp("com.swedbank", MIN_SUPPORTED_VERSION + 2, SWEDBANK_SIGNATURE);
    try {
      final Intent banklinkIntent = eeClient.createBanklinkIntent(Collections.<String, String>emptyMap());
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("packet" + EMPTY_MESSAGE_END);
    }
  }

  @Test
  public void createIntentWithNoAppThrows() {
    try {
      final Intent banklinkIntent = eeClient.createBanklinkIntent(SIGNED_PACKET_MAP);
      fail();
    } catch (ActivityNotFoundException e) {
      assertThat(e.getMessage()).isEqualTo("Swedbank app is not installed on this device.");
    }
  }

  @Test
  public void createIntentWithInvalidSignatureThrows() {
    try {
      installApp("com.swedbank", MIN_SUPPORTED_VERSION + 2, INVALID_SIGNATURE);
      final Intent banklinkIntent = eeClient.createBanklinkIntent(SIGNED_PACKET_MAP);
      fail();
    } catch (ActivityNotFoundException e) {
      assertThat(e.getMessage()).isEqualTo("Swedbank app is not installed on this device.");
    }
  }

  @Test
  public void createIntentWithOldSwedbankAppThrows() {
    try {
      installApp("com.swedbank", 1, SWEDBANK_SIGNATURE);
      final Intent banklinkIntent = eeClient.createBanklinkIntent(SIGNED_PACKET_MAP);
      fail();
    } catch (ActivityNotFoundException e) {
      assertThat(e.getMessage()).isEqualTo("Swedbank app is not installed on this device.");
    }
  }

  @Test
  public void pinsToHighestVersionNumber() {
    installApp("com.swedbank", MIN_SUPPORTED_VERSION + 2, SWEDBANK_SIGNATURE);
    installApp("com.swedbank.beta", MIN_SUPPORTED_VERSION + 3, SWEDBANK_SIGNATURE);

    final Intent banklinkIntent = eeClient.createBanklinkIntent(SIGNED_PACKET_MAP);

    assertThat(banklinkIntent.getPackage()).isEqualTo("com.swedbank.beta");
  }

  private void uninstallAllApps() {
    banklinkActivities.clear();
  }

  @SuppressWarnings("WrongConstant")
  private void installApp(@NonNull String packageName,
                          int versionCode,
                          @NonNull Signature signature) {
    final ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.packageName = packageName;
    banklinkActivities.add(resolveInfo);
    final PackageInfo packageInfo = new PackageInfo();
    packageInfo.versionCode = versionCode;
    packageInfo.packageName = packageName;
    packageInfo.signatures = new Signature[1];
    packageInfo.signatures[0] = signature;
    try {
      when(context.getPackageManager().getPackageInfo(eq(packageName), anyInt()))
          .thenReturn(packageInfo);
    } catch (PackageManager.NameNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
