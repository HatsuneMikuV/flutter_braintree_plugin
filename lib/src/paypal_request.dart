import 'enums.dart';
import 'postal_address.dart';
import 'paypal_line_item.dart';
import 'dart:core';

class BTPayPalRequest {
  BTPayPalRequest({
    this.shippingAddressRequired = false,
    this.shippingAddressEditable = false,
    this.localeCode,
    this.shippingAddressOverride,
    this.landingPageType = BTPayPalRequestLandingPageType.default_,
    this.displayName,
    this.merchantAccountID,
    this.lineItems,
    this.billingAgreementDescription,
    this.riskCorrelationId,
  });

  /// Defaults to false. When set to true, the shipping address selector will be displayed.
  final bool shippingAddressRequired;

  /// Defaults to false. Set to true to enable user editing of the shipping address.
  ///
  /// Only applies when `shippingAddressOverride` is set.
  final bool shippingAddressEditable;

  /// Optional: A locale code to use for the transaction.
  final BTPayPalLocaleCode? localeCode;

  /// Optional: A valid shipping address to be displayed in the transaction flow. An error will occur if this address is not valid.
  final BTPostalAddress? shippingAddressOverride;

  /// Optional: Landing page type. Defaults to `BTPayPalRequestLandingPageTypeDefault`.
  ///
  /// Setting the BTPayPalRequest's landingPageType changes the PayPal page to display when a user lands on the PayPal site to complete the payment. BTPayPalRequestLandingPageTypeLogin specifies a PayPal account login page is used. BTPayPalRequestLandingPageTypeBilling specifies a non-PayPal account landing
  /// page is used.
  final BTPayPalRequestLandingPageType landingPageType;

  /// Optional: The merchant name displayed inside of the PayPal flow; defaults to the company name on your Braintree account
  final String? displayName;

  /// Optional: A non-default merchant account to use for tokenization.
  final String? merchantAccountID;

  /// Optional: The line items for this transaction. It can include up to 249 line items.
  final List<BTPayPalLineItem>? lineItems;

  /// Optional: Display a custom description to the user for a billing agreement. For Checkout with Vault flows, you must also set requestBillingAgreement to true on your BTPayPalCheckoutRequest.
  final String? billingAgreementDescription;

  /// Optional: A risk correlation ID created with Set Transaction Context on your server.
  final String? riskCorrelationId;

  /// Converts this request object into a JSON-encodable format.
  Map<String, dynamic> toJson() => {
    'shippingAddressRequired': shippingAddressRequired,
    'shippingAddressEditable': shippingAddressEditable,
    if (localeCode != null) 'localeCode': localeCode?.index,
    if (shippingAddressOverride != null) 'shippingAddressOverride': shippingAddressOverride?.toJson(),
    'landingPageType': landingPageType.index,
    'displayName': displayName,
    'merchantAccountID': merchantAccountID,
    if (lineItems != null) 'lineItems': lineItems?.map((e) => e.toJson()).toList(),
    'billingAgreementDescription': billingAgreementDescription,
    'riskCorrelationId': riskCorrelationId,
  };
}