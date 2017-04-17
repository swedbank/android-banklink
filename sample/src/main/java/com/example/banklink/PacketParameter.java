package com.example.banklink;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PacketParameter {
  public abstract String id();

  public abstract String value();

  public abstract int maxLength();

  @NonNull
  @CheckResult
  public static Builder builder(@NonNull String packetId) {
    return new AutoValue_PacketParameter.Builder()
        .id(packetId)
        .value("")
        .maxLength(0);
  }

  @AutoValue.Builder
  public static abstract class Builder {
    abstract Builder id(String id);

    public abstract Builder value(String value);

    public abstract Builder maxLength(int maxLength);

    @NonNull
    @CheckResult
    public abstract PacketParameter build();
  }
}
