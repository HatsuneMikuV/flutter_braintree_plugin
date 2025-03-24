import 'package:flutter/material.dart';
import 'package:flutter_braintree_plugin/flutter_braintree_platform.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  String token = '';

  @override
  void initState() {
    super.initState();
    getToken();
  }


  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: CustomScrollView(
          slivers: <Widget>[
             token.isNotEmpty ? SliverList(
              delegate: SliverChildListDelegate(
                <Widget>[
                  Container(
                    child: ElevatedButton(
                      onPressed: () {
                        oneTimePayPalAccount();
                      },
                    child: Text('Tokenize PayPal Account\n${oneTimePaymentResult?.email}'),
                    ),
                  ),
                  Container(
                    child: ElevatedButton(
                      onPressed: () {
                        vaultPayPalAccount();
                      },
                      child: Text('Tokenize PayPal Account\n${vaultPaymentResult?.email}'),
                    ),
                  ),
                  Container(
                    child: ElevatedButton(
                      onPressed: () {
                        tokenizeVenmoAccount(false);
                      },
                      child: Text('Tokenize Venmo Account\n${oneTimeVenmoPaymentResult?.username}'),
                    ),
                  ),
                  Container(
                    child: ElevatedButton(
                      onPressed: () {
                        tokenizeVenmoAccount(true);
                      },
                      child: Text('Tokenize Venmo Account\n${vaultVenmoPaymentResult?.username}'),
                    ),
                  ),
                ],
              ),
            ): SliverList(
              delegate: SliverChildListDelegate(
                <Widget>[
                  Container(
                    child: ElevatedButton(
                      onPressed: () {
                        getToken();
                      },
                      child: Text('Get Token ${token.length}'),
                    ),
                  ),
                ],
              ),
            ),
            if (paymentMethodNonces != null) SliverList(
              delegate: SliverChildBuilderDelegate(
                (BuildContext context, int index) {
                  final item = paymentMethodNonces![index];
                  return ListTile(
                    onLongPress: () {
                      showDialog(
                        context: context,
                        builder: (BuildContext context) {
                          return AlertDialog(
                            title: Text('Delete Payment Method Nonce'),
                            content: Text('Are you sure you want to delete this payment method nonce?'),
                            actions: <Widget>[
                              TextButton(
                                onPressed: () {
                                },
                                child: Text('Cancel'),
                              ),
                              TextButton(
                                onPressed: () {
                                  deletePaymentMethodNonce(item.nonce);
                                },
                                child: Text('Delete'),
                              ),
                            ],
                          );
                        },
                      );
                    },
                    title: Text(item.type),
                    subtitle: Text(item.nonce),
                  );
                },
                childCount: paymentMethodNonces!.length,
              ),
            ),
          ],
        ),
      ),
    );
  }

  void getToken() {
    //paypal create token with server
    token = 'sandbox_1234567890_xxxxxxxxxxxx';
    setState(() {

    });
  }

  BTPayPalAccountNonce? oneTimePaymentResult;
  void oneTimePayPalAccount() async {
    if (token.isEmpty) {
      print("Token is empty");
      return;
    }
    final request = BTPayPalCheckoutRequest(
      amount: "10.00",
      displayName: "Example Company",
      billingAgreementDescription: "Your agreement description",
      shippingAddressRequired: true,
      shippingAddressEditable: false,
      shippingAddressOverride: BTPostalAddress(
        recipientName: "Brian Gecko",
        streetAddress: "1234 Elm St.",
        extendedAddress: "4th Floor",
        locality: "Chicago",
        region: "IL",
        postalCode: "60654",
        countryCodeAlpha2: "US",
      ),
    );

    try {
      oneTimePaymentResult = await FlutterBraintree.tokenizePayPalAccount(token, request);
      setState(() {});
    } catch (e) {
      print("Error: $e");
    }
  }

  BTPayPalAccountNonce? vaultPaymentResult;
  void vaultPayPalAccount() async {
    if (token.isEmpty) {
      print("Token is empty");
      return;
    }
    final request = BTPayPalVaultRequest(
      displayName: "Example Company",
      billingAgreementDescription: "Your agreement description",
      shippingAddressRequired: true,
      shippingAddressEditable: false,
      shippingAddressOverride: BTPostalAddress(
        recipientName: "Brian Gecko",
        streetAddress: "1234 Elm St.",
        extendedAddress: "4th Floor",
        locality: "Chicago",
        region: "IL",
        postalCode: "60654",
        countryCodeAlpha2: "US",
      ),
    );

    try {
      vaultPaymentResult = await FlutterBraintree.tokenizePayPalAccount(token, request);
      setState(() {});
    } catch (e) {
      print("Error: $e");
    }
  }

  BTVenmoAccountNonce? oneTimeVenmoPaymentResult;
  BTVenmoAccountNonce? vaultVenmoPaymentResult;
  void tokenizeVenmoAccount(bool vault) async {
    if (token.isEmpty) {
      print("Token is empty");
      return;
    }
    final request = BTVenmoRequest(
      displayName: "Example Company",
      vault: vault,
    );
    try {
      if (vault) {
        vaultVenmoPaymentResult = await FlutterBraintree.tokenizeVenmoAccount(token, request);
      } else {
        oneTimeVenmoPaymentResult = await FlutterBraintree.tokenizeVenmoAccount(token, request);
      }
      setState(() {});
    } catch (e) {
      print("Error: $e");
    }
  }

  List<BTPaymentMethodNonce>? paymentMethodNonces;
  void fetchPaymentMethodNonces() async {
    if (token.isEmpty) {
      print("Token is empty");
      return;
    }
    try {
      paymentMethodNonces = await FlutterBraintree.fetchPaymentMethodNonces(token);
      print("Payment Method Nonces: $paymentMethodNonces");
      setState(() {

      });
    } catch (e) {
      print("Error: $e");
    }
  }

  void deletePaymentMethodNonce(String nonce) async {
    if (token.isEmpty) {
      print("Token is empty");
      return;
    }
    try {
      paymentMethodNonces = await FlutterBraintree.deletePaymentMethodNonce(token, nonce);
      print("Payment Method Nonces: $paymentMethodNonces");
      setState(() {

      });
    } catch (e) {
      print("Error: $e");
    }
  }
}
