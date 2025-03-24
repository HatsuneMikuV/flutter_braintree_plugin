import 'payment_method_nonce.dart';
import 'postal_address.dart';

class BTVenmoAccountNonce extends BTPaymentMethodNonce {
  const BTVenmoAccountNonce({
    required this.email,
    required this.externalId,
    required this.firstName,
    required this.lastName,
    required this.phoneNumber,
    required this.username,
    required this.billingAddress,
    required this.shippingAddress,
    required String nonce,
    required String type,
    required bool isDefault,
  }) : super(
    nonce: nonce,
    type: type,
    isDefault: isDefault,
  );

  /// Creates a new instance from a JSON object.
  factory BTVenmoAccountNonce.fromJson(dynamic source) {
    return BTVenmoAccountNonce(
      email: source['email'],
      externalId: source['externalId'],
      firstName: source['firstName'],
      lastName: source['lastName'],
      phoneNumber: source['phoneNumber'],
      username: source['username'],
      billingAddress: source['billingAddress'] != null ? BTPostalAddress.fromJson(source['billingAddress']) : null,
      shippingAddress: source['shippingAddress'] != null ?  BTPostalAddress.fromJson(source['shippingAddress']) : null,
      nonce: source['nonce'],
      type: source['type'],
      isDefault: source['isDefault'],
    );
  }

  /// The email associated with the Venmo account
  final String? email;

  /// The external ID associated with the Venmo account
  final String? externalId;

  /// The first name associated with the Venmo account
  final String? firstName;

  /// The last name associated with the Venmo account
  final String? lastName;

  /// The phone number associated with the Venmo account
  final String? phoneNumber;

  /// The username associated with the Venmo account
  final String? username;

  /// The primary billing address associated with the Venmo account
  final BTPostalAddress? billingAddress;

  /// The primary shipping address associated with the Venmo account
  final BTPostalAddress? shippingAddress;

  /// Converts this request object into a JSON-encodable format.
  Map<String, dynamic> toJson() {
    return {
      'email': email,
      'externalId': externalId,
      'firstName': firstName,
      'lastName': lastName,
      'phoneNumber': phoneNumber,
      'username': username,
      'billingAddress': billingAddress?.toJson(),
      'shippingAddress': shippingAddress?.toJson(),
      ...super.toJson(),
    };
  }
}