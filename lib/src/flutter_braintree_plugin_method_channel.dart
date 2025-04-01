import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_braintree_plugin/src/payment_method_nonce.dart';
import 'package:flutter_braintree_plugin/src/paypal_account_nonce.dart';
import 'package:flutter_braintree_plugin/src/paypal_request.dart';
import 'package:flutter_braintree_plugin/src/venmo_account_nonce.dart';
import 'package:flutter_braintree_plugin/src/venmo_request.dart';

import 'flutter_braintree_plugin_platform_interface.dart';

/// An implementation of [FlutterBraintreePluginPlatform] that uses method channels.
class MethodChannelFlutterBraintreePlugin extends FlutterBraintreePluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_braintree_plugin');

  /// Tokenizes a PayPal account for vault or checkout.
  ///
  /// You can use this as the final step in your order/checkout flow. If you want, you may create a transaction from your
  /// server when this method completes without any additional user interaction.
  ///
  /// On success, you will receive an instance of `BTPayPalAccountNonce`; on failure or user cancelation you will receive an error. If the user cancels out of the flow, the error code will be `BTPayPalDriverErrorTypeCanceled`.
  ///
  /// @param request Either a BTPayPalCheckoutRequest or a BTPayPalVaultRequest
  @override
  Future<BTPayPalAccountNonce?> tokenizePayPalAccount(
      String authorization,
      BTPayPalRequest request,
      //android only required
      String? appLinkReturnUri
      ) async {
    final result = await methodChannel.invokeMethod('tokenizePayPalAccount', {
      'authorization': authorization,
      'request': request.toJson(),
      'appLinkReturnUri': appLinkReturnUri,
    });
    if (result is Map && result.isNotEmpty) {
      if (result['error'] != null) {
        throw PlatformException(
          code: "${result['error']['code']}",
          message: result['error']['message'],
        );
      }
      return BTPayPalAccountNonce.fromJson(result['data']);
    }
    return null;
  }

  @override
  Future<BTVenmoAccountNonce?> tokenizeVenmoAccount(
      String authorization,
      BTVenmoRequest request,
      //android only required
      String? appLinkReturnUri
      ) async {
    final result = await methodChannel.invokeMethod('tokenizeVenmoAccount', {
      'authorization': authorization,
      'request': request.toJson(),
      'appLinkReturnUri': appLinkReturnUri,
    });
    if (result is Map && result.isNotEmpty) {
      if (result['error'] != null) {
        throw PlatformException(
          code: "${result['error']['code']}",
          message: result['error']['message'],
        );
      }
      return BTVenmoAccountNonce.fromJson(result['data']);
    }
    return null;
  }

  /// Fetches a customer's vaulted payment method nonces.
  /// Must be using client token with a customer ID specified.
  /// @param defaultFirst Specifies whether to sorts the fetched payment method nonces with the default payment method or the most recently used payment method first
  /// @param completion Callback that returns an array of payment method nonces
  @override
  Future<List<BTPaymentMethodNonce>?> fetchPaymentMethodNonces(
      String authorization,
      {bool defaultFirst = false}) async {
    final result = await methodChannel.invokeMethod('fetchPaymentMethodNonces', {
      'authorization': authorization,
      'defaultFirst': defaultFirst,
    });
    if (result is Map && result.isNotEmpty) {
      if (result['error'] != null) {
        throw PlatformException(
          code: "${result['error']['code']}",
          message: result['error']['message'],
        );
      }
      final list = result['methods'];
      List<BTPaymentMethodNonce> nonces = [];
      if (list is List && list.isNotEmpty) {
        for (var json in list) {
          final item = BTPaymentMethodNonce.buildFromJson(json);
          if (item != null) {
            nonces.add(item);
          }
        }
      }
      return nonces;
    }
    return null;
  }

  /// Deletes a payment method nonce.
  /// Must be using client token with a customer ID specified.
  /// @param nonce The payment method nonce to delete
  /// @param completion Callback that returns an array of payment method nonces
  @override
  Future<bool> deletePaymentMethodNonce(
      String authorization,
      String nonce,
      ) async {
    final result = await methodChannel.invokeMethod('deletePaymentMethodNonce', {
      'payment_nonce': nonce,
      'authorization': authorization,
    });
    if (result is bool) {
      return result;
    }
    if (result is Map && result.isNotEmpty) {
      if (result['error'] != null) {
        throw PlatformException(
          code: "${result['error']['code']}",
          message: result['error']['message'],
        );
      }
    }
    return false;
  }
}
