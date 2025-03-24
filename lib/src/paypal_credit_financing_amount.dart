

class BTPayPalCreditFinancingAmount {
  final String? currency;
  final String? value;

  BTPayPalCreditFinancingAmount({
    this.currency,
    this.value,
  });

  factory BTPayPalCreditFinancingAmount.fromJson(dynamic source) {
    return BTPayPalCreditFinancingAmount(
      currency: source['currency'],
      value: source['value'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'currency': currency,
      'value': value,
    };
  }
}

