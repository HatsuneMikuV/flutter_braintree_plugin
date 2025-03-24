import 'package:flutter/material.dart';
import 'dart:async';

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


  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {

    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running \n'),
        ),
      ),
    );
  }

  void test() {
    // This is a test method

    final token = "";
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
      final result = FlutterBraintree.tokenizePayPalAccount(token, request);
      if (result != null) {
        // Success
      } else {
        // Error
      }
    } catch (e) {
      // Error
    }
  }
}
