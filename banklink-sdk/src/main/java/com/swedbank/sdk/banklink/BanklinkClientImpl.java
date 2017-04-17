package com.swedbank.sdk.banklink;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import static android.content.Intent.ACTION_VIEW;
import static android.content.pm.PackageManager.GET_SIGNATURES;
import static com.swedbank.sdk.banklink.BanklinkApi.MIN_SUPPORTED_VERSION;
import static com.swedbank.sdk.banklink.Extensions.bundleToMap;
import static com.swedbank.sdk.banklink.Extensions.bytesToHexString;
import static com.swedbank.sdk.banklink.Extensions.mapToBundle;
import static com.swedbank.sdk.banklink.Extensions.notEmptyArgument;
import static java.util.Collections.emptyMap;

final class BanklinkClientImpl implements BanklinkClient {
  private static final String PLAY_STORE_WEB_URL_BASE = "https://play.google.com/store/apps/details?id=";
  private static final String PLAY_STORE_APP_URL_BASE = "market://details?id=";

  @NonNull
  private final Context context;
  @NonNull
  private final PackageManager packageManager;
  @NonNull
  private final BanklinkApi.Country country;

  BanklinkClientImpl(@NonNull Context context,
                     @NonNull BanklinkApi.Country country) {
    this.context = context;
    this.packageManager = context.getPackageManager();
    this.country = country;
  }

  @Override
  public boolean isSupportedSwedbankAppInstalled() {
    final List<ResolveInfo> activities = queryBanklinkActivities();
    for (ResolveInfo activity : activities) {
      final String packageName = activity.activityInfo.packageName;
      if (isSwedbank(packageName)) {
        return true;
      }
    }
    return false;
  }

  @NonNull
  @Override
  public Intent createBanklinkIntent(@NonNull Map<String, String> packet) {
    notEmptyArgument(packet, "packet");
    final List<ResolveInfo> activities = queryBanklinkActivities();
    final PackageInfo swedbankPackage = findSwedbankWithHighestSupportedVersion(activities);
    final Intent banklinkIntent = new Intent(BanklinkApi.INTENT_ACTION_BANKLINK);
    banklinkIntent.putExtra(BanklinkApi.EXTRA_PACKET, mapToBundle(packet));
    banklinkIntent.setPackage(swedbankPackage.packageName);
    return banklinkIntent;
  }

  @Override
  public void openSwedbankAppPlayStoreListing() {
    final Uri uri = Uri.parse((isPlayStoreInstalled() ? PLAY_STORE_APP_URL_BASE : PLAY_STORE_WEB_URL_BASE) + country.packageName);
    final Intent playStoreIntent = new Intent(ACTION_VIEW, uri);
    //noinspection deprecation
    playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    context.startActivity(playStoreIntent);
  }

  @NonNull
  @Override
  public BanklinkResult parseResult(int resultCode, @Nullable Intent data) {
    final boolean success;
    final Map<String, String> responsePacket;
    if (data != null) {
      final Bundle extras = data.getExtras();
      responsePacket = bundleToMap(extras.getBundle(BanklinkApi.EXTRA_PACKET));
      success = resultCode == Activity.RESULT_OK && !responsePacket.isEmpty();
    } else {
      success = false;
      responsePacket = emptyMap();
    }
    return new BanklinkResult(success,
        resultCode == Activity.RESULT_CANCELED,
        responsePacket);
  }

  @NonNull
  private PackageInfo findSwedbankWithHighestSupportedVersion(@NonNull List<ResolveInfo> activities) {
    PackageInfo swedbankPackage = null;
    for (ResolveInfo activity : activities) {
      final String packageName = activity.activityInfo.packageName;
      if (!isSwedbank(packageName)) {
        continue;
      }
      final PackageInfo packageInfo;
      try {
        packageInfo = packageManager.getPackageInfo(packageName, 0);
      } catch (NameNotFoundException e) {
        // Package was uninstalled in between list and getting package info.
        continue;
      }
      if (swedbankPackage == null || packageInfo.versionCode > swedbankPackage.versionCode) {
        swedbankPackage = packageInfo;
      }
    }
    if (swedbankPackage == null) {
      throw new ActivityNotFoundException("Swedbank app is not installed on this device.");
    }
    return swedbankPackage;
  }

  @NonNull
  private List<ResolveInfo> queryBanklinkActivities() {
    final Intent intent = new Intent(BanklinkApi.INTENT_ACTION_BANKLINK);
    return packageManager.queryIntentActivities(intent, 0);
  }

  private boolean isSwedbank(@NonNull String packageName) {
    return packageName.startsWith(country.packageName)
        && matchesExpectedFingerprint(packageManager, packageName, country.fingerprint)
        && isSupportedVersion(packageManager, packageName, MIN_SUPPORTED_VERSION);
  }

  @SuppressLint("PackageManagerGetSignatures")
  private static boolean matchesExpectedFingerprint(@NonNull PackageManager packageManager,
                                                    @NonNull String packageName,
                                                    @NonNull String expectedFingerprint) {
    final PackageInfo packageInfo;
    try {
      // Potential Multiple Certificate Exploit
      // Improper validation of app signatures could lead to issues where a malicious app submits
      // itself to the Play Store with both its real certificate and a fake certificate and gains
      // access to functionality or information it shouldn't have due to another application only
      // checking for the fake certificate and ignoring the rest. We make sure to validate all
      // signatures returned by this method.
      // https://bluebox.com/technical/android-fake-id-vulnerability/
      packageInfo = packageManager.getPackageInfo(packageName, GET_SIGNATURES);
    } catch (NameNotFoundException e) {
      return false;
    }

    final Signature[] signatures = packageInfo.signatures;
    if (signatures == null || signatures.length == 0) {
      return false;
    }

    final CertificateFactory certificateFactory;
    try {
      certificateFactory = CertificateFactory.getInstance("X509");
    } catch (CertificateException e) {
      return false;
    }
    final MessageDigest sha1;
    try {
      sha1 = MessageDigest.getInstance("SHA1");
    } catch (NoSuchAlgorithmException e) {
      return false;
    }
    for (Signature signature : signatures) {
      final byte[] signatureBytes = signature.toByteArray();
      final InputStream input = new ByteArrayInputStream(signatureBytes);
      final X509Certificate certificate;
      try {
        certificate = (X509Certificate) certificateFactory.generateCertificate(input);
      } catch (CertificateException e) {
        return false;
      }
      final byte[] encodedCertificate;
      try {
        encodedCertificate = certificate.getEncoded();
      } catch (CertificateEncodingException e) {
        return false;
      }
      final byte[] publicKey = sha1.digest(encodedCertificate);
      final String actualFingerprint = bytesToHexString(publicKey);

      // If any of the embedded certificates is not on the list of authorized fingerprints for
      // this package, we error out.
      if (!expectedFingerprint.equals(actualFingerprint)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isSupportedVersion(@NonNull PackageManager packageManager,
                                            @NonNull String packageName,
                                            int minSupportedVersion) {
    try {
      final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
      return packageInfo.versionCode >= minSupportedVersion;
    } catch (NameNotFoundException e) {
      // Package was uninstalled
    }
    return false;
  }

  private boolean isPlayStoreInstalled() {
    try {
      packageManager.getPackageInfo("com.android.vending", 0);
      return true;
    } catch (NameNotFoundException e) {
      return false;
    }
  }
}
