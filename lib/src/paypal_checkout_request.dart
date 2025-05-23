
import 'enums.dart';
import 'paypal_request.dart';
import 'paypal_line_item.dart';
import 'postal_address.dart';


class BTPayPalCheckoutRequest extends BTPayPalRequest {
  BTPayPalCheckoutRequest({
    required this.amount,
    this.currencyCode,
    this.intent = BTPayPalRequestIntent.authorize,
    this.userAction = BTPayPalRequestUserAction.default_,
    this.offerPayLater = false,
    this.requestBillingAgreement = false,
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

  /// Used for a one-time payment.
  ///
  /// Amount must be greater than or equal to zero, may optionally contain exactly 2 decimal places separated by '.' and is limited to 7 digits before the decimal point.
  final String amount;

  /// Optional: A three-character ISO-4217 ISO currency code to use for the transaction. Defaults to merchant currency code if not set.
  ///
  ///  @note See https://developer.paypal.com/docs/api/reference/currency-codes/ for a list of supported currency codes.
  final String? currencyCode;

  /// Optional: Payment intent. Defaults to BTPayPalRequestIntentAuthorize. Only applies to PayPal Checkout.
  final BTPayPalRequestIntent intent;

  /// Optional: Changes the call-to-action in the PayPal Checkout flow. Defaults to `BTPayPalRequestUserActionDefault`.
  final BTPayPalRequestUserAction userAction;

  /// Optional: Offers PayPal Pay Later if the customer qualifies. Defaults to false. Only available with PayPal Checkout.
  final bool offerPayLater;

  /// Optional: If set to true, this enables the Checkout with Vault flow, where the customer will be prompted to consent to a billing agreement during checkout.
  final bool requestBillingAgreement;

  /// Converts this request object into a JSON-encodable format.
  @override
  Map<String, dynamic> toJson() {
    return {
      'amount': amount,
      'currencyCode': currencyCode,
      'intent': intent.index,
      'userAction': userAction.index,
      'offerPayLater': offerPayLater,
      'requestBillingAgreement': requestBillingAgreement,
      'vault': false,
      ...super.toJson(),
    };
  }
}

