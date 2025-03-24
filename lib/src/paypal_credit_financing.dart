import 'paypal_credit_financing_amount.dart';

class BTPayPalCreditFinancing {
  const BTPayPalCreditFinancing({
    required this.cardAmountImmutable,
    this.monthlyPayment,
    required this.payerAcceptance,
    required this.term,
    this.totalCost,
    this.totalInterest,
  });

  factory BTPayPalCreditFinancing.fromJson(dynamic source) {
    return BTPayPalCreditFinancing(
      cardAmountImmutable: source['cardAmountImmutable'] ?? false,
      monthlyPayment: source['monthlyPayment'] != null ? BTPayPalCreditFinancingAmount.fromJson(source['monthlyPayment']) : null,
      payerAcceptance: source['payerAcceptance'] ?? false,
      term: source['term'] ?? 0,
      totalCost: source['totalCost'] != null ?  BTPayPalCreditFinancingAmount.fromJson(source['totalCost']) : null,
      totalInterest: source['totalInterest'] != null ?  BTPayPalCreditFinancingAmount.fromJson(source['totalInterest']) : null,
    );
  }

  /// Indicates whether the card amount is editable after payer's acceptance on PayPal side.
  final bool cardAmountImmutable;

  /// Estimated amount per month that the customer will need to pay including fees and interest.
  final BTPayPalCreditFinancingAmount? monthlyPayment;

  /// Status of whether the customer ultimately was approved for and chose to make the payment using the approved installment credit.
  final bool payerAcceptance;

  /// Length of financing terms in months.
  final int term;

  /// Estimated total payment amount including interest and fees the user will pay during the lifetime of the loan.
  final BTPayPalCreditFinancingAmount? totalCost;

  /// Estimated interest or fees amount the payer will have to pay during the lifetime of the loan.
  final BTPayPalCreditFinancingAmount? totalInterest;

  /// Converts this request object into a JSON-encodable format.
  Map<String, dynamic> toJson() {
    return {
      'cardAmountImmutable': cardAmountImmutable,
      'monthlyPayment': monthlyPayment?.toJson(),
      'payerAcceptance': payerAcceptance,
      'term': term,
      'totalCost': totalCost?.toJson(),
      'totalInterest': totalInterest?.toJson(),
    };
  }
}