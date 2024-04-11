package com.abcgroup.demoplui

data class YourResponseModel(val responseString: String)

data class PaymentRequest(
    val content: Content,
    val key: String,
    val iv: String
)



data class DecryptRequest(
    val content: String,
    val key: String,
    val iv: String
)

data class CheckStatusRequest(
    val content: CheckStatusContent,
    val key: String,
    val iv: String
)

data class CheckStatusContent(
    val merc_order_id: String?,
    val merc_id: String
)

data class CreateOrderRequest(
    val content: String
)


data class Content(
    val payment_data: PaymentData
)



data class PaymentData(
    val merc_order_id: String,
    val merc_order_date: String,
    val merc_id: String,
    val amount: String,
    val return_url: String,
    val product_type: String,
    val product_id: String,
    val customer_id: String,
    val customer_name: String,
    val customer_email: String,
    val customer_phone: String,
    val udf1: String,
    val udf2: String,
    val invoice_no: String,
    val order_desc: String,
    val account_no: String,
    val account_ifsc: String
)
