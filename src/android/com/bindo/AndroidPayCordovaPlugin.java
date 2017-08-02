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

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ResultCallback;

import com.google.android.gms.common.api.Scope;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;

import java.util.Date;

public class AndroidPayCordovaPlugin extends CordovaPlugin  implements GoogleApiClient.OnConnectionFailedListener {
  private static final String TAG = "AndroidPayCordovaPlugin";
  private GoogleApiClient mGoogleApiClient;
  private String mCartTotal = "";
  public static final String ACTION_IS_AVAILABLE = "isAvailable";
  public static final String ACTION_LOGIN = "login";
  public static final String ACTION_TRY_SILENT_LOGIN = "trySilentLogin";
  public static final String ACTION_LOGOUT = "logout";
  public static final String ACTION_DISCONNECT = "disconnect";
  public static final String ACTION_GET_SIGNING_CERTIFICATE_FINGERPRINT = "getSigningCertificateFingerprint";

  private final static String FIELD_ACCESS_TOKEN      = "accessToken";
  private final static String FIELD_TOKEN_EXPIRES     = "expires";
  private final static String FIELD_TOKEN_EXPIRES_IN  = "expires_in";
  private final static String VERIFY_TOKEN_URL        = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=";

  //String options/config object names passed in to login and trySilentLogin
  public static final String ARGUMENT_WEB_CLIENT_ID = "webClientId";
  public static final String ARGUMENT_SCOPES = "scopes";
  public static final String ARGUMENT_OFFLINE_KEY = "offline";
  public static final String ARGUMENT_HOSTED_DOMAIN = "hostedDomain";

  private static CallbackContext callbackContext;

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // retrieve the error code, if available
    int errorCode = -1;

    if (data != null) {
      errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
    }
    switch (requestCode) {
      case Constants.REQUEST_CODE_MASKED_WALLET:
        switch (resultCode) {
          case Activity.RESULT_OK:
            if (data != null) {
              MaskedWallet maskedWallet =
                data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
              launchConfirmationPage(maskedWallet);
            }
            break;
          case Activity.RESULT_CANCELED:
            break;
          default:

            break;
        }
        break;
      case Constants.REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET:
        switch (resultCode) {
          case Activity.RESULT_OK:
            if (data != null) {
              // Get payment method token
              FullWallet fullWallet =
                data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
              PaymentMethodToken token = fullWallet.getPaymentMethodToken();

// Get the JSON of the token object as a String
              String tokenJSON = token.getToken();
              this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, tokenJSON));
            }
            break;
          case Activity.RESULT_CANCELED:
            break;
          default:

            break;
        }
        break;
      case WalletConstants.RESULT_ERROR:

        break;
      default:
        super.onActivityResult(requestCode, resultCode, data);
        break;
    }
  }

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing AndroidPayCordovaPlugin");
  }

  public void onConnectionFailed(ConnectionResult result) {
      // An unresolvable error has occurred and a connection to Google APIs
      // could not be established. Display an error message, or handle
      // the failure silently
       // ...
  }


  public void walletRequest(String cartTotal){
    mCartTotal = cartTotal;
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
               .setTotalPrice(mCartTotal)
               .build())
       .setPaymentMethodTokenizationParameters(parameters)
       .build();
    Wallet.Payments.loadMaskedWallet(mGoogleApiClient, request, Constants.REQUEST_CODE_MASKED_WALLET);
           cordova.setActivityResultCallback (this);
  }
  private synchronized void buildGoogleApiClient(JSONObject clientOptions) throws JSONException {
    if (clientOptions == null) {
      return;
    }

    //If options have been passed in, they could be different, so force a rebuild of the client
    // disconnect old client iff it exists
    if (this.mGoogleApiClient != null) this.mGoogleApiClient.disconnect();
    // nullify
    this.mGoogleApiClient = null;

    Log.i(TAG, "Building Google options");

    // Make our SignIn Options builder.
    GoogleSignInOptions.Builder gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN);

    // request the default scopes
    gso.requestEmail().requestProfile();

    // We're building the scopes on the Options object instead of the API Client
    // b/c of what was said under the "addScope" method here:
    // https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder.html#public-methods
    String scopes = clientOptions.optString(ARGUMENT_SCOPES, null);

    if (scopes != null && !scopes.isEmpty()) {
      // We have a string of scopes passed in. Split by space and request
      for (String scope : scopes.split(" ")) {
        gso.requestScopes(new Scope(scope));
      }
    }

    // Try to get web client id
    String webClientId = clientOptions.optString(ARGUMENT_WEB_CLIENT_ID, null);

    // if webClientId included, we'll request an idToken
    if (webClientId != null && !webClientId.isEmpty()) {
      gso.requestIdToken(webClientId);

      // if webClientId is included AND offline is true, we'll request the serverAuthCode
      if (clientOptions.optBoolean(ARGUMENT_OFFLINE_KEY, false)) {
        gso.requestServerAuthCode(webClientId, false);
      }
    }

    // Try to get hosted domain
    String hostedDomain = clientOptions.optString(ARGUMENT_HOSTED_DOMAIN, null);

    // if hostedDomain included, we'll request a hosted domain account
    if (hostedDomain != null && !hostedDomain.isEmpty()) {
      gso.setHostedDomain(hostedDomain);
    }

    //Now that we have our options, let's build our Client
    Log.i(TAG, "Building GoogleApiClient");

    GoogleApiClient.Builder builder = new GoogleApiClient.Builder(webView.getContext())
      .addOnConnectionFailedListener(this)
      .addApi(Auth.GOOGLE_SIGN_IN_API, gso.build());

    this.mGoogleApiClient = builder.build();

    Log.i(TAG, "GoogleApiClient built");
  }

  public void checkAndroidPayAvailability(String cartTotal) {

    mCartTotal = cartTotal;

    Wallet.Payments.isReadyToPay(mGoogleApiClient).setResultCallback(new ResultCallback<BooleanResult>() {
                  @Override
                  public void onResult(@NonNull BooleanResult booleanResult) {


                      if (booleanResult.getStatus().isSuccess()) {
                          if (booleanResult.getValue()) {
                                walletRequest(mCartTotal);
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

    if ( action.equals("preparepay")){
      buildGoogleApiClient(args.optJSONObject(0));
      checkAndroidPayAvailability(args.getString(0));
      this.callbackContext = callbackContext;
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

  private void launchConfirmationPage(MaskedWallet maskedWallet){
    String googleTransactionId = maskedWallet.getGoogleTransactionId();
    FullWalletRequest request = FullWalletRequest.newBuilder()
      .setGoogleTransactionId(googleTransactionId)
      .setCart(Cart.newBuilder()
        .setCurrencyCode(Constants.CURRENCY_CODE_USD)
        .setTotalPrice(mCartTotal)
        .build())
      .build();

    Wallet.Payments.loadFullWallet(mGoogleApiClient, request, Constants.REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET);
  }

}
