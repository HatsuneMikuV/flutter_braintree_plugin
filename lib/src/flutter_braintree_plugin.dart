

import 'package:flutter_braintree_plugin/src/flutter_braintree_plugin_platform_interface.dart';
import 'package:flutter_braintree_plugin/src/payment_method_nonce.dart';
import 'package:flutter_braintree_plugin/src/paypal_account_nonce.dart';
import 'package:flutter_braintree_plugin/src/paypal_request.dart';
import 'package:flutter_braintree_plugin/src/venmo_account_nonce.dart';
import 'package:flutter_braintree_plugin/src/venmo_request.dart';


class FlutterBraintree {

  /// Tokenizes a PayPal account for vault or checkout.
  ///
  /// You can use this as the final step in your order/checkout flow. If you want, you may create a transaction from your
  /// server when this method completes without any additional user interaction.
  ///
  /// On success, you will receive an instance of `BTPayPalAccountNonce`; on failure or user cancelation you will receive an error. If the user cancels out of the flow, the error code will be `BTPayPalDriverErrorTypeCanceled`.
  ///
  /// @param request Either a BTPayPalCheckoutRequest or a BTPayPalVaultRequest
  static Future<BTPayPalAccountNonce?> tokenizePayPalAccount(
      String authorization,
      BTPayPalRequest request,
      ) {
    return FlutterBraintreePluginPlatform.instance.tokenizePayPalAccount(authorization, request);
  }

  /// Initiates Venmo login via app switch, which returns a BTVenmoAccountNonce when successful.
  /// @param request A Venmo request.
  /// @param completion This completion will be invoked when app switch is complete or an error occurs.
  /// On success, you will receive an instance of `BTVenmoAccountNonce`; on failure, an error; on user
  /// cancellation, you will receive `null` for both parameters.
  static Future<BTVenmoAccountNonce?> tokenizeVenmoAccount(
      String authorization,
      BTVenmoRequest request,
      ) {
    return FlutterBraintreePluginPlatform.instance.tokenizeVenmoAccount(authorization, request);
  }

  /// Fetches a customer's vaulted payment method nonces.
  /// Must be using client token with a customer ID specified.
  /// @param defaultFirst Specifies whether to sorts the fetched payment method nonces with the default payment method or the most recently used payment method first
  /// @param completion Callback that returns an array of payment method nonces
  static Future<List<BTPaymentMethodNonce>?> fetchPaymentMethodNonces(
      String authorization,
      {bool defaultFirst = false}) {
    return FlutterBraintreePluginPlatform.instance.fetchPaymentMethodNonces(authorization, defaultFirst: defaultFirst);
  }

  /// Deletes a payment method nonce.
  /// Must be using client token with a customer ID specified.
  /// @param nonce The payment method nonce to delete
  /// @param completion Callback that returns an array of payment method nonces
  static Future<bool> deletePaymentMethodNonce(
      String authorization,
      String nonce,
      ) {
    return FlutterBraintreePluginPlatform.instance.deletePaymentMethodNonce(authorization, nonce);
  }
}
