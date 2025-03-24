
import 'dart:convert';
import 'paypal_account_nonce.dart';
import 'venmo_account_nonce.dart';


enum PaymentMethodType {
  Unknown,
  PayPal,
  Venmo,
  AMEX,
  DinersClub,
  Discover,
  MasterCard,
  Visa,
  JCB,
  Laser,
  Maestro,
  UnionPay,
  Hiper,
  Hipercard,
  Solo,
  Switch,
  UKMaestro,
  ApplePayCard,
  AndroidPayCard,
}


class BTPaymentMethodNonce {
  const BTPaymentMethodNonce({
    required this.nonce,
    required this.type,
    required this.isDefault,
  });

  factory BTPaymentMethodNonce.fromJson(dynamic source) {
    return BTPaymentMethodNonce(
      nonce: source['nonce'],
      type: source['type'],
      isDefault: source['isDefault'],
    );
  }

  /// The one-time use payment method nonce
  final String nonce;

  /// The type of the tokenized data, e.g. PayPal, Venmo, MasterCard, Visa, Amex
  final String type;

  /// True if this nonce is the customer's default payment method, otherwise false.
  final bool isDefault;

  /// Converts this request object into a JSON-encodable format.
  Map<String, dynamic> toJson() {
    return {
      'nonce': nonce,
      'type': type,
      'isDefault': isDefault,
    };
  }


  static BTPaymentMethodNonce? buildFromJson(dynamic json) {
    if (json['type'] == PaymentMethodType.PayPal.name) {
      return BTPayPalAccountNonce.fromJson(json);
    } else if (json['type'] == PaymentMethodType.Venmo.name) {
      return BTVenmoAccountNonce.fromJson(json);
    }
    return null;
  }
}
