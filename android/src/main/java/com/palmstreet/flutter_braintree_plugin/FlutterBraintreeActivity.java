package com.palmstreet.flutter_braintree_plugin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.api.BraintreeClient;
import com.braintreepayments.api.Configuration;
import com.braintreepayments.api.ConfigurationCallback;
import com.braintreepayments.api.PayPalAccountNonce;
import com.braintreepayments.api.PayPalCheckoutRequest;
import com.braintreepayments.api.PayPalClient;
import com.braintreepayments.api.PayPalListener;
import com.braintreepayments.api.PayPalPaymentIntent;
import com.braintreepayments.api.PayPalRequest;
import com.braintreepayments.api.PayPalVaultRequest;
import com.braintreepayments.api.PostalAddress;
import com.braintreepayments.api.VenmoAccountNonce;
import com.braintreepayments.api.VenmoClient;
import com.braintreepayments.api.VenmoListener;
import com.braintreepayments.api.VenmoPaymentMethodUsage;
import com.braintreepayments.api.VenmoRequest;

import java.util.HashMap;


public class FlutterBraintreeActivity extends FragmentActivity implements PayPalListener, VenmoListener {

    private BraintreeClient braintreeClient;
    private PayPalClient payPalClient;
    private VenmoClient venmoClient;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flutter_braintree);
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
                case "tokenizeVenmoAccount":
                    venmoClient = new VenmoClient(this, braintreeClient);
                    venmoClient.setListener(this);
                    tokenizeVenmoAccount();
                    break;
                default:
                    onError(new Exception("Invalid request type: " + type), "-1");
            }
        } catch (Exception e) {
            onError(e, "-1");
        }
    }

    protected void tokenizeVenmoAccount() {
        Intent intent = getIntent();

        boolean shouldVault = intent.getBooleanExtra("vault", false);
        int venmoPaymentMethodUsage = shouldVault ?
                VenmoPaymentMethodUsage.MULTI_USE : VenmoPaymentMethodUsage.SINGLE_USE;
        VenmoRequest venmoRequest = new VenmoRequest(venmoPaymentMethodUsage);
        venmoRequest.setProfileId(intent.getStringExtra("profileID"));
        venmoRequest.setShouldVault(shouldVault);
        venmoRequest.setDisplayName(intent.getStringExtra("displayName"));
        venmoRequest.setCollectCustomerBillingAddress(intent.getBooleanExtra("collectCustomerBillingAddress", false));
        venmoRequest.setCollectCustomerShippingAddress(intent.getBooleanExtra("collectCustomerShippingAddress", false));
        venmoRequest.setSubTotalAmount(intent.getStringExtra("subTotalAmount"));
        venmoRequest.setTotalAmount(intent.getStringExtra("totalAmount"));
        venmoRequest.setDiscountAmount(intent.getStringExtra("discountAmount"));
        venmoRequest.setShippingAmount(intent.getStringExtra("shippingAmount"));
        venmoRequest.setTaxAmount(intent.getStringExtra("taxAmount"));

        boolean fallbackToWeb = intent.getBooleanExtra("fallbackToWeb", false);
        braintreeClient.getConfiguration(new ConfigurationCallback() {
            @Override
            public void onResult(@Nullable Configuration configuration, @Nullable Exception e) {
                if (e != null) {
                    onError(e, "-1");
                } else {
                    if (fallbackToWeb) {
                        venmoRequest.setFallbackToWeb(true);
                        venmoClient.tokenizeVenmoAccount(FlutterBraintreeActivity.this, venmoRequest);
                    } else {
                        if (venmoClient.isVenmoAppSwitchAvailable(FlutterBraintreeActivity.this)) {
                            venmoClient.tokenizeVenmoAccount(FlutterBraintreeActivity.this, venmoRequest);
                        } else if (configuration.isVenmoEnabled()) {
                            onError(new Exception("Please install the Venmo app first."), "VENMO_APP_NOT_INSTALLED");
                        } else {
                            onError(new Exception("Venmo is not enabled for the current merchant."), "-1");
                        }
                    }
                }
            }
        });
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

    protected void onError(Exception e, String code) {
        Intent result = new Intent();
        HashMap<String, Object> errorMap = new HashMap<String, Object>();
        errorMap.put("message", e.getLocalizedMessage());
        errorMap.put("code", code);
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
        onError(e, "-1");
    }



    @Override
    public void onVenmoSuccess(@NonNull VenmoAccountNonce venmoAccountNonce) {
        HashMap<String, Object> nonceMap = new HashMap<String, Object>();
        nonceMap.put("data", FlutterBraintreeTool.buildResultMap(venmoAccountNonce));
        onSuccessResult(nonceMap);
    }

    @Override
    public void onVenmoFailure(@NonNull Exception e) {
        onError(e, "-1");
    }
}
