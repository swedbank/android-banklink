package com.example.banklink.network;

import java.util.Map;

import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rx.Observable;

public interface SignService {
  @FormUrlEncoded
  @Headers("Accept:application/json")
  @POST("banklink-test/")
  Observable<Map<String, String>> sign(@FieldMap Map<String, String> fields);
}
