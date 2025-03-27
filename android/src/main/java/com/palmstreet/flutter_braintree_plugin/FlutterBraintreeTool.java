package com.palmstreet.flutter_braintree_plugin;
import com.braintreepayments.api.PayPalCreditFinancing;
import com.braintreepayments.api.PayPalCreditFinancingAmount;
import com.braintreepayments.api.PaymentMethodNonce;
import com.braintreepayments.api.PayPalAccountNonce;
import com.braintreepayments.api.PostalAddress;
import com.braintreepayments.api.VenmoAccountNonce;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FlutterBraintreeTool {


    static public HashMap<String, Object> buildFetchMethodsMap(String responseBody) throws JSONException {
        JSONArray paymentMethods = new JSONObject(responseBody).getJSONArray("paymentMethods");
        List<HashMap<String, Object>> paymentMethodList = new ArrayList<>();
        for (int i = 0; i < paymentMethods.length(); i++) {
            JSONObject json = paymentMethods.getJSONObject(i);
            HashMap<String, Object> paymentMethodNonce = null;
            String type = json.getString("type");
            if (type.equals("PayPalAccount")) {
                paymentMethodNonce = buildPayPalAccountMap(json);
            } else if (type.equals("VenmoAccount")) {
                paymentMethodNonce = buildVenmoAccountMap(json);
            }
            if (paymentMethodNonce != null) {
                paymentMethodList.add(paymentMethodNonce);
            }
        }

        HashMap<String, Object> noncesMap = new HashMap<String, Object>();
        noncesMap.put("methods", paymentMethodList);
        return noncesMap;
    }

    static public HashMap<String, Object> buildErrorMap(Exception error) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", error.getClass().getSimpleName());
        map.put("message", error.getMessage());
        HashMap<String, Object> errorMap = new HashMap<>();
        errorMap.put("error", map);
        return errorMap;
    }

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

    static private HashMap<String, Object> buildPayPalAccountMap(JSONObject inputJson) throws JSONException {
        if (inputJson == null) {
            return null;
        }

        HashMap<String, Object> map = new HashMap<>();

        boolean getShippingAddressFromTopLevel = false;
        JSONObject json;
        if (inputJson.has("paypalAccounts")) {
            json = inputJson.getJSONArray("paypalAccounts").getJSONObject(0);
        } else if (inputJson.has("paymentMethodData")) {
            getShippingAddressFromTopLevel = true;
            JSONObject tokenObj = new JSONObject(inputJson.getJSONObject("paymentMethodData").getJSONObject("tokenizationData").getString("token"));
            json = tokenObj.getJSONArray("paypalAccounts").getJSONObject(0);
        } else {
            json = inputJson;
        }

        String nonce = json.getString("nonce");
        boolean isDefault = json.optBoolean("default", false);
        JSONObject details = json.getJSONObject("details");
        String email = details.optString("email");
        String clientMetadataId = details.optString("correlationId");
        HashMap<String, Object> payPalCreditFinancing = null;
        String firstName = null;
        String lastName = null;
        String phone = null;
        String payerId = null;

        HashMap<String, Object> shippingAddress = null;
        HashMap<String, Object> billingAddress = null;
        JSONObject payerInfo;
        try {
            if (details.has("creditFinancingOffered")) {
                payerInfo = details.getJSONObject("creditFinancingOffered");
                payPalCreditFinancing = buildCreditFinancingMap(payerInfo);
            }

            payerInfo = details.getJSONObject("payerInfo");
            JSONObject billingAddressJson = payerInfo.optJSONObject("billingAddress");
            if (payerInfo.has("accountAddress")) {
                billingAddressJson = payerInfo.optJSONObject("accountAddress");
            }

            shippingAddress = buildAddressMap(payerInfo.optJSONObject("shippingAddress"));
            billingAddress = buildAddressMap(billingAddressJson);
            firstName = payerInfo.optString("firstName", "");
            lastName = payerInfo.optString("lastName", "");
            phone = payerInfo.optString("phone", "");
            payerId = payerInfo.optString("payerId", "");
            if (email == null) {
                email = payerInfo.optString("email");
            }
        } catch (JSONException var18) {

        }

        if (getShippingAddressFromTopLevel) {
            payerInfo = json.optJSONObject("shippingAddress");
            if (payerInfo != null) {
                shippingAddress = buildAddressMap(payerInfo);
            }
        }


        map.put("nonce", nonce);
        map.put("type", "PayPal");
        map.put("isDefault", isDefault);
        map.put("email", email);
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("phone", phone);
        map.put("clientMetadataID", clientMetadataId);
        map.put("payerID", payerId);
        map.put("billingAddress", billingAddress);
        map.put("shippingAddress", shippingAddress);
        map.put("creditFinancing", payPalCreditFinancing);
        return map;
    }

    static private HashMap<String, Object> buildVenmoAccountMap(JSONObject inputJson) throws JSONException {
        if (inputJson == null) {
            return null;
        }
        HashMap<String, Object> map = new HashMap<>();

        JSONObject json;
        if (inputJson.has("venmoAccounts")) {
            json = inputJson.getJSONArray("venmoAccounts").getJSONObject(0);
        } else {
            json = inputJson;
        }

        String nonce;
        boolean isDefault;
        String username;
        if (json.has("paymentMethodId")) {
            isDefault = false;
            nonce = json.getString("paymentMethodId");
            username = json.getString("userName");
        } else {
            nonce = json.getString("nonce");
            isDefault = json.optBoolean("default", false);
            JSONObject details = json.getJSONObject("details");
            username = details.getString("username");
        }

        map.put("nonce", nonce);
        map.put("type", "Venmo");
        map.put("isDefault", isDefault);
        map.put("username", username);

        JSONObject payerInfo = json.optJSONObject("payerInfo");
        if (payerInfo != null) {
            map.put("email", payerInfo.optString("email"));
            map.put("externalID", payerInfo.optString("externalId"));
            map.put("firstName", payerInfo.optString("firstName"));
            map.put("lastName", payerInfo.optString("lastName"));
            map.put("phoneNumber", payerInfo.optString("phoneNumber"));
            map.put("billingAddress", buildAddressMap(payerInfo.optJSONObject("billingAddress")));
            map.put("shippingAddress", buildAddressMap(payerInfo.optJSONObject("shippingAddress")));
        }
        return map;
    }

    static private HashMap<String, Object> buildAddressMap(JSONObject address) {
        if (address == null) {
            return null;
        }
        HashMap<String, Object> result = new HashMap<>();
        result.put("recipientName", address.optString("recipientName"));
        result.put("streetAddress", address.optString("streetAddress"));
        result.put("extendedAddress", address.optString("extendedAddress"));
        result.put("locality", address.optString("locality"));
        result.put("region", address.optString("region"));
        result.put("postalCode", address.optString("postalCode"));
        result.put("countryCodeAlpha2", address.optString("countryCodeAlpha2"));
        return result;
    }

    static private HashMap<String, Object> buildCreditFinancingMap(JSONObject financing) {
        if (financing == null) {
            return null;
        }
        HashMap<String, Object> result = new HashMap<>();
        result.put("cardAmountImmutable", financing.optBoolean("cardAmountImmutable", false));
        result.put("monthlyPayment", buildCreditFinancingAmountMap(financing.optJSONObject("monthlyPayment")));
        result.put("payerAcceptance", financing.optBoolean("payerAcceptance", false));
        result.put("term", financing.optInt("term", 0));
        result.put("totalCost", buildCreditFinancingAmountMap(financing.optJSONObject("totalCost")));
        result.put("totalInterest", buildCreditFinancingAmountMap(financing.optJSONObject("totalInterest")));
        return result;
    }

    static private HashMap<String, Object> buildCreditFinancingAmountMap(JSONObject amount) {
        if (amount == null) {
            return null;
        }
        HashMap<String, Object> result = new HashMap<>();
        result.put("currency", amount.optString("currency"));
        result.put("value", amount.optString("value"));
        return result;
    }
}
