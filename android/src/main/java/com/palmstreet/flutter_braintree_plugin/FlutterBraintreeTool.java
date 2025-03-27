package com.palmstreet.flutter_braintree_plugin;
import com.braintreepayments.api.PayPalCreditFinancing;
import com.braintreepayments.api.PayPalCreditFinancingAmount;
import com.braintreepayments.api.PaymentMethodNonce;
import com.braintreepayments.api.PayPalAccountNonce;
import com.braintreepayments.api.PostalAddress;
import com.braintreepayments.api.VenmoAccountNonce;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class FlutterBraintreeTool {

    static public HashMap<String, Object> buildResultMap(PaymentMethodNonce nonce) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("nonce", nonce.getString());
        map.put("isDefault", nonce.isDefault());
        if (nonce instanceof PayPalAccountNonce) {
            map.put("typeLabel", "PayPal");
            PayPalAccountNonce paypalNonce = (PayPalAccountNonce) nonce;
            map.put("email", paypalNonce.getEmail());
            map.put("firstName", paypalNonce.getFirstName());
            map.put("lastName", paypalNonce.getLastName());
            map.put("phone", paypalNonce.getPhone());
            map.put("clientMetadataID", paypalNonce.getClientMetadataId());
            map.put("payerID", paypalNonce.getPayerId());
            map.put("billingAddress", buildPostalAddressDict(paypalNonce.getBillingAddress()));
            map.put("shippingAddress", buildPostalAddressDict(paypalNonce.getShippingAddress()));
            map.put("creditFinancing", buildPayPalCreditFinancingDict(paypalNonce.getCreditFinancing()));
        }
        if (nonce instanceof VenmoAccountNonce) {
            map.put("typeLabel", "Venmo");
            VenmoAccountNonce venmoNonce = (VenmoAccountNonce) nonce;
            map.put("email", venmoNonce.getEmail());
            map.put("externalId", venmoNonce.getExternalId());
            map.put("firstName", venmoNonce.getFirstName());
            map.put("lastName", venmoNonce.getLastName());
            map.put("phoneNumber", venmoNonce.getPhoneNumber());
            map.put("username", venmoNonce.getUsername());
            map.put("billingAddress", buildPostalAddressDict(venmoNonce.getBillingAddress()));
            map.put("shippingAddress", buildPostalAddressDict(venmoNonce.getShippingAddress()));
        }
        return map;
    }

    static private HashMap<String, Object> buildPostalAddressDict(PostalAddress address) {
        if (address == null) {
            return null;
        }
        HashMap<String, Object> dict = new HashMap<>();
        dict.put("recipientName", address.getRecipientName());
        dict.put("streetAddress", address.getStreetAddress());
        dict.put("extendedAddress", address.getExtendedAddress());
        dict.put("locality", address.getLocality());
        dict.put("countryCodeAlpha2", address.getCountryCodeAlpha2());
        dict.put("postalCode", address.getPostalCode());
        dict.put("region", address.getRegion());
        return dict;
    }

    static private HashMap<String, Object> buildPayPalCreditFinancingDict(PayPalCreditFinancing financing) {
        if (financing == null) {
            return null;
        }
        HashMap<String, Object> dict = new HashMap<>();
        dict.put("cardAmountImmutable", financing.isCardAmountImmutable());
        dict.put("monthlyPayment", buildPayPalCreditFinancingAmountDict(financing.getMonthlyPayment()));
        dict.put("payerAcceptance", financing.hasPayerAcceptance());
        dict.put("term", financing.getTerm());
        dict.put("totalCost", buildPayPalCreditFinancingAmountDict(financing.getTotalCost()));
        dict.put("totalInterest", buildPayPalCreditFinancingAmountDict(financing.getTotalInterest()));
        return dict;
    }

    static private HashMap<String, Object> buildPayPalCreditFinancingAmountDict(PayPalCreditFinancingAmount financing) {
        if (financing == null) {
            return null;
        }
        HashMap<String, Object> dict = new HashMap<>();
        dict.put("currency", financing.getCurrency());
        dict.put("value", financing.getValue());
        return dict;
    }
}
