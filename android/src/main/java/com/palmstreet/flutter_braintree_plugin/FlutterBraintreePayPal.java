package com.palmstreet.flutter_braintree_plugin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.api.BraintreeClient;
import com.braintreepayments.api.PayPalAccountNonce;
import com.braintreepayments.api.PayPalCheckoutRequest;
import com.braintreepayments.api.PayPalClient;
import com.braintreepayments.api.PayPalListener;
import com.braintreepayments.api.PayPalPaymentIntent;
import com.braintreepayments.api.PayPalRequest;
import com.braintreepayments.api.PayPalVaultRequest;
import com.braintreepayments.api.PostalAddress;
import java.util.HashMap;


public class FlutterBraintreePayPal extends FragmentActivity implements PayPalListener {

    private BraintreeClient braintreeClient;
    private PayPalClient payPalClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flutter_braintree_paypal);
        try {
            Intent intent = getIntent();
            braintreeClient = new BraintreeClient(
                    this,
                    intent.getStringExtra("authorization"),
                    this.getPackageName() + ".braintree",
                    Uri.parse( intent.getStringExtra("appLinkReturnUri"))
            );
            String type = intent.getStringExtra("type");
            switch (type) {
                case "tokenizePayPalAccount":
                    payPalClient = new PayPalClient(this, braintreeClient);
                    payPalClient.setListener(this);
                    tokenizePayPalAccount();
                    break;
                default:
                    onError(new Exception("Invalid request type: " + type));
            }
        } catch (Exception e) {
            onError(e);
        }
    }

    protected void tokenizePayPalAccount() {
        PayPalRequest paypalRequest;
        Intent intent = getIntent();
        boolean vault = intent.getBooleanExtra("vault", false);
        if (vault) {
            PayPalVaultRequest paypalVaultRequest = new PayPalVaultRequest(true);
            paypalVaultRequest.setShouldOfferCredit(intent.getBooleanExtra("offerCredit", false));
            paypalRequest = paypalVaultRequest;
        } else {
            String amount = intent.getStringExtra("amount");
            if (amount == null) {
                amount = "0.00";
            }
            PayPalCheckoutRequest paypalCheckoutRequest = new PayPalCheckoutRequest(amount, true);
            paypalCheckoutRequest.setCurrencyCode(intent.getStringExtra("currencyCode"));
            int intentType = intent.getIntExtra("intent", 0);
            if (intentType == 2) {
                paypalCheckoutRequest.setIntent(PayPalPaymentIntent.ORDER);
            } else if (intentType == 1) {
                paypalCheckoutRequest.setIntent(PayPalPaymentIntent.SALE);
            } else {
                paypalCheckoutRequest.setIntent(PayPalPaymentIntent.AUTHORIZE);
            }
            int userAction = intent.getIntExtra("userAction", 0);
            if (userAction == 1) {
                paypalCheckoutRequest.setUserAction(PayPalCheckoutRequest.USER_ACTION_COMMIT);
            } else {
                paypalCheckoutRequest.setUserAction(PayPalCheckoutRequest.USER_ACTION_DEFAULT);
            }
            paypalCheckoutRequest.setShouldRequestBillingAgreement(intent.getBooleanExtra("requestBillingAgreement", false));
            paypalCheckoutRequest.setShouldOfferPayLater(intent.getBooleanExtra("offerPayLater", false));
            paypalRequest = paypalCheckoutRequest;
        }

        paypalRequest.setDisplayName(intent.getStringExtra("displayName"));
        paypalRequest.setBillingAgreementDescription(intent.getStringExtra("billingAgreementDescription"));
        paypalRequest.setLocaleCode(intent.getStringExtra("localeCode"));
        paypalRequest.setMerchantAccountId(intent.getStringExtra("merchantAccountID"));
        paypalRequest.setRiskCorrelationId(intent.getStringExtra("riskCorrelationId"));
        paypalRequest.setShippingAddressRequired(intent.getBooleanExtra("shippingAddressRequired", false));
        paypalRequest.setShippingAddressEditable(intent.getBooleanExtra("shippingAddressEditable", false));

        int landingPageType = intent.getIntExtra("landingPageType", 0);
        switch (landingPageType) {
            case 2:
                paypalRequest.setLandingPageType(PayPalRequest.LANDING_PAGE_TYPE_BILLING);
                break;
            default:
                paypalRequest.setLandingPageType(PayPalRequest.LANDING_PAGE_TYPE_LOGIN);
        }

        boolean shippingAddressOverride = intent.getBooleanExtra("shippingAddressOverride", false);
        if (shippingAddressOverride) {
            PostalAddress address = new PostalAddress();
            address.setRecipientName(intent.getStringExtra("shippingRecipientName"));
            address.setStreetAddress(intent.getStringExtra("shippingStreetAddress"));
            address.setExtendedAddress(intent.getStringExtra("shippingExtendedAddress"));
            address.setLocality(intent.getStringExtra("shippingLocality"));
            address.setRegion(intent.getStringExtra("shippingRegion"));
            address.setPostalCode(intent.getStringExtra("shippingPostalCode"));
            address.setCountryCodeAlpha2(intent.getStringExtra("shippingCountryCodeAlpha2"));
            paypalRequest.setShippingAddressOverride(address);
        }
        payPalClient.tokenizePayPalAccount(this, paypalRequest);
    }

    protected void onError(Exception e) {
        Intent result = new Intent();
        HashMap<String, Object> errorMap = new HashMap<String, Object>();
        errorMap.put("message", e.getLocalizedMessage());
        errorMap.put("code", "-1");
        HashMap<String, Object> nonceMap = new HashMap<String, Object>();
        nonceMap.put("error", errorMap);
        result.putExtra("error", nonceMap);
        setResult(2, result);
        finish();
    }

    protected void onSuccessResult(HashMap<String, Object> nonceMap) {
        Intent result = new Intent();
        result.putExtra("data", nonceMap);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPayPalSuccess(@NonNull PayPalAccountNonce paypalAccountNonce) {
        HashMap<String, Object> nonce = FlutterBraintreeTool.buildResultMap(paypalAccountNonce);
        HashMap<String, Object> nonceMap = new HashMap<String, Object>();
        nonceMap.put("data", nonce);
        onSuccessResult(nonceMap);
    }

    @Override
    public void onPayPalFailure(@NonNull Exception e) {
        onError(e);
    }
}
