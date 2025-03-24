package com.example.flutter_braintree_plugin

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import com.braintreepayments.api.BraintreeClient
import com.braintreepayments.api.PayPalCheckoutRequest
import com.braintreepayments.api.PayPalClient
import com.braintreepayments.api.PayPalLineItem
import com.braintreepayments.api.PayPalPaymentIntent
import com.braintreepayments.api.PayPalRequest
import com.braintreepayments.api.PayPalVaultRequest
import com.braintreepayments.api.PostalAddress

import com.example.flutter_braintree_plugin.FlutterBraintreePluginHelper
import java.util.ArrayList

/** FlutterBraintreePlugin */
class FlutterBraintreePlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private var context: Context? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_braintree_plugin")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    val authorization = FlutterBraintreePluginHelper.getAuthorization(call)
    if (authorization == null) {
      FlutterBraintreePluginHelper.returnAuthorizationMissingError(result)
      return
    }
    val apiClient = context?.let { BraintreeClient(it, authorization) }
    if (apiClient == null) {
      FlutterBraintreePluginHelper.returnAuthorizationMissingError(result)
      return
    }

    if (call.method == "tokenizePayPalAccount") {
      tokenizePayPalAccount(call, apiClient, result)
      return
    } else if (call.method == "tokenizeVenmoAccount") {
      tokenizeVenmoAccount(call, apiClient, result)
      return
    } else if (call.method == "fetchPaymentMethodNonces") {
      val defaultFirst = FlutterBraintreePluginHelper.bool("defaultFirst", call) ?: true
      fetchPaymentMethodNonces(apiClient, defaultFirst, result)
      return
    } else if (call.method == "deletePaymentMethodNonce") {
      val nonce = FlutterBraintreePluginHelper.string("payment_nonce", call)
      if (nonce == null) {
        result(mapOf(
          "error" to mapOf(
            "code" to "-1",
            "message" to "Payment method does not exist"
          )
        ))
        return
      }
      deletePaymentMethodNonce(apiClient, nonce, result)
      return
    }
    result.notImplemented()
  }

  private fun tokenizeVenmoAccount(call: MethodCall, apiClient: BTAPIClient, result: Result) {
    val driver = BTVenmoDriver(apiClient)

    val venmoInfo = FlutterBraintreePluginHelper.dict("request", call) ?: run {
      FlutterBraintreePluginHelper.returnFlutterError(result, "-1", "Missing request parameters")
      return
    }

    val venmoRequest = BTVenmoRequest()
    venmoRequest.profileID = venmoInfo["profileID"] as? String
    val vault = venmoInfo["vault"] as? Boolean
    vault?.let { venmoRequest.vault = it }
    val paymentMethodUsage = venmoInfo["paymentMethodUsage"] as? Int
    when (paymentMethodUsage) {
      1 -> venmoRequest.paymentMethodUsage = BTVenmoPaymentMethodUsage.unspecified
      2 -> venmoRequest.paymentMethodUsage = BTVenmoPaymentMethodUsage.singleUse
      else -> venmoRequest.paymentMethodUsage = BTVenmoPaymentMethodUsage.multiUse
    }
    venmoRequest.displayName = venmoInfo["displayName"] as? String
    val collectCustomerBillingAddress = venmoInfo["collectCustomerBillingAddress"] as? Boolean
    collectCustomerBillingAddress?.let { venmoRequest.collectCustomerBillingAddress = it }
    val collectCustomerShippingAddress = venmoInfo["collectCustomerShippingAddress"] as? Boolean
    collectCustomerShippingAddress?.let { venmoRequest.collectCustomerShippingAddress = it }
    venmoRequest.subTotalAmount = venmoInfo["subTotalAmount"] as? String
    venmoRequest.totalAmount = venmoInfo["totalAmount"] as? String
    venmoRequest.discountAmount = venmoInfo["discountAmount"] as? String
    venmoRequest.shippingAmount = venmoInfo["shippingAmount"] as? String
    venmoRequest.taxAmount = venmoInfo["taxAmount"] as? String
    val lineItems = venmoInfo["lineItems"] as? List<Any>
    lineItems?.let { venmoRequest.lineItems = FlutterBraintreePluginHelper.makeVenmoItems(it) }

    driver.tokenizeVenmoAccount(venmoRequest) { nonce, error in
      guard error == nil else {
        result(mapOf(
          "error" to mapOf(
            "message" to (error as? NSError)?.localizedDescription,
            "code" to (error as? NSError)?.code
          )
        ))
        return
      }
      result(mapOf(
        "data" to FlutterBraintreePluginHelper.buildPaymentNonceDict(nonce)
      ))
    }
  }

  private fun tokenizePayPalAccount(call: MethodCall, apiClient: BraintreeClient, result: Result) {
    val driver = PayPalClient(apiClient)

    val requestInfo = FlutterBraintreePluginHelper.dict("request", call) ?: run {
      FlutterBraintreePluginHelper.returnFlutterError(result, "-1", "Missing request parameters")
      return
    }

    val vault = requestInfo["vault"] as? Boolean ?: run {
      FlutterBraintreePluginHelper.returnFlutterError(result, "-1", "Missing request parameters")
      return
    }

    val paypalRequest:PayPalRequest
    if (vault) {
      val paypalVaultRequest = PayPalVaultRequest(true)
      val offerCredit = requestInfo["offerCredit"] as? Boolean
      if (offerCredit != null) {
        paypalVaultRequest.shouldOfferCredit = offerCredit
      }
      paypalRequest = paypalVaultRequest
    } else {
      val amount = requestInfo["amount"] as? String ?: "0"
      val paypalCheckoutRequest = PayPalCheckoutRequest(amount, true)
      paypalCheckoutRequest.currencyCode = requestInfo["currencyCode"] as? String
      val intent = requestInfo["intent"] as? Int
      when (intent) {
        1 -> paypalCheckoutRequest.intent = PayPalPaymentIntent.SALE
        2 -> paypalCheckoutRequest.intent = PayPalPaymentIntent.ORDER
        else -> paypalCheckoutRequest.intent = PayPalPaymentIntent.AUTHORIZE
      }
      val userAction = requestInfo["userAction"] as? Int
      when (userAction) {
        1 -> paypalCheckoutRequest.userAction = PayPalCheckoutRequest.USER_ACTION_COMMIT
        else -> paypalCheckoutRequest.userAction = PayPalCheckoutRequest.USER_ACTION_DEFAULT
      }
      val requestBillingAgreement = requestInfo["requestBillingAgreement"] as? Boolean
      requestBillingAgreement?.let { paypalCheckoutRequest.shouldRequestBillingAgreement = it }
      val offerPayLater = requestInfo["offerPayLater"] as? Boolean
      offerPayLater?.let { paypalCheckoutRequest.shouldOfferPayLater = it }
      paypalRequest = paypalCheckoutRequest
    }

    paypalRequest.displayName = requestInfo["displayName"] as? String
    val shippingAddressRequired = requestInfo["shippingAddressRequired"] as? Boolean
    shippingAddressRequired?.let { paypalRequest.isShippingAddressRequired = it }
    val billingAgreementDescription = requestInfo["billingAgreementDescription"] as? String
    billingAgreementDescription?.let { paypalRequest.billingAgreementDescription = it }
    val shippingAddressOverride = requestInfo["shippingAddressOverride"] as? Map<String, Any>
    shippingAddressOverride?.let {
      val address = PostalAddress()
        address.recipientName = it["recipientName"] as? String
        address.streetAddress = it["streetAddress"] as? String
        address.extendedAddress = it["extendedAddress"] as? String
        address.locality = it["locality"] as? String
        address.region = it["region"] as? String
        address.postalCode = it["postalCode"] as? String
        address.countryCodeAlpha2 = it["countryCodeAlpha2"] as? String
        paypalRequest.shippingAddressOverride = address
    }
    val shippingAddressEditable = requestInfo["shippingAddressEditable"] as? Boolean
    shippingAddressEditable?.let { paypalRequest.isShippingAddressEditable = it }
    val localeCode = requestInfo["localeCode"] as? String
    localeCode?.let { paypalRequest.localeCode = it }
    val merchantAccountID = requestInfo["merchantAccountID"] as? String
    merchantAccountID?.let { paypalRequest.merchantAccountId = it }
    val riskCorrelationId = requestInfo["riskCorrelationId"] as? String
    riskCorrelationId?.let { paypalRequest.riskCorrelationId = it }

    val landingPageType = requestInfo["landingPageType"] as? Int
    when (landingPageType) {
      1 -> paypalRequest.landingPageType = PayPalRequest.LANDING_PAGE_TYPE_LOGIN
      2 -> paypalRequest.landingPageType = PayPalRequest.LANDING_PAGE_TYPE_BILLING
      else -> paypalRequest.landingPageType = PayPalRequest.LANDING_PAGE_TYPE_LOGIN
    }

    val lineItems = requestInfo["lineItems"] as? List<Any>
    lineItems?.let {
      val items = FlutterBraintreePluginHelper.makePayPalItems(it)
      if (items != null) {
        paypalRequest.setLineItems(items)
      }
    }

    driver.tokenizePayPalAccount()
    driver.tokenizePayPalAccount(paypalRequest) { nonce, error in
      guard error == nil else {
        result(mapOf(
          "error" to mapOf(
            "message" to (error as? NSError)?.localizedDescription,
            "code" to (error as? NSError)?.code
          )
        ))
        return
      }
      result(mapOf(
        "data" to FlutterBraintreePluginHelper.buildPaymentNonceDict(nonce)
      ))
    }
  }

  private fun deletePaymentMethodNonce(apiClient: BTAPIClient, nonce:String, result: Result) {
    val parameters = mapOf(
      "operationName" to "DeletePaymentMethodFromSingleUseToken",
      "query" to "mutation DeletePaymentMethodFromSingleUseToken($input: DeletePaymentMethodFromSingleUseTokenInput!) { deletePaymentMethodFromSingleUseToken(input: $input) { clientMutationId } }",
      "variables" to mapOf(
        "input" to mapOf( "singleUseTokenId" to nonce )
      )
    )

    apiClient.post("", parameters, httpType: .graphQLAPI) { body, response, error in
      guard error == nil else {
        result(mapOf(
          "error" to mapOf(
            "code" to (error as? NSError)?.code.toString(),
            "message" to error?.localizedDescription ?: "No vaulted payment methods"
          )
        ))
        return
      }
      fetchPaymentMethodNonces(apiClient, true, result)
    }
  }


  private fun fetchPaymentMethodNonces(apiClient: BTAPIClient, defaultFirst:Boolean, result: Result) {
    apiClient.fetchOrReturnRemoteConfiguration { config, error in
      guard error == null else {
        result(mapOf(
          "error" to mapOf(
            "code" to (error as? NSError)?.code.toString(),
            "message" to error?.localizedDescription ?: "No vaulted payment methods"
          )
        ))
        return
      }
      apiClient.fetchPaymentMethodNonces(defaultFirst, completion: { methods, err in
        guard err == null else {
          result(mapOf(
            "error" to mapOf(
              "code" to (err as? NSError)?.code.toString(),
              "message" to err?.localizedDescription ?: "No vaulted payment methods"
            )
          ))
          return
        }

        guard let methods = methods else {
          result(mapOf(
            "error" to mapOf(
              "code" to "-1",
              "message" to "No vaulted payment methods"
            )
          ))
          return
        }
        result(mapOf(
          "methods" to methods.map({ nonce in
                  return FlutterBraintreePluginHelper.buildPaymentNonceDict(nonce: nonce)
          }),
        ))
      })
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    context = null
    channel.setMethodCallHandler(null)
  }
}
