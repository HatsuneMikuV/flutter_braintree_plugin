import 'package:flutter_braintree_plugin/src/payment_method_nonce.dart';
import 'package:flutter_braintree_plugin/src/paypal_account_nonce.dart';
import 'package:flutter_braintree_plugin/src/paypal_request.dart';
import 'package:flutter_braintree_plugin/src/venmo_account_nonce.dart';
import 'package:flutter_braintree_plugin/src/venmo_request.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_braintree_plugin_method_channel.dart';

abstract class FlutterBraintreePluginPlatform extends PlatformInterface {
  /// Constructs a FlutterBraintreePluginPlatform.
  FlutterBraintreePluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterBraintreePluginPlatform _instance = MethodChannelFlutterBraintreePlugin();

  /// The default instance of [FlutterBraintreePluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterBraintreePlugin].
  static FlutterBraintreePluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterBraintreePluginPlatform] when
  /// they register themselves.
  static set instance(FlutterBraintreePluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<BTPayPalAccountNonce?> tokenizePayPalAccount(
      String authorization,
      BTPayPalRequest request,
      //android only required
      String? appLinkReturnUri
      ) {
    throw UnimplementedError('tokenizePayPalAccount() has not been implemented.');
  }

  Future<BTVenmoAccountNonce?> tokenizeVenmoAccount(
      String authorization,
      BTVenmoRequest request,
      //android only required
      String? appLinkReturnUri
      ) {
    throw UnimplementedError('tokenizeVenmoAccount() has not been implemented.');
  }

  Future<List<BTPaymentMethodNonce>?> fetchPaymentMethodNonces(
      String authorization,
      {bool defaultFirst = false}) {
    throw UnimplementedError('fetchPaymentMethodNonces() has not been implemented.');
  }

  Future<bool> deletePaymentMethodNonce(
      String authorization,
      String nonce,
      ) {
    throw UnimplementedError('deletePaymentMethodNonce() has not been implemented.');
  }
}
