package com.abcgroup.demoplui

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class ApiManager {
    private val apiService: ApiService

    init {
        val client = OkHttpClient()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://paymentloungeapiuat.adityabirlacapital.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    suspend fun encryptPaymentData(oId : String, context : Context): String? {
        val mediaType = "application/json".toMediaType()
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        val currentTime = Date()
        val timestamp = dateFormat.format(currentTime)
        val paymentData = PaymentData(
            oId,
            timestamp,
            "MRC7734",
            "100.00",
            "https://paymentloungeapiuat.adityabirlacapital.com/resultpage",
            "policy",
            "PROD34352",
            "CUST567",
            "Ramesh Prasad Kulkarni",
            "guest@phicommerce.com",
            "919876543210",
            "",
            "",
            "INV745497",
            "",
            "12345678",
            "sdh456"
        )

        val paymentRequest = PaymentRequest(
            Content(paymentData),
            "7x!A%D*G-JaNdRgUkXp2s5v8y/B?E(H+",
            "ABCDEFGHIJKLMNOP"
        )

        val request = apiService.encryptPaymentData(paymentRequest)
        var eString:  String? = null
        request.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val responseString = responseBody.string()
                        eString = responseBody.string()
                        println("Response String: $responseString")
                    } else {
                        println("Response body is null")
                    }
                } else {
                    println("Response Code: ${response.code()}")
                    Toast.makeText(context,response.message().toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Handle network or request error
                Log.i("OnFailure",call.toString())
                Log.i("OnFailure",t.toString())
                Toast.makeText(context,t.toString(), Toast.LENGTH_SHORT).show()


            }
        })

        delay(4000)
        return eString
    }


    suspend fun decryptData(eData: String, context: Context) : String? {
            val mediaType = "application/json".toMediaType()
            val decryptRequest = DecryptRequest(
                eData,  "7x!A%D*G-JaNdRgUkXp2s5v8y/B?E(H+",
                "ABCDEFGHIJKLMNOP"
            )
            val request = apiService.decryptData(decryptRequest)
            var paymentLink: String? = null
            request.enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                    Log.i("Decrypt Request", call.toString())
                    Log.i("Decrypt Request", response.toString())
                    if (response.isSuccessful) {
                        val responseBody = response.body()

                        if (responseBody != null) {
                            try {
                                val responseString = responseBody.string()
                                val json = JSONObject(responseString)
                                val orderData = json.getJSONObject("order_data")
                                val links = orderData.getJSONObject("links")
                                paymentLink = links.getString("payment_link_web")
                                println("Response Data: $paymentLink")
                            } catch (e: IOException) {
                                // Handle any IOException that may occur while reading the response
                                e.printStackTrace()
                            }
                        } else {
                            // Handle the case where the response body is null
                            println("Response body is null")
                        }
                    } else {
                        // Handle API error (non-2xx response)
                        println("Response Code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Handle network or request error
                    Toast.makeText(context,t.toString(), Toast.LENGTH_SHORT).show()
                }
            })
        delay(2000)
        return paymentLink
        }
    }
