package com.abcgroup.demoplui

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.Date

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("dev/encrypt")
    fun encryptPaymentData(@Body request: PaymentRequest): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("dev/decrypt")
    fun decryptData(@Body request: DecryptRequest): Call<ResponseBody>

//    @Headers("Content-Type: application/json","pl-merc-id:MRC7734", "pl-traceid:3628342","pl-timestamp: 20230709172132")
    @POST("v1/order/create")
    fun createOrderData(@HeaderMap headers: Map<String, String>, @Body request: CreateOrderRequest): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("dev/encrypt")
    fun encryptCheckStatus(@Body request: CheckStatusRequest): Call<ResponseBody>

//    @Headers("Content-Type: application/json","pl-merc-id:MRC7734", "pl-traceid:1970441","pl-timestamp: 20230908045140")
    @POST("v1/order/query")
    fun queryCheckStatus(@HeaderMap headers: Map<String, String>, @Body request: CreateOrderRequest): Call<ResponseBody>
}
