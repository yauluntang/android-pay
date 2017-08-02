/**
 */
package com.bindo;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import java.util.Date;

public class AndroidPayCordovaPlugin extends CordovaPlugin {
  private static final String TAG = "AndroidPayCordovaPlugin";

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing AndroidPayCordovaPlugin");
  }

  @Override
  public void onConnectionFailed(ConnectionResult result) {
      // An unresolvable error has occurred and a connection to Google APIs
      // could not be established. Display an error message, or handle
      // the failure silently
       // ...
  }

  public void walletRequest(){
        String publicKey = "BNSUODPqWgPLRPp368MkTJIH+HAgNGQ5l61BCKeKe7xxteZoDR2M7jVLLY9H7Ai8OdS5Q1CLpBFXv2LLMb1sHxU=";
        PaymentMethodTokenizationParameters parameters =
          PaymentMethodTokenizationParameters.newBuilder()
              .setPaymentMethodTokenizationType(PaymentMethodTokenizationType.NETWORK_TOKEN)
              .addParameter("publicKey", publicKey)
              .build();

        MaskedWalletRequest request = MaskedWalletRequest.newBuilder()
           .setMerchantName(Constants.MERCHANT_NAME)
           .setPhoneNumberRequired(true)
           .setShippingAddressRequired(true)
           .setCurrencyCode(Constants.CURRENCY_CODE_USD)
           .setEstimatedTotalPrice(cartTotal)
                   // Create a Cart with the current line items. Provide all the information
                   // available up to this point with estimates for shipping and tax included.
           .setCart(Cart.newBuilder()
                   .setCurrencyCode(Constants.CURRENCY_CODE_USD)
                   .setTotalPrice(cartTotal)
                   .setLineItems(lineItems)
                   .build())
           .setPaymentMethodTokenizationParameters(parameters)
           .build();

           cordova.setActivityResultCallback (this);
  }

  public void checkAndroidPayAvailability() {
    GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
      .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
      .addApi(Drive.API)
      .addScope(Drive.SCOPE_FILE)
      .build();
    Wallet.Payments.isReadyToPay(mGoogleApiClient).setResultCallback( new ResultCallback<BooleanResult>() {
                  @Override
                  public void onResult(@NonNull BooleanResult booleanResult) {


                      if (booleanResult.getStatus().isSuccess()) {
                          if (booleanResult.getValue()) {
                                walletRequest();
                          } else {

                          }
                      } else {
                          // Error making isReadyToPay call
                          Log.e(TAG, "isReadyToPay:" + booleanResult.getStatus());
                      }
                  }
          });
  }
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

    if ( action.equals("pay")){
        checkAndroidPayAvailability();
    }

    if(action.equals("echo")) {



      String phrase = args.getString(0);
      // Echo back the first argument
      Log.d(TAG, phrase);
    } else if(action.equals("getDate")) {
      // An example of returning data back to the web layer
      final PluginResult result = new PluginResult(PluginResult.Status.OK, (new Date()).toString());
      callbackContext.sendPluginResult(result);
    }
    return true;
  }

}
