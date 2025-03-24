import 'payment_method_nonce.dart';
import 'postal_address.dart';
import 'paypal_credit_financing.dart';



class BTPayPalAccountNonce extends BTPaymentMethodNonce {
  const BTPayPalAccountNonce({
    required this.email,
    required this.firstName,
    required this.lastName,
    required this.phone,
    required this.billingAddress,
    required this.shippingAddress,
    required this.clientMetadataID,
    required this.payerID,
    required this.creditFinancing,
    required String nonce,
    required String type,
    required bool isDefault,
  }) : super(
    nonce: nonce,
    type: type,
    isDefault: isDefault,
  );

  /// Creates a new instance from a JSON object.
  factory BTPayPalAccountNonce.fromJson(dynamic source) {
    return BTPayPalAccountNonce(
      email: source['email'],
      firstName: source['firstName'],
      lastName: source['lastName'],
      phone: source['phone'],
      billingAddress: source['billingAddress'] != null ? BTPostalAddress.fromJson(source['billingAddress']) : null,
      shippingAddress: source['shippingAddress'] != null ?  BTPostalAddress.fromJson(source['shippingAddress']) : null,
      clientMetadataID: source['clientMetadataID'],
      payerID: source['payerID'],
      creditFinancing: source['creditFinancing'] != null ? BTPayPalCreditFinancing.fromJson(source['creditFinancing']) : null,
      nonce: source['nonce'],
      type: source['type'],
      isDefault: source['isDefault'],
    );
  }

  /// Payer's email address.
  final String? email;

  /// Payer's first name.
  final String? firstName;

  /// Payer's last name.
  final String? lastName;

  /// Payer's phone number.
  final String? phone;

  /// The billing address.
  final BTPostalAddress? billingAddress;

  /// The shipping address.
  final BTPostalAddress? shippingAddress;

  /// Client metadata id associated with this transaction.
  final String? clientMetadataID;

  /// Optional. Payer id associated with this transaction.
  ///
  /// Will be provided for Vault and Checkout.
  final String? payerID;

  /// Optional. Credit financing details if the customer pays with PayPal Credit.
  ///
  /// Will be provided for Vault and Checkout.
  final BTPayPalCreditFinancing? creditFinancing;

  /// Converts this request object into a JSON-encodable format.
  Map<String, dynamic> toJson() {
    return {
      'email': email,
      'firstName': firstName,
      'lastName': lastName,
      'phone': phone,
      'billingAddress': billingAddress?.toJson(),
      'shippingAddress': shippingAddress?.toJson(),
      'clientMetadataID': clientMetadataID,
      'payerID': payerID,
      'creditFinancing': creditFinancing?.toJson(),
      ...super.toJson(),
    };
  }
}