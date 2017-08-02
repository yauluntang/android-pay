package com.bindo;

import com.google.android.gms.wallet.WalletConstants;

/**
 * Constants used by Google Wallet SDK Sample.
 */
public class Constants {

  // Environment to use when creating an instance of Wallet.WalletOptions
  public static final int WALLET_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST;

  public static final String MERCHANT_NAME = "Bindo";

  // Intent extra keys
  public static final String EXTRA_ITEM_ID = "EXTRA_ITEM_ID";
  public static final String EXTRA_MASKED_WALLET = "EXTRA_MASKED_WALLET";
  public static final String EXTRA_FULL_WALLET = "EXTRA_FULL_WALLET";

  public static final String CURRENCY_CODE_USD = "USD";

  // values to use with KEY_DESCRIPTION
  public static final String DESCRIPTION_LINE_ITEM_SHIPPING = "Shipping";
  public static final String DESCRIPTION_LINE_ITEM_TAX = "Tax";
  public static final int REQUEST_CODE_MASKED_WALLET = 12345;
  public static final int REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET = 12346;
  /**
   * Sample list of items for sale. The list would normally be fetched from
   * the merchant's servers.
   */
  public static final ItemInfo[] ITEMS_FOR_SALE = {
    new ItemInfo("Simple Bike", "Features", 300000000, 9990000, CURRENCY_CODE_USD,
      "seller data 0", 0),
    new ItemInfo("Adjustable Bike", "More features", 400000000, 9990000, CURRENCY_CODE_USD,
      "seller data 1", 0),
    new ItemInfo("Conference Bike", "Even more features", 600000000, 9990000,
      CURRENCY_CODE_USD, "seller data 2", 0)
  };

  // To change promotion item, change the item here and also corresponding text/image
  // in fragment_promo_address_lookup.xml layout.
  public static final int PROMOTION_ITEM = 2;

}
