import Flutter
import UIKit
import Braintree


public class FlutterBraintreePlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_braintree_plugin", binaryMessenger: registrar.messenger())
    let instance = FlutterBraintreePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    guard let authorization = FlutterBraintreePluginHelper.getAuthorization(call: call) else {
      FlutterBraintreePluginHelper.returnAuthorizationMissingError(result: result)
      return
    }
    
    guard let apiClient = BTAPIClient(authorization: authorization) else {
      FlutterBraintreePluginHelper.returnAuthorizationMissingError(result: result)
      return
    }
    
    switch call.method {
    case "tokenizePayPalAccount":
      tokenizePayPalAccount(call, apiClient: apiClient, result: result)
    case "tokenizeVenmoAccount":
      tokenizeVenmoAccount(call, apiClient: apiClient, result: result)
    case "fetchPaymentMethodNonces":
      let defaultFirst = FlutterBraintreePluginHelper.bool(for: "defaultFirst", in: call) ?? true
      fetchPaymentMethodNonces(apiClient: apiClient, defaultFirst: defaultFirst, result: result)
    case "deletePaymentMethodNonce":
      guard let nonce = FlutterBraintreePluginHelper.string(for: "payment_nonce", in: call) else {
        result([
          "error": [
            "code": "-1",
            "message": "Payment method does not exist",
          ]
        ])
        return
      }
      deletePaymentMethodNonce(apiClient: apiClient, nonce: nonce, result: result)
    default:
      result(FlutterMethodNotImplemented)
    }
  }
  
  
  private func tokenizePayPalAccount(_ call: FlutterMethodCall, apiClient: BTAPIClient, result: @escaping FlutterResult) {
    
    guard let requestInfo = FlutterBraintreePluginHelper.dict(for: "request", in: call) else {
      FlutterBraintreePluginHelper.returnFlutterError(result: result, code: "-1", message: "Missing request parameters")
      return
    }
    
    guard let vault = requestInfo["vault"] as? Bool else {
      FlutterBraintreePluginHelper.returnFlutterError(result: result, code: "-1", message: "Missing request parameters")
      return
    }
    
    var paypalRequest:BTPayPalRequest;
    if vault {
      let paypalVaultRequest = BTPayPalVaultRequest()
      if let offerCredit = requestInfo["offerCredit"] as? Bool {
        paypalVaultRequest.offerCredit = offerCredit
      }
      paypalRequest = paypalVaultRequest
    } else {
      let amount = requestInfo["amount"] as? String ?? "0"
      let paypalCheckoutRequest = BTPayPalCheckoutRequest(amount: amount)
      paypalCheckoutRequest.currencyCode = requestInfo["currencyCode"] as? String
      if let intent = requestInfo["intent"] as? Int {
        switch intent {
        case 1:
          paypalCheckoutRequest.intent = BTPayPalRequestIntent.sale
        case 2:
          paypalCheckoutRequest.intent = BTPayPalRequestIntent.order
        default:
          paypalCheckoutRequest.intent = BTPayPalRequestIntent.authorize
        }
      }
      if let userAction = requestInfo["userAction"] as? Int {
        switch userAction {
        case 1:
          paypalCheckoutRequest.userAction = BTPayPalRequestUserAction.payNow
        default:
          paypalCheckoutRequest.userAction = BTPayPalRequestUserAction.none
        }
      }
      if let requestBillingAgreement = requestInfo["requestBillingAgreement"] as? Bool {
        paypalCheckoutRequest.requestBillingAgreement = requestBillingAgreement
      }
      if let offerPayLater = requestInfo["offerPayLater"] as? Bool {
        paypalCheckoutRequest.offerPayLater = offerPayLater
      }
      paypalRequest = paypalCheckoutRequest
    }
    
    paypalRequest.displayName = requestInfo["displayName"] as? String
    if let shippingAddressRequired = requestInfo["shippingAddressRequired"] as? Bool {
      paypalRequest.isShippingAddressRequired = shippingAddressRequired
    }
    if let billingAgreementDescription = requestInfo["billingAgreementDescription"] as? String {
      paypalRequest.billingAgreementDescription = billingAgreementDescription
    }
    if let shippingAddressOverride = requestInfo["shippingAddressOverride"] as? [String:Any] {
      let address = BTPostalAddress()
      if let recipientName = shippingAddressOverride["recipientName"] as? String {
        address.recipientName = recipientName
      }
      if let postalCode = shippingAddressOverride["postalCode"] as? String {
        address.postalCode = postalCode
      }
      if let streetAddress = shippingAddressOverride["streetAddress"] as? String {
        address.streetAddress = streetAddress
      }
      if let extendedAddress = shippingAddressOverride["extendedAddress"] as? String {
        address.extendedAddress = extendedAddress
      }
      if let locality = shippingAddressOverride["locality"] as? String {
        address.locality = locality
      }
      if let countryCodeAlpha2 = shippingAddressOverride["countryCodeAlpha2"] as? String {
        address.countryCodeAlpha2 = countryCodeAlpha2
      }
      if let region = shippingAddressOverride["region"] as? String {
        address.region = region
      }
      paypalRequest.shippingAddressOverride = address
    }
    if let shippingAddressEditable = requestInfo["shippingAddressEditable"] as? Bool {
      paypalRequest.isShippingAddressEditable = shippingAddressEditable
    }
    if let localeCode = requestInfo["localeCode"] as? Int {
      paypalRequest.localeCode = BTPayPalLocaleCode(rawValue: localeCode) ?? .none
    }
    if let merchantAccountID = requestInfo["merchantAccountID"] as? String {
      paypalRequest.merchantAccountID = merchantAccountID
    }
    if let riskCorrelationId = requestInfo["riskCorrelationId"] as? String {
      paypalRequest.riskCorrelationID = riskCorrelationId
    }
    if let landingPageType = requestInfo["landingPageType"] as? Int {
      switch landingPageType {
      case 1:
        paypalRequest.landingPageType = BTPayPalRequestLandingPageType.login
      case 2:
        paypalRequest.landingPageType = BTPayPalRequestLandingPageType.billing
      default:
        paypalRequest.landingPageType = BTPayPalRequestLandingPageType.none
      }
    }
    
    if let lineItems = requestInfo["lineItems"] as? [Any] {
      paypalRequest.lineItems = FlutterBraintreePluginHelper.makePayPalItems(from: lineItems)
    }
    
    let payPalClient = BTPayPalClient(apiClient: apiClient)
    
    if vault {
      payPalClient.tokenize(paypalRequest as! BTPayPalVaultRequest) { nonce, err in
        guard err == nil else {
          result([
            "error": [
              "message": String(describing: (err as? NSError)?.localizedDescription),
              "code": String(describing: (err as? NSError)?.code),
            ]
          ])
          return
        }
        result([
          "data": FlutterBraintreePluginHelper.buildPaymentNonceDict(nonce: nonce),
        ])
      }
    } else {
      payPalClient.tokenize(paypalRequest as! BTPayPalCheckoutRequest) { nonce, err in
        guard err == nil else {
          result([
            "error": [
              "message": String(describing: (err as? NSError)?.localizedDescription),
              "code": String(describing: (err as? NSError)?.code),
            ]
          ])
          return
        }
        result([
          "data": FlutterBraintreePluginHelper.buildPaymentNonceDict(nonce: nonce),
        ])
      }
    }
  }
  
  
  private func tokenizeVenmoAccount(_ call: FlutterMethodCall, apiClient: BTAPIClient, result: @escaping FlutterResult) {
    
    guard let venmoInfo = FlutterBraintreePluginHelper.dict(for: "request", in: call) else {
      FlutterBraintreePluginHelper.returnFlutterError(result: result, code: "-1", message: "Missing request parameters")
      return
    }
    
    var paymentMethod = BTVenmoPaymentMethodUsage.multiUse
    if let paymentMethodUsage = venmoInfo["paymentMethodUsage"] as? Int {
      paymentMethod = BTVenmoPaymentMethodUsage(rawValue: paymentMethodUsage) ?? .multiUse
    }
    
    let venmoRequest = BTVenmoRequest(paymentMethodUsage: paymentMethod)
    venmoRequest.profileID = venmoInfo["profileID"] as? String
    if let vault = venmoInfo["vault"] as? Bool {
      venmoRequest.vault = vault
    }
    if let isFinalAmount = venmoInfo["isFinalAmount"] as? Bool {
      venmoRequest.isFinalAmount = isFinalAmount
    }
    
    if let fallbackToWeb = venmoInfo["fallbackToWeb"] as? Bool {
      venmoRequest.fallbackToWeb = fallbackToWeb
    }
    
    venmoRequest.displayName = venmoInfo["displayName"] as? String
    if let collectCustomerBillingAddress = venmoInfo["collectCustomerBillingAddress"] as? Bool {
      venmoRequest.collectCustomerBillingAddress = collectCustomerBillingAddress
    }
    if let collectCustomerShippingAddress = venmoInfo["collectCustomerShippingAddress"] as? Bool {
      venmoRequest.collectCustomerShippingAddress = collectCustomerShippingAddress
    }
    venmoRequest.subTotalAmount = venmoInfo["subTotalAmount"] as? String
    venmoRequest.totalAmount = venmoInfo["totalAmount"] as? String
    venmoRequest.discountAmount = venmoInfo["discountAmount"] as? String
    venmoRequest.shippingAmount = venmoInfo["shippingAmount"] as? String
    venmoRequest.taxAmount = venmoInfo["taxAmount"] as? String
    if let lineItems = venmoInfo["lineItems"] as? [Any] {
      venmoRequest.lineItems = FlutterBraintreePluginHelper.makeVenmoItems(from: lineItems)
    }
    
    let venmoClient = BTVenmoClient(apiClient: apiClient)
    
    venmoClient.tokenize(venmoRequest, completion: { nonce, error in
      guard error == nil else {
        result([
          "error": [
            "message": String(describing: (error as? NSError)?.localizedDescription),
            "code": String(describing: (error as? NSError)?.code),
          ]
        ])
        return
      }
      result([
        "data": FlutterBraintreePluginHelper.buildPaymentNonceDict(nonce: nonce),
      ])
    })
  }
  
  
  private func deletePaymentMethodNonce(apiClient: BTAPIClient, nonce:String, result: @escaping FlutterResult) {
    
    let parameters = [
      "operationName": "DeletePaymentMethodFromSingleUseToken",
      "query": "mutation DeletePaymentMethodFromSingleUseToken($input: DeletePaymentMethodFromSingleUseTokenInput!) { deletePaymentMethodFromSingleUseToken(input: $input) { clientMutationId } }",
      "variables": [
        "input": [ "singleUseTokenId" : nonce ]
      ]
    ] as [String : Any]
    
    apiClient.post("", parameters: parameters, httpType: .graphQLAPI) { body, response, error in
      guard error == nil else {
        result([
          "error": [
            "code": String(describing: (error as? NSError)?.code),
            "message": error?.localizedDescription ?? "No vaulted payment methods",
          ]
        ])
        return
      }
      result(true)
    }
  }


  private func fetchPaymentMethodNonces(apiClient: BTAPIClient, defaultFirst:Bool, result: @escaping FlutterResult) {
    apiClient.fetchOrReturnRemoteConfiguration { config, error in
      guard error == nil else {
        result([
          "error": [
            "code": String(describing: (error as? NSError)?.code),
            "message": error?.localizedDescription ?? "No vaulted payment methods",
          ]
        ])
        return
      }
      apiClient.fetchPaymentMethodNonces(defaultFirst, completion: { methods, err in
        guard err == nil else {
          result([
            "error": [
              "code": String(describing: (err as? NSError)?.code),
              "message": err?.localizedDescription ?? "No vaulted payment methods",
            ]
          ])
          return
        }

        guard let methods = methods else {
          result([
            "error": [
              "code": "-1",
              "message": "No vaulted payment methods",
            ]
          ])
          return
        }
        result([
          "methods": methods.map({ nonce in
            return FlutterBraintreePluginHelper.buildPaymentNonceDict(nonce: nonce)
          }),
        ])
      })
    }
  }
}


