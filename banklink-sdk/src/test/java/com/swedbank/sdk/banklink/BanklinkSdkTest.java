package com.swedbank.sdk.banklink;

import android.content.Context;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.swedbank.sdk.banklink.Extensions.NPE_MESSAGE_END;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"CheckResult", "ConstantConditions"})
public class BanklinkSdkTest {
  @Test
  public void nullContextThrows() {
    try {
      BanklinkSdk.createClient(null, BanklinkApi.Country.EE);
      fail("NPE not thrown for null context");
    } catch (NullPointerException e) {
      assertThat(e.getMessage()).isEqualTo("context" + NPE_MESSAGE_END);
    }
  }

  @Test
  public void nullLocaleThrows() {
    try {
      BanklinkSdk.createClient(mock(Context.class), null);
      fail("NPE not thrown for null locale");
    } catch (NullPointerException e) {
      assertThat(e.getMessage()).isEqualTo("country" + NPE_MESSAGE_END);
    }
  }

  @Test
  public void clientUsesApplicationContext() {
    final Context context = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(context);
    BanklinkSdk.createClient(context, BanklinkApi.Country.EE);
    verify(context).getApplicationContext();
  }
}
