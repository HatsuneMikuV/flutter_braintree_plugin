
import 'enums.dart';
import 'paypal_request.dart';
import 'paypal_line_item.dart';
import 'postal_address.dart';

class BTPayPalVaultRequest extends BTPayPalRequest {
  BTPayPalVaultRequest({
    this.offerCredit = false,
    super.displayName,
    super.shippingAddressRequired,
    super.shippingAddressEditable,
    super.shippingAddressOverride,
    super.localeCode,
    super.landingPageType,
    super.merchantAccountID,
    super.lineItems,
    super.billingAgreementDescription,
    super.riskCorrelationId,
  });

  /// Optional: Offers PayPal Credit if the customer qualifies. Defaults to false.
  final bool offerCredit;

  /// Converts this request object into a JSON-encodable format.
  @override
  Map<String, dynamic> toJson() {
    return {
      'offerCredit': offerCredit,
      'vault': true,
      ...super.toJson(),
    };
  }
}