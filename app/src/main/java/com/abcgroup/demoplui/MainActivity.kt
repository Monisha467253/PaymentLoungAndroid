@file:Suppress("DEPRECATION")

package com.abcgroup.demoplui


import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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


private fun randomID(): String = List(10) {
    (('A'..'Z') + ('0'..'9')).random()
}.joinToString("")

class MainActivity : AppCompatActivity() {

    var mPrefs: SharedPreferences? = null
    var enterOrderAmount: EditText? = null
    var enterMerchantId: EditText? = null
    var clickCreateOrder: TextView? = null
    var clickMakePayment: TextView? = null
    var clickCheckTranStatus: TextView? = null
    var clickReset: TextView? = null
    var orderID: String? = null
    var encryptedString: String? = null
    var paymentUrl: String? = null
    var transactionStatus: String? = null
    var transactionID: String? = null
    var transStatusCode: String? = null
    var orderData: TextView? = null


    private var loadingPB: ProgressBar? = null
    private var apiService: ApiService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val scope = CoroutineScope(Dispatchers.IO)

        val client = OkHttpClient()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://paymentloungeapiuat.adityabirlacapital.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        mPrefs = getPreferences(MODE_PRIVATE)
        apiService = retrofit.create(ApiService::class.java)

        enterOrderAmount = findViewById<EditText>(R.id.enterOrderAmount)
        enterMerchantId = findViewById<EditText>(R.id.enterMerchantID)
        clickCreateOrder = findViewById<Button>(R.id.createOrder)
        clickMakePayment = findViewById<Button>(R.id.makePayment)
        clickCheckTranStatus = findViewById<Button>(R.id.checkTransStatus)
        clickReset = findViewById<Button>(R.id.resetButton)
        orderData =  findViewById<TextView>(R.id.orderData)

