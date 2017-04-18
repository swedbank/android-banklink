package com.swedbank.sdk.banklink;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;
import static com.google.common.truth.Truth.assertThat;
import static com.swedbank.sdk.banklink.BanklinkApi.EXTRA_PACKET;
import static com.swedbank.sdk.banklink.Extensions.mapToBundle;
import static com.swedbank.sdk.banklink.TestData.SIGNED_PACKET_MAP;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@Config(constants = BuildConfig.class, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ResultTest {
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  Context mockContext;
  @Mock
  Intent mockIntent;
  @Mock
  Bundle mockBundle;

  private BanklinkClient banklinkClient;

  @Before
  public void setUp() {
    initMocks(this);

    when(mockContext.getApplicationContext()).thenReturn(mockContext);
    when(mockIntent.getExtras()).thenReturn(mockBundle);
    when(mockBundle.getString(anyString(), anyString())).thenReturn("");
    banklinkClient = BanklinkSdk.createClient(mockContext, BanklinkApi.Country.EE);
  }

  @Test
  public void successfulResponse() {
    final Intent resultIntent = new Intent();
    resultIntent.putExtra(EXTRA_PACKET, mapToBundle(SIGNED_PACKET_MAP));

    final BanklinkResult result = banklinkClient.parseResult(RESULT_OK, resultIntent);
    assertThat(result).isNotNull();
    assertThat(result.success()).isTrue();
    assertThat(result.responsePacket()).isEqualTo(SIGNED_PACKET_MAP);
    assertThat(result.canceled()).isFalse();
  }

  @Test
  public void unsuccessfulForInvalidResponseCode() {
    final BanklinkResult result = banklinkClient.parseResult(RESULT_FIRST_USER + 2, mockIntent);
    assertUnsuccessfulResponse(result);
    assertThat(result.canceled()).isFalse();
  }

  @Test
  public void unsuccessfulForNullData() {
    final BanklinkResult result = banklinkClient.parseResult(RESULT_OK, null);
    assertUnsuccessfulResponse(result);
    assertThat(result.canceled()).isFalse();
  }

  @Test
  public void unsuccessfulForMissingResponsePacket() {
    final BanklinkResult result = banklinkClient.parseResult(RESULT_OK, mockIntent);
    assertUnsuccessfulResponse(result);
    assertThat(result.canceled()).isFalse();
  }

  @Test
  public void canceledWithNullData() {
    final BanklinkResult result = banklinkClient.parseResult(RESULT_CANCELED, null);
    assertUnsuccessfulResponse(result);
    assertThat(result.canceled()).isTrue();
  }

  @Test
  public void canceledWithMissingResponsePacket() {
    final BanklinkResult result = banklinkClient.parseResult(RESULT_CANCELED, mockIntent);
    assertUnsuccessfulResponse(result);
    assertThat(result.canceled()).isTrue();
  }

  private void assertUnsuccessfulResponse(BanklinkResult result) {
    assertThat(result).isNotNull();
    assertThat(result.success()).isFalse();
    assertThat(result.responsePacket()).isNotNull();
    assertThat(result.responsePacket()).isEmpty();
  }
}
