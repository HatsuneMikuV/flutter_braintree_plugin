
class BTPostalAddress {
  BTPostalAddress({
    this.recipientName,
    this.streetAddress,
    this.extendedAddress,
    this.locality,
    this.countryCodeAlpha2,
    this.postalCode,
    this.region,
  });

  /// Creates a new instance from a JSON object.
  factory BTPostalAddress.fromJson(dynamic source) {
    return BTPostalAddress(
      recipientName: source['recipientName'],
      streetAddress: source['streetAddress'],
      extendedAddress: source['extendedAddress'],
      locality: source['locality'],
      countryCodeAlpha2: source['countryCodeAlpha2'],
      postalCode: source['postalCode'],
      region: source['region'],
    );
  }

  /// Optional. Recipient name for shipping address.
  final String? recipientName;

  /// Line 1 of the Address (eg. number, street, etc).
  final String? streetAddress;

  /// Optional line 2 of the Address (eg. suite, apt #, etc.).
  final String? extendedAddress;

  /// City name
  final String? locality;

  /// 2 letter country code.
  final String? countryCodeAlpha2;

  /// Zip code or equivalent is usually required for countries that have them. For a list of countries that do not have postal codes please refer to http://en.wikipedia.org/wiki/Postal_code.
  final String? postalCode;

  /// Either a two-letter state code (for the US), or an ISO-3166-2 country subdivision code of up to three letters.
  final String? region;

  /// Converts this postal address object into a JSON-encodable format.
  Map<String, dynamic> toJson() => {
    'recipientName': recipientName,
    'streetAddress': streetAddress,
    'extendedAddress': extendedAddress,
    'locality': locality,
    'countryCodeAlpha2': countryCodeAlpha2,
    'postalCode': postalCode,
    'region': region,
  };
}