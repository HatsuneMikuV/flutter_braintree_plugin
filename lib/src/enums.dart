
///  @note Must be set to BTPayPalRequestIntentSale for immediate payment, BTPayPalRequestIntentAuthorize to authorize a payment for capture later, or BTPayPalRequestIntentOrder to create an order. Defaults to BTPayPalRequestIntentAuthorize. Only applies to PayPal Checkout.
///  @see https://developer.paypal.com/docs/integration/direct/payments/capture-payment/ Capture payments later
///  @see https://developer.paypal.com/docs/integration/direct/payments/create-process-order/ Create and process orders
enum BTPayPalRequestIntent {
  authorize,
  sale,
  order,
}

/// The call-to-action in the PayPal Checkout flow.
///  @note By default the final button will show the localized word for "Continue" and implies that the final amount billed is not yet known.
///  Setting the BTPayPalRequest's userAction to `BTPayPalRequestUserActionCommit` changes the button text to "Pay Now", conveying to
///  the user that billing will take place immediately.

enum BTPayPalRequestUserAction {
  default_,
  commit,
}

/// The type of landing page to display when a user lands on the PayPal site to complete the payment.
enum BTPayPalLineItemKind {
  debit,
  credit,
}

/// The type of landing page to display when a user lands on the PayPal site to complete the payment.
enum BTVenmoLineItemKind {
  debit,
  credit,
}

enum BTPayPalPaymentType {
  /// Checkout
  checkout,

  /// Vault
  vault,
}


/// The type of landing page to display when a user lands on the PayPal site to complete the payment.
enum BTPayPalRequestLandingPageType {
  /// Default
  default_,

  /// Login
  login,

  /// Billing
  billing,
}


/// A BTVenmoRequest specifies options that contribute to the Venmo flow
enum BTVenmoPaymentMethodUsage {

  /// Unspecified
  unspecified,

  /// Multi-use
  multiUse,

  /// Single use
  singleUse,
}
