package com.example.tripbadu;

import android.app.Activity;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PaymentsUtil {

    private static final BigDecimal MICROS = new BigDecimal(1000000);

    private static JSONObject getBaseRequest() throws JSONException {
        return new JSONObject().put("apiVersion", 2).put("apiVersionMinor", 0);
    }

    private static JSONObject getGatewayTokenizationSpecification() throws JSONException {
        return new JSONObject() {{
            put("type", "PAYMENT_GATEWAY");
            put("parameters", new JSONObject() {{
                put("gateway", "stripe");
                put("stripe:version", "2023-10-16"); // Update to latest Stripe API version
                put("stripe:publishableKey", "pk_test_51TGhxD2LdE4pr3IryKu9LARLJGX9EPWkeD3qzTw2J77agDP3EFisQQhCX971Vpc2WJ6z0fvOvKJk7HL7fdkY1KCP00n59k3R3X");
            }});
        }};
    }

    private static JSONArray getAllowedCardNetworks() {
        return new JSONArray()
                .put("AMEX")
                .put("DISCOVER")
                .put("INTERAC")
                .put("JCB")
                .put("MASTERCARD")
                .put("VISA");
    }

    private static JSONArray getAllowedCardAuthMethods() {
        return new JSONArray()
                .put("PAN_ONLY")
                .put("CRYPTOGRAM_3DS");
    }

    private static JSONObject getBaseCardPaymentMethod() throws JSONException {
        JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");

        JSONObject parameters = new JSONObject();
        parameters.put("allowedAuthMethods", getAllowedCardAuthMethods());
        parameters.put("allowedCardNetworks", getAllowedCardNetworks());
        // Optionally, you can add billing address/phone number requirements here
        cardPaymentMethod.put("parameters", parameters);

        return cardPaymentMethod;
    }

    private static JSONObject getCardPaymentMethod() throws JSONException {
        JSONObject cardPaymentMethod = getBaseCardPaymentMethod();
        cardPaymentMethod.put("tokenizationSpecification", getGatewayTokenizationSpecification());

        return cardPaymentMethod;
    }

    public static IsReadyToPayRequest getIsReadyToPayRequest() {
        try {
            JSONObject isReadyToPayRequest = getBaseRequest();
            isReadyToPayRequest.put(
                    "allowedPaymentMethods", new JSONArray().put(getBaseCardPaymentMethod()));

            return IsReadyToPayRequest.fromJson(isReadyToPayRequest.toString());
        } catch (JSONException e) {
            return null;
        }
    }

    private static JSONObject getTransactionInfo(String price) throws JSONException {
        JSONObject transactionInfo = new JSONObject();
        transactionInfo.put("totalPrice", price);
        transactionInfo.put("totalPriceStatus", "FINAL");
        transactionInfo.put("countryCode", "US"); // Adjust to your country code
        transactionInfo.put("currencyCode", "LKR"); // Adjust to your currency
        transactionInfo.put("checkoutOption", "COMPLETE_IMMEDIATE_PURCHASE");

        return transactionInfo;
    }

    private static JSONObject getMerchantInfo() throws JSONException {
        return new JSONObject().put("merchantName", "TripBadu Store");
    }

    public static PaymentDataRequest getPaymentDataRequest(long priceCents) {
        try {
            JSONObject paymentDataRequest = PaymentsUtil.getBaseRequest();
            paymentDataRequest.put(
                    "allowedPaymentMethods", new JSONArray().put(PaymentsUtil.getCardPaymentMethod()));
            paymentDataRequest.put("transactionInfo", PaymentsUtil.getTransactionInfo(centsToString(priceCents)));
            paymentDataRequest.put("merchantInfo", PaymentsUtil.getMerchantInfo());

            /* An optional shipping address requirement is a common part of many checkout flows.
             * See the direct-integration guide for more details:
             * https://developers.google.com/pay/api/android/guides/tutorial#shipping-address
             */
            // paymentDataRequest.put("shippingAddressRequired", true);

            return PaymentDataRequest.fromJson(paymentDataRequest.toString());
        } catch (JSONException e) {
            return null;
        }
    }

    private static String centsToString(long cents) {
        return new BigDecimal(cents)
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_EVEN)
                .toString();
    }

    public static PaymentsClient createPaymentsClient(Activity activity) {
        Wallet.WalletOptions walletOptions =
                new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build();
        return Wallet.getPaymentsClient(activity, walletOptions);
    }
}
