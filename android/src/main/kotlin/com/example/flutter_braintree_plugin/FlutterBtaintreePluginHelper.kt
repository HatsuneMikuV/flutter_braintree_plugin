
package com.example.flutter_braintree_plugin

class FlutterBtaintreePluginHelper {
    companion object {
        fun getAuthorization(call: MethodCall): String? {
            val clientToken = string("clientToken", call)
            val tokenizationKey = string("tokenizationKey", call)
            val authorizationKey = string("authorization", call)

            return clientToken ?: tokenizationKey ?: authorizationKey
        }

        fun buildPaymentNativeNonceDict(nonce: BTPayPalNativeCheckoutAccountNonce?): Map<String, Any?>? {
            nonce ?: return null
            return mapOf(
                "nonce" to nonce.nonce,
                "type" to nonce.type,
                "isDefault" to nonce.isDefault,
                "email" to nonce.email,
                "firstName" to nonce.firstName,
                "lastName" to nonce.lastName,
                "phone" to nonce.phone,
                "clientMetadataID" to nonce.clientMetadataID,
                "payerID" to nonce.payerID,
                "billingAddress" to buildPostalAddressDict(nonce.billingAddress),
                "shippingAddress" to buildPostalAddressDict(nonce.shippingAddress)
            )
        }

        fun buildPaymentNonceDict(nonce: BTPaymentMethodNonce?): Map<String, Any?>? {
            nonce ?: return null
            return mapOf(
                "nonce" to nonce.nonce,
                "type" to nonce.type,
                "isDefault" to nonce.isDefault,
                "email" to (nonce as? BTPayPalAccountNonce)?.email,
                "firstName" to (nonce as? BTPayPalAccountNonce)?.firstName,
                "lastName" to (nonce as? BTPayPalAccountNonce)?.lastName,
                "phone" to (nonce as? BTPayPalAccountNonce)?.phone,
                "clientMetadataID" to (nonce as? BTPayPalAccountNonce)?.clientMetadataID,
                "payerID" to (nonce as? BTPayPalAccountNonce)?.payerID,
                "billingAddress" to buildPostalAddressDict((nonce as? BTPayPalAccountNonce)?.billingAddress),
                "shippingAddress" to buildPostalAddressDict((nonce as? BTPayPalAccountNonce)?.shippingAddress)
            )
        }

        fun buildPostalAddressDict(address: BTPostalAddress?): Map<String, Any?>? {
            address ?: return null
            return mapOf(
                "recipientName" to address.recipientName,
                "streetAddress" to address.streetAddress,
                "extendedAddress" to address.extendedAddress,
                "locality" to address.locality,
                "countryCodeAlpha2" to address.countryCodeAlpha2,
                "postalCode" to address.postalCode,
                "region" to address.region
            )
        }

        fun buildPayPalCreditFinancingDict(financing: BTPayPalCreditFinancing?): Map<String, Any?>? {
            financing ?: return null
            return mapOf(
                "cardAmountImmutable" to financing.cardAmountImmutable,
                "monthlyPayment" to buildPayPalCreditFinancingAmountDict(financing.monthlyPayment),
                "payerAcceptance" to financing.payerAcceptance,
                "term" to financing.term,
                "totalCost" to buildPayPalCreditFinancingAmountDict(financing.totalCost),
                "totalInterest" to buildPayPalCreditFinancingAmountDict(financing.totalInterest)
            )
        }

        fun buildPayPalCreditFinancingAmountDict(financing: BTPayPalCreditFinancingAmount?): Map<String, Any?>? {
            financing ?: return null
            return mapOf(
                "currency" to financing.currency,
                "value" to financing.value
            )
        }

        fun makeVenmoItems(from: List<Any>): List<BTVenmoLineItem>? {
            val venmoItems = from as? List<Map<String, Any>> ?: return null
            val outList = mutableListOf<BTVenmoLineItem>()
            for (venmoItem in venmoItems) {
                val quantity = venmoItem["quantity"] as? Int ?: return null
                val unitAmount = venmoItem["unitAmount"] as? String ?: return null
                val name = venmoItem["name"] as? String ?: return null
                val kind = venmoItem["kind"] as? Int ?: return null
                val venmoKind = BTVenmoLineItemKind.values().find { it.ordinal == kind } ?: return null
                val item = BTVenmoLineItem(quantity, unitAmount, name, venmoKind)
                item.unitTaxAmount = venmoItem["unitTaxAmount"] as? String
                item.itemDescription = venmoItem["itemDescription"] as? String
                item.productCode = venmoItem["productCode"] as? String
                item.url = URL(venmoItem["url"] as? String)
                outList.add(item)
            }
            return outList
        }

        fun makePayPalItems(from: List<Any>): List<BTPayPalLineItem>? {
            val paypalItems = from as? List<Map<String, Any>> ?: return null
            val outList = mutableListOf<BTPayPalLineItem>()
            for (paypalItem in paypalItems) {
                val quantity = paypalItem["quantity"] as? Int ?: return null
                val unitAmount = paypalItem["unitAmount"] as? String ?: return null
                val name = paypalItem["name"] as? String ?: return null
                val kind = paypalItem["kind"] as? Int ?: return null
                val paypalKind = BTPayPalLineItemKind.values().find { it.ordinal == kind } ?: return null
                val item = BTPayPalLineItem(quantity.toString(), unitAmount, name, paypalKind)
                item.unitTaxAmount = paypalItem["unitTaxAmount"] as? String
                item.itemDescription = paypalItem["itemDescription"] as? String
                item.productCode = paypalItem["productCode"] as? String
                item.url = URL(paypalItem["url"] as? String)
                outList.add(item)
            }
            return outList
        }

        fun returnAuthorizationMissingError(result: Result) {
            returnFlutterError(result, "braintree_error", "Authorization not specified (no clientToken or tokenizationKey)")
        }

        fun returnBraintreeError(result: Result, error: Exception) {
            returnFlutterError(result, "braintree_error", error.message)
        }

        fun returnFlutterError(result: Result, code: String, message: String) {
            result.error(code, message, null)
        }

        fun string(key: String, call: MethodCall): String? {
            return (call.arguments as? Map<String, Any>)?.get(key) as? String
        }

        fun bool(key: String, call: MethodCall): Boolean? {
            return (call.arguments as? Map<String, Any>)?.get(key) as? Boolean
        }

        fun dict(key: String, call: MethodCall): Map<String, Any>? {
            return (call.arguments as? Map<String, Any>)?.get(key) as? Map<String, Any>
        }
}