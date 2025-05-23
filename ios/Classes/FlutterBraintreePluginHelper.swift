
import Foundation
import Flutter
import Braintree

class FlutterBraintreePluginHelper {

  class func getAuthorization(call: FlutterMethodCall) -> String? {
    let clientToken = string(for: "clientToken", in: call)
    let tokenizationKey = string(for: "tokenizationKey", in: call)
    let authorizationKey = string(for: "authorization", in: call)

    guard let authorization = clientToken
            ?? tokenizationKey
            ?? authorizationKey else {
      return nil
    }

    return authorization
  }

  class func buildPaymentNonceDict(nonce: BTPaymentMethodNonce?) -> [String: Any?]? {
    guard let nonce = nonce else { return nil }
    var dict = [String: Any?]()
    dict["nonce"] = nonce.nonce
    dict["type"] = nonce.type
    dict["isDefault"] = nonce.isDefault

    if let paypalNonce = nonce as? BTPayPalAccountNonce {
      dict["email"] = paypalNonce.email
      dict["firstName"] = paypalNonce.firstName
      dict["lastName"] = paypalNonce.lastName
      dict["phone"] = paypalNonce.phone
      dict["clientMetadataID"] = paypalNonce.clientMetadataID
      dict["payerID"] = paypalNonce.payerID
      dict["billingAddress"] = buildPostalAddressDict(address: paypalNonce.billingAddress)
      dict["shippingAddress"] = buildPostalAddressDict(address: paypalNonce.shippingAddress)
      dict["creditFinancing"] = buildPayPalCreditFinancingDict(financing: paypalNonce.creditFinancing)
    }
    if let venmoNonce = nonce as? BTVenmoAccountNonce {
      dict["email"] = venmoNonce.email
      dict["externalId"] = venmoNonce.externalID
      dict["firstName"] = venmoNonce.firstName
      dict["lastName"] = venmoNonce.lastName
      dict["phoneNumber"] = venmoNonce.phoneNumber
      dict["username"] = venmoNonce.username
      dict["billingAddress"] = buildPostalAddressDict(address: venmoNonce.billingAddress)
      dict["shippingAddress"] = buildPostalAddressDict(address: venmoNonce.shippingAddress)
    }
    return dict
  }

  class func buildPostalAddressDict(address: BTPostalAddress?) -> [String: Any?]? {
    guard let address = address else { return nil }
    var dict = [String: Any?]()
    dict["recipientName"] = address.recipientName
    dict["streetAddress"] = address.streetAddress
    dict["extendedAddress"] = address.extendedAddress
    dict["locality"] = address.locality
    dict["countryCodeAlpha2"] = address.countryCodeAlpha2
    dict["postalCode"] = address.postalCode
    dict["region"] = address.region
    return dict
  }

  class func buildPayPalCreditFinancingDict(financing: BTPayPalCreditFinancing?) -> [String: Any?]? {
    guard let financing = financing else { return nil }
    var dict = [String: Any?]()
    dict["cardAmountImmutable"] = financing.cardAmountImmutable
    dict["monthlyPayment"] = buildPayPalCreditFinancingAmountDict(financing: financing.monthlyPayment)
    dict["payerAcceptance"] = financing.payerAcceptance
    dict["term"] = financing.term
    dict["totalCost"] = buildPayPalCreditFinancingAmountDict(financing: financing.totalCost)
    dict["totalInterest"] = buildPayPalCreditFinancingAmountDict(financing: financing.totalInterest)
    return dict
  }

  class func buildPayPalCreditFinancingAmountDict(financing: BTPayPalCreditFinancingAmount?) -> [String: Any?]? {
    guard let financing = financing else { return nil }
    var dict = [String: Any?]()
    dict["currency"] = financing.currency
    dict["value"] = financing.value
    return dict
  }

  class func makeVenmoItems(from: [Any]) -> [BTVenmoLineItem]? {
    guard let venmoItems = from as? [[String: Any]] else {
      return nil;
    }

    var outList: [BTVenmoLineItem] = []
    for venmoItem in venmoItems {
      guard let quantity = venmoItem["quantity"] as? Int else {
        return nil;
      }
      guard let unitAmount = venmoItem["unitAmount"] as? String else {
        return nil;
      }
      guard let name = venmoItem["name"] as? String else {
        return nil;
      }
      guard let kind = venmoItem["kind"] as? Int else {
        return nil;
      }
      guard let venmoKind = BTVenmoLineItemKind.init(rawValue: kind) else {
        return nil;
      }
      let item = BTVenmoLineItem(quantity: quantity, unitAmount: unitAmount, name: name, kind: venmoKind)

      item.unitTaxAmount = venmoItem["unitTaxAmount"] as? String
      item.itemDescription = venmoItem["itemDescription"] as? String
      item.productCode = venmoItem["productCode"] as? String
      if let urlStr = venmoItem["url"] as? String, let url = URL.init(string: urlStr) {
        item.url = url
      }

      outList.append(item);
    }

    return outList;
  }

  class func makePayPalItems(from: [Any]) -> [BTPayPalLineItem]? {
    guard let paypalItems = from as? [[String: Any]] else {
      return nil;
    }

    var outList: [BTPayPalLineItem] = []
    for paypalItem in paypalItems {
      guard let quantity = paypalItem["quantity"] as? Int else {
        return nil;
      }
      guard let unitAmount = paypalItem["unitAmount"] as? String else {
        return nil;
      }
      guard let name = paypalItem["name"] as? String else {
        return nil;
      }
      guard let kind = paypalItem["kind"] as? Int else {
        return nil;
      }
      guard let paypalKind = BTPayPalLineItemKind.init(rawValue: kind) else {
        return nil;
      }
      let item = BTPayPalLineItem(quantity: "\(quantity)", unitAmount: unitAmount, name: name, kind: paypalKind)
      outList.append(item);
    }

    return outList;
  }


  class func returnAuthorizationMissingError (result: FlutterResult) {
    returnFlutterError(result: result, code: "braintree_error", message: "Authorization not specified (no clientToken or tokenizationKey)")
  }

  class func returnBraintreeError(result: FlutterResult, error: Error) {
    returnFlutterError(result: result, code: "braintree_error", message: error.localizedDescription)
  }

  class func returnFlutterError(result: FlutterResult, code: String, message: String) {
    result(FlutterError(code: code, message: message, details: nil));
  }

  class func string(for key: String, in call: FlutterMethodCall) -> String? {
    return (call.arguments as? [String: Any])?[key] as? String
  }

  class func bool(for key: String, in call: FlutterMethodCall) -> Bool? {
    return (call.arguments as? [String: Any])?[key] as? Bool
  }

  class func dict(for key: String, in call: FlutterMethodCall) -> [String: Any]? {
    return (call.arguments as? [String: Any])?[key] as? [String: Any]
  }
}
