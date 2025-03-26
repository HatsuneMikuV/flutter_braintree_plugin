package com.palmstreet.braintree.flutter_braintree_plugin

import android.annotation.SuppressLint
import android.content.res.Resources
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.braintreepayments.api.ApiClient
import com.braintreepayments.api.AuthorizationCallback
import com.braintreepayments.api.BraintreeClient
import com.braintreepayments.api.ClientToken
import com.braintreepayments.api.GraphQLConstants
import com.braintreepayments.api.GraphQLQueryHelper
import com.braintreepayments.api.HttpResponseCallback
import com.braintreepayments.api.MetadataBuilder
import com.braintreepayments.api.PayPalAccountNonce
import com.braintreepayments.api.PayPalCheckoutRequest
import com.braintreepayments.api.PayPalClient
import com.braintreepayments.api.PayPalListener
import com.braintreepayments.api.PayPalPaymentIntent
import com.braintreepayments.api.PayPalRequest
import com.braintreepayments.api.PayPalVaultRequest
import com.braintreepayments.api.PostalAddress
import com.braintreepayments.api.VenmoAccountNonce
import com.braintreepayments.api.VenmoClient
import com.braintreepayments.api.VenmoListener
import com.braintreepayments.api.VenmoRequest
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

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
            "code" to -1,
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

  @SuppressLint("RestrictedApi")
  private fun deletePaymentMethodNonce(braintreeClient: BraintreeClient, nonce:String, result: Result) {
    braintreeClient.getAuthorization(AuthorizationCallback { authorization, error ->
      val usesClientToken = authorization is ClientToken

      if (!usesClientToken) {
        result.success(mapOf(
          "error" to mapOf(
            "code" to -1,
            "message" to "A client token with a customer id must be used to delete a payment method nonce."
          )
        ))
        return@AuthorizationCallback
      }
      if (error != null) {
        result.success(mapOf(
          "error" to mapOf(
            "code" to error.hashCode(),
            "message" to error.localizedMessage
          )
        ))
        return@AuthorizationCallback
      }
      val base = JSONObject()
      val variables = JSONObject()
      val input = JSONObject()

      try {
        base.put(
          "clientSdkMetadata", MetadataBuilder()
            .sessionId(braintreeClient.sessionId)
            .source("client")
            .integration(braintreeClient.integrationType)
            .build()
        )
        base.put(
          GraphQLConstants.Keys.QUERY, GraphQLQueryHelper.getQuery(
            this, R.raw.delete_payment_method_mutation
          )
        )
        input.put("singleUseTokenId", nonce)
        variables.put("input", input)
        base.put("variables", variables)
        base.put(
          GraphQLConstants.Keys.OPERATION_NAME,
          "DeletePaymentMethodFromSingleUseToken"
        )
      } catch (e: Resources.NotFoundException) {
        result.success(mapOf(
          "error" to mapOf(
            "code" to -1,
            "message" to "Unable to read GraphQL query"
          )
        ))
        return@AuthorizationCallback
      } catch (e: IOException) {
        result.success(mapOf(
          "error" to mapOf(
            "code" to -1,
            "message" to "Unable to read GraphQL query"
          )
        ))
        return@AuthorizationCallback
      } catch (e: JSONException) {
        result.success(mapOf(
          "error" to mapOf(
            "code" to -1,
            "message" to "Unable to read GraphQL query"
          )
        ))
        return@AuthorizationCallback
      }

      braintreeClient.sendGraphQLPOST(
        base,
        HttpResponseCallback { responseBody, httpError ->
          if (responseBody != null) {
            braintreeClient.sendAnalyticsEvent("delete-payment-methods.succeeded")
            fetchPaymentMethodNonces(braintreeClient, true, result)
          } else {
            val deletePaymentMethodError = java.lang.Exception(httpError)
            result.success(mapOf(
              "error" to mapOf(
                "code" to deletePaymentMethodError.hashCode(),
                "message" to deletePaymentMethodError.localizedMessage
              )
            ))
            braintreeClient.sendAnalyticsEvent("delete-payment-methods.failed")
          }
        }
      )
    })
  }

  @SuppressLint("RestrictedApi")
  private fun fetchPaymentMethodNonces(braintreeClient: BraintreeClient, defaultFirst:Boolean, result: Result) {
    val uri = Uri.parse(ApiClient.versionedPath(ApiClient.PAYMENT_METHOD_ENDPOINT))
      .buildUpon()
      .appendQueryParameter("default_first", defaultFirst.toString())
      .appendQueryParameter("session_id", braintreeClient.sessionId)
      .build()

    braintreeClient.sendGET(uri.toString(), HttpResponseCallback { responseBody, httpError ->
      if (responseBody != null) {
        try {
          val paymentMethods = JSONObject(responseBody).getJSONArray("paymentMethods")
          result.success(mapOf(
            "methods" to paymentMethods
          ))
          braintreeClient.sendAnalyticsEvent("get-payment-methods.succeeded")
        } catch (e: JSONException) {
          result.success(mapOf(
            "error" to mapOf(
              "code" to e.hashCode(),
              "message" to e.localizedMessage
            )
          ))
          braintreeClient.sendAnalyticsEvent("get-payment-methods.failed")
        }
      } else {
        val fetchPaymentMethodError = java.lang.Exception(httpError)
        result.success(mapOf(
          "error" to mapOf(
            "code" to fetchPaymentMethodError.hashCode(),
            "message" to fetchPaymentMethodError.localizedMessage
          )
        ))
        braintreeClient.sendAnalyticsEvent("get-payment-methods.failed")
      }
    })
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
