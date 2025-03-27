package com.palmstreet.flutter_braintree_plugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.braintreepayments.api.ApiClient;
import com.braintreepayments.api.Authorization;
import com.braintreepayments.api.AuthorizationCallback;
import com.braintreepayments.api.BraintreeClient;
import com.braintreepayments.api.BraintreeException;
import com.braintreepayments.api.ClientToken;
import com.braintreepayments.api.GraphQLConstants;
import com.braintreepayments.api.GraphQLQueryHelper;
import com.braintreepayments.api.MetadataBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;


/** FlutterBraintreePlugin */
public class FlutterBraintreePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, ActivityResultListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Activity activity;
  private Result activeResult;
  private Context context;

  private static final int PAYPAL_ACTIVITY_REQUEST_CODE = 0x420;

  private static final int VENMO_ACTIVITY_REQUEST_CODE = 0x620;


  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_braintree_plugin");
    FlutterBraintreePlugin plugin = new FlutterBraintreePlugin();
    plugin.activity = registrar.activity();
    registrar.addActivityResultListener(plugin);
    channel.setMethodCallHandler(plugin);
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    context = flutterPluginBinding.getApplicationContext();
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_braintree_plugin");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    activeResult = result;

    if (call.method.equals("tokenizePayPalAccount")) {
      Intent intent = new Intent(activity, FlutterBraintreePayPal.class);
      intent.putExtra("type", "tokenizePayPalAccount");
      intent.putExtra("authorization", (String) call.argument("authorization"));
      assert(call.argument("request") instanceof Map);
      Map request = (Map) call.argument("request");
      intent.putExtra("vault", (Boolean) request.get("vault"));
      intent.putExtra("offerCredit", (Boolean) request.get("offerCredit"));
      intent.putExtra("amount", (String) request.get("amount"));
      intent.putExtra("currencyCode", (String) request.get("currencyCode"));
      intent.putExtra("intent", (Integer) request.get("intent"));
      intent.putExtra("userAction", (Integer) request.get("userAction"));
      intent.putExtra("requestBillingAgreement", (Boolean) request.get("requestBillingAgreement"));
      intent.putExtra("offerPayLater", (Boolean) request.get("offerPayLater"));
      intent.putExtra("shippingAddressRequired", (Boolean) request.get("shippingAddressRequired"));
      intent.putExtra("shippingAddressEditable", (Boolean) request.get("shippingAddressEditable"));
      intent.putExtra("displayName", (String) request.get("displayName"));
      intent.putExtra("billingAgreementDescription", (String) request.get("billingAgreementDescription"));
      intent.putExtra("localeCode", (String) request.get("localeCode"));
      intent.putExtra("merchantAccountID", (String) request.get("merchantAccountID"));
      intent.putExtra("riskCorrelationId", (String) request.get("riskCorrelationId"));
      intent.putExtra("landingPageType", (Integer) request.get("landingPageType"));
      Map shippingAddressOverride = (Map) call.argument("shippingAddressOverride");
      if (shippingAddressOverride != null) {
        intent.putExtra("shippingAddressOverride", true);
        intent.putExtra("shippingRecipientName", (String) shippingAddressOverride.get("recipientName"));
        intent.putExtra("shippingStreetAddress", (String) shippingAddressOverride.get("streetAddress"));
        intent.putExtra("shippingExtendedAddress", (String) shippingAddressOverride.get("extendedAddress"));
        intent.putExtra("shippingLocality", (String) shippingAddressOverride.get("locality"));
        intent.putExtra("shippingRegion", (String) shippingAddressOverride.get("region"));
        intent.putExtra("shippingPostalCode", (String) shippingAddressOverride.get("postalCode"));
        intent.putExtra("shippingCountryCodeAlpha2", (String) shippingAddressOverride.get("countryCodeAlpha2"));
      } else {
        intent.putExtra("shippingAddressOverride", false);
      }
      activity.startActivityForResult(intent, PAYPAL_ACTIVITY_REQUEST_CODE);
    } else if (call.method.equals("tokenizeVenmoAccount")) {
      Intent intent = new Intent(activity, FlutterBraintreePayPal.class);
      intent.putExtra("type", "tokenizeVenmoAccount");
      intent.putExtra("authorization", (String) call.argument("authorization"));
      assert(call.argument("request") instanceof Map);
      Map request = (Map) call.argument("request");
      intent.putExtra("paymentMethodUsage", (Integer) request.get("paymentMethodUsage"));
      intent.putExtra("profileID", (String) request.get("profileID"));
      intent.putExtra("vault", (Boolean) request.get("vault"));
      intent.putExtra("displayName", (String) request.get("displayName"));
      intent.putExtra("collectCustomerBillingAddress", (Boolean) request.get("collectCustomerBillingAddress"));
      intent.putExtra("collectCustomerShippingAddress", (Boolean) request.get("collectCustomerShippingAddress"));
      intent.putExtra("subTotalAmount", (String) request.get("subTotalAmount"));
      intent.putExtra("totalAmount", (String) request.get("totalAmount"));
      intent.putExtra("discountAmount", (String) request.get("discountAmount"));
      intent.putExtra("shippingAmount", (String) request.get("shippingAmount"));
      intent.putExtra("taxAmount", (String) request.get("taxAmount"));
      activity.startActivityForResult(intent, VENMO_ACTIVITY_REQUEST_CODE);
    } else if (call.method.equals("fetchPaymentMethodNonces")) {
      activeResult = null;
      fetchPaymentMethodNonces(call, result);
    } else if (call.method.equals("deletePaymentMethodNonce")) {
      activeResult = null;
      deletePaymentMethodNonce(call, result);
    } else {
      activeResult = null;
      result.notImplemented();
    }
  }

  @SuppressLint("RestrictedApi")
  private void deletePaymentMethodNonce(@NonNull MethodCall call, @NonNull Result result) {
    final BraintreeClient braintreeClient = new BraintreeClient(context, (String) call.argument("authorization"));
    braintreeClient.getAuthorization(new AuthorizationCallback() {
      @Override
      public void onAuthorizationResult(@Nullable Authorization authorization, @Nullable Exception error) {
        boolean usesClientToken = authorization instanceof ClientToken;

        if (!usesClientToken) {
          Exception clientTokenRequiredError =
                  new BraintreeException("A client token with a customer id must be used to delete a payment method nonce.");
          onResultError(result, clientTokenRequiredError);
          return;
        }

        final JSONObject base = new JSONObject();
        JSONObject variables = new JSONObject();
        JSONObject input = new JSONObject();

        try {
          base.put("clientSdkMetadata", new MetadataBuilder()
                  .sessionId(braintreeClient.getSessionId())
                  .source("client")
                  .integration(braintreeClient.getIntegrationType())
                  .build());

          base.put(GraphQLConstants.Keys.QUERY, GraphQLQueryHelper.getQuery(
                  context, com.braintreepayments.api.R.raw.delete_payment_method_mutation));
          input.put("singleUseTokenId", call.argument("payment_nonce"));
          variables.put("input", input);
          base.put("variables", variables);
          base.put(GraphQLConstants.Keys.OPERATION_NAME,
                  "DeletePaymentMethodFromSingleUseToken");
        } catch (Resources.NotFoundException | IOException | JSONException e) {
          Exception graphQLError = new BraintreeException("Unable to read GraphQL query");
          onResultError(result, graphQLError);
        }

        braintreeClient.sendGraphQLPOST(base, (responseBody, httpError) -> {
          if (responseBody != null) {
            result.success(true);
            braintreeClient.sendAnalyticsEvent("delete-payment-methods.succeeded");
          } else {
            Exception deletePaymentMethodError = new Exception(httpError);
            onResultError(result, deletePaymentMethodError);
            braintreeClient.sendAnalyticsEvent("delete-payment-methods.failed");
          }
        });
      }
    });
  }

  @SuppressLint("RestrictedApi")
  private void fetchPaymentMethodNonces(@NonNull MethodCall call, @NonNull Result result) {
    final BraintreeClient braintreeClient = new BraintreeClient(context, (String) call.argument("authorization"));
    Boolean defaultFirst = call.argument("defaultFirst");
    final Uri uri = Uri.parse(ApiClient.versionedPath(ApiClient.PAYMENT_METHOD_ENDPOINT))
            .buildUpon()
            .appendQueryParameter("default_first", String.valueOf(defaultFirst))
            .appendQueryParameter("session_id", braintreeClient.getSessionId())
            .build();

    braintreeClient.sendGET(uri.toString(), (responseBody, httpError) -> {
      if (responseBody != null) {
        try {
          braintreeClient.sendAnalyticsEvent("get-payment-methods.succeeded");

          JSONArray paymentMethods = new JSONObject(responseBody).getJSONArray("paymentMethods");
          HashMap<String, Object> nonceMap = new HashMap<String, Object>();
          nonceMap.put("methods", paymentMethods.toString());
          Log.d("fetchPaymentMethodNonces", paymentMethods.toString());
          result.success(nonceMap);
        } catch (JSONException e) {
          onResultError(result, e);
          braintreeClient.sendAnalyticsEvent("get-payment-methods.failed");
        }
      } else {
        Exception error = new Exception(httpError);
        onResultError(result, error);
        braintreeClient.sendAnalyticsEvent("get-payment-methods.failed");
      }
    });
  }

  private void onResultError(Result result, Exception error) {
    HashMap<String, Object> errorMap = new HashMap<String, Object>();
    errorMap.put("message", error.getLocalizedMessage());
    errorMap.put("code", "-1");
    HashMap<String, Object> nonceMap = new HashMap<String, Object>();
    nonceMap.put("error", errorMap);
    result.success(nonceMap);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    context = null;
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
    binding.addActivityResultListener(this);
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
    binding.addActivityResultListener(this);
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (activeResult == null) {
      return false;
    }
    if (data == null) {
      activeResult.error("no_data", "No data was returned", null);
      activeResult = null;
      return true;
    }
    if (resultCode == Activity.RESULT_OK) {
      activeResult.success(data.getSerializableExtra("data"));
    } else {
      activeResult.success(data.getSerializableExtra("error"));
    }
    activeResult = null;
    return true;
  }
}
