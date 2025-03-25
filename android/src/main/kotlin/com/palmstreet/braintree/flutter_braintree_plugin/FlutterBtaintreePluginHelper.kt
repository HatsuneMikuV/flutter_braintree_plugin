
package com.palmstreet.braintree.flutter_braintree_plugin

import com.braintreepayments.api.PayPalAccountNonce
import com.braintreepayments.api.PayPalCreditFinancing
import com.braintreepayments.api.PayPalCreditFinancingAmount
import com.braintreepayments.api.PayPalLineItem
import com.braintreepayments.api.PostalAddress
import com.braintreepayments.api.VenmoAccountNonce
import com.braintreepayments.api.VenmoLineItem
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel.Result


class FlutterBraintreePluginHelper {
    companion object {
        fun getAuthorization(call: MethodCall): String? {
            val clientToken = string("clientToken", call)
            val tokenizationKey = string("tokenizationKey", call)
            val authorizationKey = string("authorization", call)

            return clientToken ?: tokenizationKey ?: authorizationKey
        }

        fun buildPayPalPaymentNonceDict(nonce: PayPalAccountNonce?): Map<String, Any?>? {
            nonce ?: return null
            return mapOf(
                "nonce" to nonce.string,
                "type" to "PayPal",
                "isDefault" to nonce.isDefault,
                "email" to nonce.email,
                "firstName" to nonce.firstName,
                "lastName" to nonce.lastName,
                "phone" to nonce.phone,
                "clientMetadataID" to nonce.clientMetadataId,
                "payerID" to nonce.payerId,
                "billingAddress" to buildPostalAddressDict(nonce.billingAddress),
                "shippingAddress" to buildPostalAddressDict(nonce.shippingAddress),
                "creditFinancing" to buildPayPalCreditFinancingDict(nonce.creditFinancing),
                "authenticateUrl" to nonce.authenticateUrl
            )
        }

        fun buildVenmoPaymentNonceDict(nonce: VenmoAccountNonce?): Map<String, Any?>? {
            nonce ?: return null
            return mapOf(
                "nonce" to nonce.string,
                "type" to "Venmo",
                "isDefault" to nonce.isDefault,
                "email" to nonce.email,
                "externalId" to nonce.externalId,
                "firstName" to nonce.firstName,
                "lastName" to nonce.lastName,
                "phoneNumber" to nonce.phoneNumber,
                "username" to nonce.username,
                "billingAddress" to buildPostalAddressDict(nonce.billingAddress),
                "shippingAddress" to buildPostalAddressDict(nonce.shippingAddress)
            )
        }

        private fun buildPostalAddressDict(address: PostalAddress?): Map<String, Any?>? {
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

        private fun buildPayPalCreditFinancingDict(financing: PayPalCreditFinancing?): Map<String, Any?>? {
            financing ?: return null
            return mapOf(
                "cardAmountImmutable" to financing.isCardAmountImmutable,
                "monthlyPayment" to buildPayPalCreditFinancingAmountDict(financing.monthlyPayment),
                "payerAcceptance" to financing.hasPayerAcceptance(),
                "term" to financing.term,
                "totalCost" to buildPayPalCreditFinancingAmountDict(financing.totalCost),
                "totalInterest" to buildPayPalCreditFinancingAmountDict(financing.totalInterest)
            )
        }

        private fun buildPayPalCreditFinancingAmountDict(financing: PayPalCreditFinancingAmount?): Map<String, Any?>? {
            financing ?: return null
            return mapOf(
                "currency" to financing.currency,
                "value" to financing.value
            )
        }

        fun makeVenmoItems(from: List<Any>): List<VenmoLineItem>? {
            val venmoItems = from as? List<Map<String, Any>> ?: return null
            val outList = mutableListOf<VenmoLineItem>()
            for (venmoItem in venmoItems) {
                val quantity = venmoItem["quantity"] as? Int ?: return null
                val unitAmount = venmoItem["unitAmount"] as? String ?: return null
                val name = venmoItem["name"] as? String ?: return null
                val kind = venmoItem["kind"] as? Int ?: return null
                val item = VenmoLineItem(kind.toString(), name, quantity, unitAmount)
                val unitTaxAmount = venmoItem["unitTaxAmount"] as? String
                if (unitTaxAmount != null) {
                    item.setUnitTaxAmount(unitTaxAmount)
                }
                val itemDescription = venmoItem["itemDescription"] as? String
                if (itemDescription != null) {
                    item.setDescription(itemDescription)
                }
                val productCode = venmoItem["productCode"] as? String
                if (productCode != null) {
                    item.setProductCode(productCode)
                }
                val url = venmoItem["url"] as? String
                if (url != null) {
                    item.setUrl(url)
                }
                outList.add(item)
            }
            return outList
        }

        fun makePayPalItems(from: List<Any>): List<PayPalLineItem>? {
            val paypalItems = from as? List<Map<String, Any>> ?: return null
            val outList = mutableListOf<PayPalLineItem>()
            for (paypalItem in paypalItems) {
                val quantity = paypalItem["quantity"] as? Int ?: return null
                val unitAmount = paypalItem["unitAmount"] as? String ?: return null
                val name = paypalItem["name"] as? String ?: return null
                val kind = paypalItem["kind"] as? Int ?: return null
                val item = PayPalLineItem(kind.toString(), name, quantity.toString(), unitAmount)
                val unitTaxAmount = paypalItem["unitTaxAmount"] as? String
                if (unitTaxAmount != null) {
                    item.setUnitTaxAmount(unitTaxAmount)
                }
                val itemDescription = paypalItem["itemDescription"] as? String
                if (itemDescription != null) {
                    item.setDescription(itemDescription)
                }
                val productCode = paypalItem["productCode"] as? String
                if (productCode != null) {
                    item.setProductCode(productCode)
                }
                val url = paypalItem["url"] as? String
                if (url != null) {
                    item.setUrl(url)
                }
                outList.add(item)
            }
            return outList
        }

//        fun createVaultRequest(
//            buyerEmailAddress: String?,
//            buyerPhoneCountryCode: String?,
//            buyerPhoneNationalNumber: String?,
//            shopperInsightsSessionId: String?
//        ): Boolean {
//            val request = PayPalVaultRequest(true)
//
//            if (buyerEmailAddress != null && !buyerEmailAddress.isEmpty()) {
//                request.userAuthenticationEmail = buyerEmailAddress
//            }
//
//            if (buyerPhoneCountryCode != null && !buyerPhoneCountryCode.isEmpty() && buyerPhoneNationalNumber != null && !buyerPhoneNationalNumber.isEmpty()) {
//                request.setUserPhoneNumber(
//                    PayPalPhoneNumber(
//                        buyerPhoneCountryCode,
//                        buyerPhoneNationalNumber
//                    )
//                )
//            }
//
//            if (shopperInsightsSessionId != null && !shopperInsightsSessionId.isEmpty()) {
//                request.setShopperSessionId(shopperInsightsSessionId)
//            }
//
//            if (Settings.isPayPalAppSwithEnabled(context)) {
//                request.setEnablePayPalAppSwitch(true)
//            }
//
//            request.displayName = Settings.getPayPalDisplayName(context)
//
//            val landingPageType: String = Settings.getPayPalLandingPageType(context)
//            if (context.getString(R.string.paypal_landing_page_type_billing)
//                    .equals(landingPageType)
//            ) {
//                request.landingPageType = PayPalLandingPageType.LANDING_PAGE_TYPE_BILLING
//            } else if (context.getString(R.string.paypal_landing_page_type_login)
//                    .equals(landingPageType)
//            ) {
//                request.landingPageType = PayPalLandingPageType.LANDING_PAGE_TYPE_LOGIN
//            }
//
//            if (Settings.isPayPalCreditOffered(context)) {
//                request.shouldOfferCredit = true
//            }
//
//            if (Settings.usePayPalAddressOverride(context)) {
//                val postalAddress = PostalAddress()
//                postalAddress.recipientName = "Brian Tree"
//                postalAddress.streetAddress = "123 Fake Street"
//                postalAddress.extendedAddress = "Floor A"
//                postalAddress.locality = "San Francisco"
//                postalAddress.postalCode = "12345"
//                postalAddress.region = "CA"
//                postalAddress.countryCodeAlpha2 = "US"
//                request.shippingAddressOverride = postalAddress
//            }
//
//            if (Settings.isRbaMetadataEnabled(context)) {
//                val billingPricing = PayPalBillingPricing(
//                    PayPalPricingModel.FIXED,
//                    "9.99",
//                    "99.99"
//                )
//                val billingCycle = PayPalBillingCycle(
//                    false,
//                    1,
//                    PayPalBillingInterval.MONTH,
//                    1,
//                    1,
//                    "2024-08-01",
//                    billingPricing
//                )
//                val billingCycles: MutableList<PayPalBillingCycle> = ArrayList<PayPalBillingCycle>()
//                billingCycles.add(billingCycle)
//                val payPalRecurringBillingDetails = PayPalRecurringBillingDetails(
//                    billingCycles,
//                    "32.56",
//                    "USD",
//                    "Vogue Magazine Subscription",
//                    "9.99",
//                    "Home delivery to Chicago, IL",
//                    "19.99",
//                    1,
//                    "1.99",
//                    "0.59"
//                )
//                request.setRecurringBillingDetails(payPalRecurringBillingDetails)
//                request.setRecurringBillingPlanType(PayPalRecurringBillingPlanType.SUBSCRIPTION)
//            }
//            return request
//        }

        fun returnAuthorizationMissingError(result: Result) {
            returnFlutterError(
                result,
                "braintree_error",
                "Authorization not specified (no clientToken or tokenizationKey)"
            )
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
}