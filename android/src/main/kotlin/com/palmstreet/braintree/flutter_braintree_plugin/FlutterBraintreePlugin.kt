package com.palmstreet.braintree.flutter_braintree_plugin

import com.palmstreet.braintree.flutter_braintree_plugin.FlutterBraintreePluginHelper
import androidx.appcompat.app.AppCompatActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import com.braintreepayments.api.BraintreeClient
import com.braintreepayments.api.PayPalAccountNonce
import com.braintreepayments.api.PayPalCheckoutRequest
import com.braintreepayments.api.PayPalClient
import com.braintreepayments.api.PayPalLineItem
import com.braintreepayments.api.PayPalListener
import com.braintreepayments.api.PayPalPaymentIntent
import com.braintreepayments.api.PayPalRequest
import com.braintreepayments.api.PayPalVaultRequest
import com.braintreepayments.api.PostalAddress
import com.braintreepayments.api.VenmoAccountNonce
import com.braintreepayments.api.VenmoClient
import com.braintreepayments.api.VenmoListener
import com.braintreepayments.api.VenmoRequest

import java.lang.Exception
import java.util.ArrayList

/** FlutterBraintreePlugin */
class FlutterBraintreePlugin: FlutterPlugin, MethodCallHandler, AppCompatActivity(), PayPalListener, VenmoListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  private var result: Result? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_braintree_plugin")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    this.result = result
    val authorization = FlutterBraintreePluginHelper.getAuthorization(call)
    if (authorization == null) {
      FlutterBraintreePluginHelper.returnAuthorizationMissingError(result)
      return
    }
    val apiClient = BraintreeClient(this, authorization)

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
        result.success(mapOf(
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

  private fun deletePaymentMethodNonce(apiClient: BraintreeClient, nonce:String, result: Result) {
//    val parameters = mapOf(
//      "operationName" to "DeletePaymentMethodFromSingleUseToken",
//      "query" to "mutation DeletePaymentMethodFromSingleUseToken($input: DeletePaymentMethodFromSingleUseTokenInput!) { deletePaymentMethodFromSingleUseToken(input: $input) { clientMutationId } }",
//      "variables" to mapOf(
//        "input" to mapOf( "singleUseTokenId" to nonce )
//      )
//    )
//
//    apiClient.post("", parameters, httpType: .graphQLAPI) { body, response, error in
//      guard error == nil else {
//        result(mapOf(
//          "error" to mapOf(
//            "code" to (error as? NSError)?.code.toString(),
//            "message" to error?.localizedDescription ?: "No vaulted payment methods"
//          )
//        ))
//        return
//      }
//      fetchPaymentMethodNonces(apiClient, true, result)
//    }
  }


  private fun fetchPaymentMethodNonces(apiClient: BraintreeClient, defaultFirst:Boolean, result: Result) {
//    apiClient.fetchOrReturnRemoteConfiguration { config, error in
//      guard error == null else {
//        result(mapOf(
//          "error" to mapOf(
//            "code" to (error as? NSError)?.code.toString(),
//            "message" to error?.localizedDescription ?: "No vaulted payment methods"
//          )
//        ))
//        return
//      }
//      apiClient.fetchPaymentMethodNonces(defaultFirst, completion: { methods, err in
//        guard err == null else {
//          result(mapOf(
//            "error" to mapOf(
//              "code" to (err as? NSError)?.code.toString(),
//              "message" to err?.localizedDescription ?: "No vaulted payment methods"
//            )
//          ))
//          return
//        }
//
//        guard let methods = methods else {
//          result(mapOf(
//            "error" to mapOf(
//              "code" to "-1",
//              "message" to "No vaulted payment methods"
//            )
//          ))
//          return
//        }
//        result(mapOf(
//          "methods" to methods.map({ nonce in
//                  return FlutterBraintreePluginHelper.buildPaymentNonceDict(nonce: nonce)
//          }),
//        ))
//      })
//    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    result = null
    channel.setMethodCallHandler(null)
  }


  // ==================================PayPal
  private fun tokenizePayPalAccount(call: MethodCall, apiClient: BraintreeClient, result: Result) {
    val requestInfo = FlutterBraintreePluginHelper.dict("request", call) ?: run {
      FlutterBraintreePluginHelper.returnFlutterError(result, "-1", "Missing request parameters")
      return
    }

    val vault = requestInfo["vault"] as? Boolean ?: run {
      FlutterBraintreePluginHelper.returnFlutterError(result, "-1", "Missing request parameters")
      return
    }

    val driver = PayPalClient(apiClient)
    driver.setListener(this)

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

    driver.tokenizePayPalAccount(this, paypalRequest)
  }


  // PayPalListener
  override fun onPayPalSuccess(p0: PayPalAccountNonce) {
    result?.success(mapOf(
      "data" to FlutterBraintreePluginHelper.buildPayPalPaymentNonceDict(p0)
    ))
  }

  override fun onPayPalFailure(p0: Exception) {
    result?.success(mapOf(
      "error" to mapOf(
        "message" to p0.localizedMessage,
        "code" to p0.hashCode()
      )
    ))
  }

  // ==================================Venmo
  private fun tokenizeVenmoAccount(call: MethodCall, apiClient: BraintreeClient, result: Result) {
    val venmoInfo = FlutterBraintreePluginHelper.dict("request", call) ?: run {
      FlutterBraintreePluginHelper.returnFlutterError(result, "-1", "Missing request parameters")
      return
    }

    val driver = VenmoClient(this, apiClient)
    driver.setListener(this)

    val paymentMethodUsage = venmoInfo["paymentMethodUsage"] as? Int ?: 0
    val venmoRequest = VenmoRequest(paymentMethodUsage)
    venmoRequest.profileId = venmoInfo["profileID"] as? String
    venmoRequest.shouldVault = venmoInfo["vault"] as? Boolean ?: false
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
    val lineItems = venmoInfo["lineItems"] as? ArrayList<Any>
    lineItems?.let {
      FlutterBraintreePluginHelper.makeVenmoItems(it)?.let { it1 -> venmoRequest.setLineItems(it1) }
    }

    driver.tokenizeVenmoAccount(this, venmoRequest)
  }

  // VenmoListener
  override fun onVenmoSuccess(p0: VenmoAccountNonce) {
    result?.success(mapOf(
      "data" to FlutterBraintreePluginHelper.buildVenmoPaymentNonceDict(p0)
    ))
  }

  override fun onVenmoFailure(p0: Exception) {
    result?.success(mapOf(
      "error" to mapOf(
        "message" to p0.localizedMessage,
        "code" to p0.hashCode()
      )
    ))
  }
}