        loadingPB = findViewById(R.id.idLoadingPB);

        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                // Check if the URL contains the desired path
                val url = request.url.toString()
                Log.i("redirects", url.contains("api/v1/payphi/processRedirectResponse").toString())
                Log.i("pg redirects", url.contains("pg/api/pgresponse?acqID=").toString())
                Log.i("pg redirects", url.contains("upi://pay?pa=").toString())
                Log.i("pg redirects", url)
                Log.i("pg redirects", url.contains("resultpage?pl_txn_id=").toString())
                if (url.contains("api/v1/payphi/processRedirectResponse")) {
                    webView.visibility = WebView.GONE
                    return true
                } else if (url.contains("pg/api/pgresponse?acqID=")) {
                    webView.visibility = WebView.GONE
                    return true
                } else if (url.contains("resultpage?pl_txn_id=")) {
                    webView.visibility = WebView.GONE
                    return true
                } else if(url.contains("upi://pay?pa=payphi")){
//                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                    if (intent.resolveActivity(packageManager) != null) {
//                        startActivity(intent)
//                    } else {
//                    }
                    Log.i("UPI App Pay", url.contains("upi://pay?pa=payphi").toString() )
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.data = Uri.parse(url)
                    val chooser = Intent.createChooser(intent, "Pay with...")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        startActivityForResult(chooser, 1, null)
                    }
                    return true;
                }
                return false
            }
        }

            clickMakePayment!!.setOnClickListener {
                val urlToLoad = mPrefs!!.getString("paymentUrlString", "").toString()
                println(urlToLoad)
                if (urlToLoad != null && urlToLoad != "") {
                    webView.loadUrl(urlToLoad)
                    webView.visibility = WebView.VISIBLE
                } else{
                    Toast.makeText(applicationContext,"Payment URL is null or Create new order", Toast.LENGTH_SHORT).show()
                }
            }

        clickCreateOrder!!.setOnClickListener{
            Log.i("no",enterMerchantId!!.text.isNotEmpty().toString())
            Log.i("no",enterOrderAmount!!.text.isNotEmpty().toString())
            val progress = ProgressDialog(this)
            progress.setTitle("Loading")
            progress.setMessage("Creating Order...")
            progress.setCancelable(false)
            progress.show()
            if(enterMerchantId!!.text.isNotEmpty() && enterOrderAmount!!.text.isNotEmpty()){
                orderID = "AB" + randomID()
                Log.i("order ID", orderID!!)
                scope.launch{

                    encryptPaymentData(orderID!!, applicationContext)
                    delay(4000)
                    Log.i("Shared Pref Data",mPrefs!!.getString("encryptedString","").toString())
                    createOrderData(mPrefs!!.getString("encryptedString","").toString(), applicationContext)
                    delay(4000)
                    Log.i("Payment URL Data",mPrefs!!.getString("createOrderString","").toString())
                    decryptData(mPrefs!!.getString("createOrderString","").toString(), applicationContext)
                    delay(4000)
                    Log.i("Payment URL Data",mPrefs!!.getString("paymentUrlString","").toString())

                }

                Handler().postDelayed({
                    val s = mPrefs!!.getString("paymentUrlString","").toString()
                    val stringbuilder = StringBuilder()

                    stringbuilder.append("Order ID : ").append(orderID)
                    stringbuilder.append("\n\n")
                    stringbuilder.append("Order Created : ").append("Successfully")
                    stringbuilder.append("\n\n")
                    stringbuilder.append("Payment Link : ").append(s)
                    stringbuilder.append("\n\n")
                    orderData!!.text = stringbuilder.toString()
                    orderData!!.movementMethod = ScrollingMovementMethod()
                    progress.dismiss()
                }, 12000)
            } else {
                progress.dismiss()
                Toast.makeText(applicationContext,"Merchant ID & Order Amount must be empty", Toast.LENGTH_SHORT).show()
                Log.i("Merchant ID & Order Amount must be empty", "False")
            }
        }

        clickCheckTranStatus!!.setOnClickListener {

            val progress = ProgressDialog(this)
            progress.setTitle("Loading")
            progress.setMessage("Check Transaction Status...")
            progress.setCancelable(false)
            progress.show()
            if(orderID != null && orderID != ""){
                scope.launch {
                    encryptCheckStatus(orderID!!, applicationContext)
                    delay(4000)
                    Log.i("Shared Pref Data", mPrefs!!.getString("checkOrderStatus", "").toString())
                    queryCheckStatus(
                        mPrefs!!.getString("checkOrderStatus", "").toString(),
                        applicationContext
                    )
                    delay(4000)
                    Log.i("Query Order Data", mPrefs!!.getString("queryCheckStatus", "").toString())
                    decryptCheckStatus(
                        mPrefs!!.getString("queryCheckStatus", "").toString(),
                        applicationContext
                    )
                    delay(4000)
//                    Log.i("Payment URL Data",mPrefs!!.getString("paymentUrlString","").toString())

                }

                Handler().postDelayed({
                    val s = mPrefs!!.getString("paymentUrlString", "").toString()

                    val tId = mPrefs!!.getString("trnId", "").toString()

                    val pOrderID = mPrefs!!.getString("plOrderId", "").toString()

                    val tStatus = mPrefs!!.getString("trnStatus", "").toString()

                    val tStatusCode = mPrefs!!.getString("trnStatusCode", "").toString()

                    val stringbuilder = StringBuilder()

                    stringbuilder.append("Order ID : ").append(orderID)
                    stringbuilder.append("\n\n")
                    stringbuilder.append("Order Created : ").append("Successfully")
                    stringbuilder.append("\n\n")
                    stringbuilder.append("Payment Link : ").append(s)
                    stringbuilder.append("\n\n")
                    stringbuilder.append("Trn Id : ").append(tId)
                    stringbuilder.append("\n\n")
                    stringbuilder.append("PL Trn Id : ").append(pOrderID)
                    stringbuilder.append("\n\n")
                    stringbuilder.append("Tran Status : ").append(tStatus)
                    stringbuilder.append("\n\n")
                    stringbuilder.append("Tran Status Code : ").append(tStatusCode)
                    stringbuilder.append("\n\n")
                    orderData!!.text = stringbuilder.toString()
                    orderData!!.movementMethod = ScrollingMovementMethod()
                    progress.dismiss()
                }, 12000)
            }
            else {
                progress.dismiss()
                Toast.makeText(applicationContext,"Order ID is empty. Create new order and once again check payment status", Toast.LENGTH_SHORT).show()

            }
            }

        clickReset!!.setOnClickListener{
            mPrefs!!.edit().remove("paymentUrlString").commit()
            mPrefs!!.edit().remove("encryptedString").commit()
            mPrefs!!.edit().remove("createOrderString").commit()
            mPrefs!!.edit().remove("checkOrderStatus").commit()
            mPrefs!!.edit().remove("queryCheckStatus").commit()
            mPrefs!!.edit().remove("trnId").commit()
            mPrefs!!.edit().remove("plOrderId").commit()
            mPrefs!!.edit().remove("trnStatus").commit()
            mPrefs!!.edit().remove("trnStatusCode").commit()
            orderData!!.text = ""
        }
    }

     fun encryptPaymentData(oId : String, context : Context){
//        val mediaType = "application/json".toMediaType()
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        val currentTime = Date()
        val timestamp = dateFormat.format(currentTime)
        val paymentData = PaymentData(
            oId,
            timestamp,
            "MRC7734",
            enterOrderAmount!!.text.toString()+".00",
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

        val request = apiService!!.encryptPaymentData(paymentRequest)
        request.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val prefsEditor: SharedPreferences.Editor = mPrefs!!.edit()
                        val responseString = responseBody.string()
                        Log.i("rString", responseString)
                        encryptedString = responseBody.string()
                        println("Response String: $encryptedString")
                        prefsEditor.putString("encryptedString", responseString)
                        prefsEditor.commit()

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
    }

    fun createOrderData(eData: String, context: Context) {

        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        val currentTime = Date()
        val timestamp = dateFormat.format(currentTime)

        Log.i("Query Check Status eData", eData)

        val rnds = (23454..5565756).random()

        val header = HashMap<String, String>()
        header["Accept"] = "*/*"
        header["Content-Type"] = "application/json"
        header["Authorization"] = "userToken"
        header["pl-merc-id"] = "MRC7734"
        header["pl-traceid"] = rnds.toString()
        header["pl-timestamp"] = timestamp

        val createOrderRequest = CreateOrderRequest(
            eData
        )
        val request = apiService!!.createOrderData(header, createOrderRequest)
        request.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                Log.i("Create Order Request", response.toString())
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if (responseBody != null) {
                        try {
                            val prefsEditor: SharedPreferences.Editor = mPrefs!!.edit()
                            val responseString = responseBody.string()
                            var v = responseString.replace("{\"content\":\"", "").replace("\"}","")
                             prefsEditor.putString("createOrderString", v)
                            prefsEditor.commit()
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
    }

     fun decryptData(eData: String, context: Context) {
        val mediaType = "application/json".toMediaType()
        val decryptRequest = DecryptRequest(
            eData,  "7x!A%D*G-JaNdRgUkXp2s5v8y/B?E(H+",
            "ABCDEFGHIJKLMNOP"
        )
        val request = apiService!!.decryptData(decryptRequest)
        request.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                Log.i("Decrypt Request", call.toString())
                Log.i("Decrypt Request", response.toString())
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            val prefsEditor: SharedPreferences.Editor = mPrefs!!.edit()
                            val responseString = responseBody.string()
                            println("Decrypt Response: $responseString")
                            val json = JSONObject(responseString)
                            val paymentData = json.getJSONObject("payment_data")
                            val orderData = json.getJSONObject("order_data")
                            val links = orderData.getJSONObject("links")
                            paymentUrl = links.getString("payment_link_web")
                            prefsEditor.putString("paymentUrlString", links.getString("payment_link_web"))
                            prefsEditor.commit()
                            println("Payment URL Data: $paymentUrl")
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
    }

    fun encryptCheckStatus(oId : String, context : Context){
        val mediaType = "application/json".toMediaType()
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        val currentTime = Date()
        val timestamp = dateFormat.format(currentTime)
        val orderData = CheckStatusContent(
           oId,
            "MRC7734"
        )

        val paymentRequest = CheckStatusRequest(
            orderData,
            "7x!A%D*G-JaNdRgUkXp2s5v8y/B?E(H+",
            "ABCDEFGHIJKLMNOP"
        )

        val request = apiService!!.encryptCheckStatus(paymentRequest)
        request.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val prefsEditor: SharedPreferences.Editor = mPrefs!!.edit()
                        val responseString = responseBody.string()
                        Log.i("rString", responseString)
                        encryptedString = responseBody.string()
                        println("Response String: $encryptedString")
                        prefsEditor.putString("checkOrderStatus", responseString)
                        prefsEditor.commit()

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
    }

    fun queryCheckStatus(eData: String, context: Context) {

        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        val currentTime = Date()
        val timestamp = dateFormat.format(currentTime)
        val createOrderRequest = CreateOrderRequest(
            eData
        )
        Log.i("Query Check Status eData", eData)

        val rnds = (23454..5565756).random()
//        23454, 5565756

        val header = HashMap<String, String>()
        header["Accept"] = "*/*"
        header["Content-Type"] = "application/json"
        header["Authorization"] = "userToken"
        header["pl-merc-id"] = "MRC7734"
        header["pl-traceid"] = rnds.toString()
        header["pl-timestamp"] = timestamp

        val request = apiService!!.queryCheckStatus(header, createOrderRequest)
        request.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                Log.i("query Check Status Request", call.request().toString())
                Log.i("query Check Status Response", response.toString())
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            val prefsEditor: SharedPreferences.Editor = mPrefs!!.edit()
                            val responseString = responseBody.string()
//                            println("Create Order Response: $responseString")
//                            val json = JSONObject(responseString)
//                            val ctnt = json.getJSONObject("content")
                            var v = responseString.replace("{\"content\":\"", "").replace("\"}","")
                            println("Create Order Response: ${responseString.replace("{\"content\":\"", "").replace("\"}","")}")
                            prefsEditor.putString("queryCheckStatus", v)
                            prefsEditor.commit()
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
    }

    fun decryptCheckStatus(eData: String, context: Context) {
//        var e = "+8MJSEqyIDfBQhy+uK6vaU/F8VPQgMdj9MTvRvGb//p0fIuWhl1GohUUbKCB6AuNy3f3XoYhMvxk5ncpqqIoM47Cb1S2SrvoYBT4DIZ5EB1x1FdJwkR7kpqZIovkNh2SNmlhVW8zJiBVc4Tyroq4ZyIBevtI53OECAdkAqaS5CtrvabBUS2HDMnpw0cOB3Yjg7SKHAsUahJEyaC9S2XzTrboue8gfdtsxKOOjdHfNHVegsP7PKL6O2/dH+R0cYVAz5CEJhYWu5lavhjI4HfPY/aNPyVAXuf2YguJTBkovFt3ug2URdEZ+mmF+Mx8Wzg899n9euGi5yMNTr6wFM2v3fWZcWFvZQzj/b9VxQiS/sYXvnSrCeOx9Ugw+Jz+oY3edo21x5SOEj4RYcdYnHA9QNhgMPQBS6io6/Q0JHMWDab7ZHA8lpj0uHmzjZ2uWKgtjAS9y+OONC5T9ccdhICAgULOfXCnTe1G0U5GGsldvAgTUpXpvWe5V/d1/wQeFU7alIK6LVGPZBAmGZA7uMlNutthNM6ebTXyquBHa98TLKOqih4BKON82DJXpyde5MnF3sXLHvRA5UU1Y58kFFtg+iaxDdt627qI/m6yN0DSJlCxkrRY5AuURa1P+w5b/mmqB9ssvXhanzQh0jB0J18lyq1ECRRjRTjZ1LuqPspLdq2FbiDjGSYi9Fuxmu1eHe5QywQ2n62ealyiWqtVuSng9MJe94yiaO6islC+IGotkqkIMaH/OkwGHdNGlFGaV+FkiM7aANYs17o+cyHa5MyXCuVqCE7nrfMIxqw+31Mrs+69Kw0Omr4g2eJWKpI2zajOrM9VHdpZF0/lUikWYEv2/AayhQUwH7qKMD3DjRFb6V9hG5cx7Yp8E+MxxCuYSlT+zimYKfK0DCcgN7883BIUGOzAMLqU1sqW5jwt+9boFy4Um6T3osjQDfcDetWstEufNco8tyoS9GoQ4HlaoizuPd6vctzNoY5mRd/7MBmMdOE3Q//XnlNr920NHWPDi8jaIZiD5NT7bAR8RFx4e2MGr+loVYctr38L0kXdN/W5FR9Kt+EppVIzff+jWk9uga7Q6Uvl400JozYrbVbg/3HjQEXlWaOcPYd3nJRHSf7fO0aiVcgkFi4eGliax7yfQvTYvYj4qU6tJrxZHCTA1iP0zt0+Q1BjdmTRyBuK4zdpNB+I+03j8oXC86giIfGzyGwzW0TiL6OZsVhGmEbN6W/NmHb3niZuuGJqV+sNRyQDJrdspevm62nBxnzojC5DgoNMyjHcmqV7dPo1LVjVuUgw3lgFZXQUukDhxOV6zcwRYgnK9IYYf/yvJuJBLHtltgiEOcFxt7CjrEAvDLfkce7WsjV5mjWlJRV7GuqTowlIKqOr21Lmjg5d20UY83zV9QNs9w8QfEJa6obXw4nozgwSar5HVpPlxtnfbydpGHjn6L5+hA3NGStQR7EiW0hKztvyuqS34pj1"
        val decryptRequest = DecryptRequest(
            eData,  "7x!A%D*G-JaNdRgUkXp2s5v8y/B?E(H+",
            "ABCDEFGHIJKLMNOP"
        )
        val request = apiService!!.decryptData(decryptRequest)
        request.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                Log.i("Decrypt Request", call.toString())
                Log.i("Decrypt Request", response.toString())
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            val prefsEditor: SharedPreferences.Editor = mPrefs!!.edit()


                            val responseString = responseBody.string()
                            println("Decrypt Response: $responseString")
                            val json = JSONObject(responseString)
                            val txnList = json.getJSONObject("order_data").getJSONArray("txn_list")
                            val txnData = txnList.getJSONObject(0)
//                            val paymentData = json.getJSONObject("order_data")
//                            val orderData = paymentData.getJSONObject("txn_list")
                            val trnId = txnData.getString("pl_txn_id")
                            val plOrderId = txnData.getString("pl_order_id")
                            val trnStatus = txnData.getString("txn_status")
                            val trnStatusCode = txnData.getString("txn_status_code")

                            prefsEditor.putString("trnId", trnId.toString())
                            prefsEditor.putString("plOrderId", plOrderId.toString())
                            prefsEditor.putString("trnStatus", trnStatus.toString())
                            prefsEditor.putString("trnStatusCode", trnStatusCode.toString())

                            prefsEditor.commit()
                            println("Trn Id: $trnId")
                            println("pl Order Id: $plOrderId")
                            println("Trn Status: $trnStatus")
                            println("Trn Status Code: $trnStatusCode")
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
    }



}