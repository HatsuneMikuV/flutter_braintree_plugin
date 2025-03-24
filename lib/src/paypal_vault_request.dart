
import 'enums.dart';
import 'paypal_request.dart';
import 'paypal_line_item.dart';
import 'postal_address.dart';

class BTPayPalVaultRequest extends BTPayPalRequest {
  BTPayPalVaultRequest({
    this.offerCredit = false,
    String? displayName,
    bool shippingAddressRequired = false,
    bool shippingAddressEditable = false,
    BTPostalAddress? shippingAddressOverride,
    String? localeCode,
    BTPayPalRequestLandingPageType landingPageType = BTPayPalRequestLandingPageType.default_,
    String? merchantAccountID,
    List<BTPayPalLineItem>? lineItems,
    String? billingAgreementDescription,
    String? riskCorrelationId,
  }): super(
    displayName: displayName,
    shippingAddressRequired: shippingAddressRequired,
    shippingAddressEditable: shippingAddressEditable,
    shippingAddressOverride: shippingAddressOverride,
    localeCode: localeCode,
    landingPageType: landingPageType,
    merchantAccountID: merchantAccountID,
    lineItems: lineItems,
    billingAgreementDescription: billingAgreementDescription,
    riskCorrelationId: riskCorrelationId,
  );

  /// Optional: Offers PayPal Credit if the customer qualifies. Defaults to false.
  final bool offerCredit;

  /// Converts this request object into a JSON-encodable format.
  Map<String, dynamic> toJson() {
    return {
      'offerCredit': offerCredit,
      'vault': true,
      ...super.toJson(),
    };
  }
}