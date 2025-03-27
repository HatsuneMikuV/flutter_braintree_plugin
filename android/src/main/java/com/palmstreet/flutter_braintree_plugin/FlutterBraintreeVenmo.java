package com.palmstreet.flutter_braintree_plugin;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.braintreepayments.api.BraintreeClient;

import com.braintreepayments.api.VenmoAccountNonce;
import com.braintreepayments.api.VenmoClient;
import com.braintreepayments.api.VenmoListener;
import com.braintreepayments.api.VenmoRequest;


import java.util.HashMap;

public class FlutterBraintreeVenmo extends AppCompatActivity implements VenmoListener {


    private BraintreeClient braintreeClient;
    private VenmoClient venmoClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_flutter_braintree_venmo);
        try {
            Intent intent = getIntent();
            braintreeClient = new BraintreeClient(this, intent.getStringExtra("authorization"));
            String type = intent.getStringExtra("type");
            switch (type) {
                case "tokenizeVenmoAccount":
                    venmoClient = new VenmoClient(this, braintreeClient);
                    venmoClient.setListener(this);
                    tokenizeVenmoAccount();
                    break;
                default:
                    onError(new Exception("Invalid request type: " + type));
            }
        } catch (Exception e) {
            onError(e);
        }
    }

    protected void tokenizeVenmoAccount() {
        Intent intent = getIntent();

        int paymentMethodUsage = intent.getIntExtra("paymentMethodUsage", 0);
        VenmoRequest venmoRequest = new VenmoRequest(paymentMethodUsage);
        venmoRequest.setProfileId(intent.getStringExtra("profileID"));
        venmoRequest.setShouldVault(intent.getBooleanExtra("vault", false));
        venmoRequest.setDisplayName(intent.getStringExtra("displayName"));
        venmoRequest.setCollectCustomerBillingAddress(intent.getBooleanExtra("collectCustomerBillingAddress", false));
        venmoRequest.setCollectCustomerShippingAddress(intent.getBooleanExtra("collectCustomerShippingAddress", false));
        venmoRequest.setSubTotalAmount(intent.getStringExtra("subTotalAmount"));
        venmoRequest.setTotalAmount(intent.getStringExtra("totalAmount"));
        venmoRequest.setDiscountAmount(intent.getStringExtra("discountAmount"));
        venmoRequest.setShippingAmount(intent.getStringExtra("shippingAmount"));
        venmoRequest.setTaxAmount(intent.getStringExtra("taxAmount"));
        venmoClient.tokenizeVenmoAccount(this, venmoRequest);
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
    public void onVenmoSuccess(@NonNull VenmoAccountNonce venmoAccountNonce) {
        HashMap<String, Object> nonceMap = new HashMap<String, Object>();
        nonceMap.put("data", FlutterBraintreeTool.buildResultMap(venmoAccountNonce));
        onSuccessResult(nonceMap);
    }

    @Override
    public void onVenmoFailure(@NonNull Exception e) {
        onError(e);
    }
}
