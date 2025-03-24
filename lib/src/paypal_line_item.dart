import 'enums.dart';

class BTPayPalLineItem {
  BTPayPalLineItem({
    required this.quantity,
    required this.unitAmount,
    required this.name,
    required this.kind,
    required this.unitTaxAmount,
    required this.itemDescription,
    required this.productCode,
    required this.url,
  });

  /// Number of units of the item purchased. This value must be a whole number and can't be negative or zero.
  final int quantity;

  /// Per-unit price of the item. Can include up to 2 decimal places. This value can't be negative or zero.
  final String unitAmount;

  /// Item name. Maximum 127 characters.
  final String name;

  /// Indicates whether the line item is a debit (sale) or credit (refund) to the customer.
  final BTPayPalLineItemKind kind;

  /// Optional: Per-unit tax price of the item. Can include up to 2 decimal places. This value can't be negative or zero.
  final String unitTaxAmount;

  /// Optional: Item description. Maximum 127 characters.
  final String itemDescription;

  /// Optional: Product or UPC code for the item. Maximum 127 characters.
  final String productCode;

  /// Optional: The URL to product information.
  final String url;

  /// Converts this line item object into a JSON-encodable format.
  Map<String, dynamic> toJson() => {
    'quantity': quantity,
    'unitAmount': unitAmount,
    'name': name,
    'kind': kind.index,
    'unitTaxAmount': unitTaxAmount,
    'itemDescription': itemDescription,
    'productCode': productCode,
    'url': url,
  };
}