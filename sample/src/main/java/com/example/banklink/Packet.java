package com.example.banklink;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import static com.example.banklink.BuildConfig.VK_CANCEL_URL;
import static com.example.banklink.BuildConfig.VK_RETURN_URL;
import static com.example.banklink.PacketParameter.builder;

public enum Packet {
  P_1001(1001,
      builder("VK_ENCODING").value("UTF-8").maxLength(12).build(),
      builder("VK_VERSION").value("008").maxLength(3).build(),
      builder("VK_SND_ID").value("AADUPOOD").maxLength(15).build(),
      builder("VK_STAMP").value("123456").maxLength(20).build(),
      builder("VK_AMOUNT").value("0.01").maxLength(12).build(),
      builder("VK_CURR").value("EUR").maxLength(3).build(),
      builder("VK_ACC").value("221010223122").maxLength(34).build(),
      builder("VK_PANK").value("").maxLength(255).build(),
      builder("VK_NAME").value("AADUPOOD").maxLength(70).build(),
      builder("VK_REF").value("123").maxLength(35).build(),
      builder("VK_MSG").value("o").maxLength(95).build(),
      builder("VK_RETURN").value(VK_RETURN_URL).maxLength(255).build(),
      builder("VK_LANG").value("ENG").maxLength(3).build()
  ),
  P_1002(1002,
      builder("VK_ENCODING").value("UTF-8").maxLength(12).build(),
      builder("VK_VERSION").value("008").maxLength(3).build(),
      builder("VK_SND_ID").value("AADUPOOD").maxLength(15).build(),
      builder("VK_STAMP").value("123456").maxLength(20).build(),
      builder("VK_AMOUNT").value("0.01").maxLength(12).build(),
      builder("VK_CURR").value("EUR").maxLength(3).build(),
      builder("VK_REF").value("123").maxLength(35).build(),
      builder("VK_MSG").value("o").maxLength(95).build(),
      builder("VK_RETURN").value(VK_RETURN_URL).maxLength(255).build(),
      builder("VK_LANG").value("ENG").maxLength(3).build()
  ),
  P_1003(1003,
      builder("VK_ENCODING").value("ISO-8859-1").maxLength(12).build(),
      builder("VK_VERSION").value("008").maxLength(3).build(),
      builder("VK_SND_ID").value("AADUPOOD").maxLength(15).build(),
      builder("VK_STAMP").value("123456").maxLength(20).build(),
      builder("VK_AMOUNT").value("0.01").maxLength(12).build(),
      builder("VK_CURR").value("EUR").maxLength(3).build(),
      builder("VK_ACC").value("").maxLength(34).build(),
      builder("VK_PANK").value("").maxLength(255).build(),
      builder("VK_NAME").value("AADUPOOD").maxLength(70).build(),
      builder("VK_REF").value("123").maxLength(35).build(),
      builder("VK_MSG").value("o").maxLength(95).build(),
      builder("VK_RETURN").value(VK_RETURN_URL).maxLength(255).build(),
      builder("VK_LANG").value("ENG").maxLength(3).build()
  ),
  P_1011(1011,
      builder("VK_ENCODING").value("UTF-8").maxLength(12).build(),
      builder("VK_VERSION").value("008").maxLength(3).build(),
      builder("VK_SND_ID").value("TAADUPOOD").maxLength(15).build(),
      builder("VK_STAMP").value("123456").maxLength(20).build(),
      builder("VK_AMOUNT").value("0.01").maxLength(12).build(),
      builder("VK_CURR").value("EUR").maxLength(3).build(),
      builder("VK_ACC").value("EE892200221010223122").maxLength(34).build(),
      builder("VK_PANK").value("").maxLength(255).build(),
      builder("VK_NAME").value("AADUPOOD").maxLength(70).build(),
      builder("VK_REF").value("123").maxLength(35).build(),
      builder("VK_MSG").value("o").maxLength(95).build(),
      builder("VK_RETURN").value(VK_RETURN_URL).maxLength(255).build(),
      builder("VK_CANCEL").value(VK_CANCEL_URL).maxLength(255).build(),
      builder("VK_LANG").value("ENG").maxLength(3).build()),
  P_1012(1012,
      builder("VK_ENCODING").value("ISO-8859-1").maxLength(12).build(),
      builder("VK_VERSION").value("008").maxLength(3).build(),
      builder("VK_SND_ID").value("AADUPOOD").maxLength(15).build(),
      builder("VK_STAMP").value("123456").maxLength(20).build(),
      builder("VK_AMOUNT").value("0.01").maxLength(12).build(),
      builder("VK_CURR").value("EUR").maxLength(3).build(),
      builder("VK_REF").value("123").maxLength(35).build(),
      builder("VK_MSG").value("o").maxLength(95).build(),
      builder("VK_RETURN").value(VK_RETURN_URL).maxLength(255).build(),
      builder("VK_CANCEL").value(VK_CANCEL_URL).maxLength(255).build(),
      builder("VK_LANG").value("ENG").maxLength(3).build()),
  ;

  public final int packetId;
  @NonNull
  public final PacketParameter[] parameters;

  Packet(int packetId, @NonNull @Size(min = 1) PacketParameter... parameters) {
    this.packetId = packetId;
    this.parameters = parameters;
  }

  public static CharSequence[] asList() {
    final Packet[] values = values();
    final int valuesLen = values.length;
    final CharSequence[] output = new CharSequence[valuesLen];
    for (int i = 0; i < valuesLen; i++) {
      output[i] = Integer.toString(values[i].packetId);
    }
    return output;
  }

  public static Packet fromSelection(@NonNull Integer selection) {
    if (selection < 0) {
      return values()[0];
    }
    return values()[selection];
  }
}
