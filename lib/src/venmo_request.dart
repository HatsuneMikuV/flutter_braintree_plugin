
import 'enums.dart';
import 'venmo_line_item.dart';


class BTVenmoRequest {
  BTVenmoRequest({
    this.profileID,
    this.vault = false,
    this.paymentMethodUsage = BTVenmoPaymentMethodUsage.singleUse,
    this.displayName,
    this.collectCustomerBillingAddress = false,
    this.collectCustomerShippingAddress = false,
    this.isFinalAmount = false,
    this.subTotalAmount,
    this.totalAmount,
    this.discountAmount,
    this.shippingAmount,
    this.taxAmount,
    this.lineItems,
    this.fallbackToWeb = false,
  });

  /// The Venmo profile ID to be used during payment authorization. Customers will see the business name and logo associated with this Venmo profile, and it may show up in the Venmo app as a "Connected Merchant". Venmo profile IDs can be found in the Braintree Control Panel. Leaving this `nil` will use the default Venmo profile.
  final String? profileID;

  /// Whether to automatically vault the Venmo account on the client. For client-side vaulting, you must initialize BTAPIClient with a client token that was created with a customer ID. Also, `paymentMethodUsage` on the BTVenmoRequest must be set to `.multiUse`.
  ///
  /// If this property is set to false, you can still vault the Venmo account on your server, provided that `paymentMethodUsage` is not set to `.singleUse`.
  ///
  /// Defaults to false.
  final bool vault;

  /// If set to `.multiUse`, the Venmo payment will be authorized for future payments and can be vaulted.
  /// If set to `.singleUse`, the Venmo payment will be authorized for a one-time payment and cannot be vaulted.
  /// If set to `.unspecified`, the legacy Venmo UI flow will launch. It is recommended to use `.multiUse` or `.singleUse` for the best customer experience.
  ///
  /// Defaults to `.unspecified`.
  final BTVenmoPaymentMethodUsage paymentMethodUsage;

  /// Optional: The business name that will be displayed in the Venmo app payment approval screen. Only used by merchants onboarded as PayFast channel partners.
  final String? displayName;

  /// Whether the customer's billing address should be collected and displayed on the Venmo paysheet.
  ///
  /// Defaults to false.
  final bool collectCustomerBillingAddress;

  /// Whether the customer's shipping address should be collected and displayed on the Venmo paysheet.
  ///
  /// Defaults to false
  final bool collectCustomerShippingAddress;

  /// Indicates whether the purchase amount is the final amount.
  /// Removes "subject to change" notice in Venmo app paysheet UI.
  /// Defaults to `false`
  final bool isFinalAmount;

  /// Optional: The subtotal amount of the transaction to be displayed on the paysheet. Excludes taxes, discounts, and shipping amounts.
  final String? subTotalAmount;

  /// Optional: The grand total amount on the transaction that should be displayed on the paysheet.
  final String? totalAmount;

  /// Optional: The total discount amount applied on the transaction to be displayed on the paysheet.
  final String? discountAmount;

  /// Optional: The shipping amount for the transaction to be displayed on the paysheet.
  final String? shippingAmount;

  /// Optional: The total tax amount for the transaction to be displayed on the paysheet.
  final String? taxAmount;

  /// Optional: The line items for this transaction. It can include up to 249 line items.
  final List<BTVenmoLineItem>? lineItems;
  
  /// only for android
  /// Whether or not to fallback to the web flow if Venmo app is not installed.
  final bool fallbackToWeb;

  /// Creates a new instance from a JSON object.
  Map<String, dynamic> toJson() {
    return {
      'profileID': profileID,
      'vault': vault,
      'isFinalAmount': isFinalAmount,
      'paymentMethodUsage': paymentMethodUsage.index,
      'displayName': displayName,
      'collectCustomerBillingAddress': collectCustomerBillingAddress,
      'collectCustomerShippingAddress': collectCustomerShippingAddress,
      'subTotalAmount': subTotalAmount,
      'totalAmount': totalAmount,
      'discountAmount': discountAmount,
      'shippingAmount': shippingAmount,
      'taxAmount': taxAmount,
      'lineItems': lineItems?.map((e) => e.toJson()).toList(),
      'fallbackToWeb': fallbackToWeb,
    };
  }
}
