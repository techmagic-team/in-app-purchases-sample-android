package co.techmagic.inapppurchasesintegrationsample.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import co.techmagic.mentionmybiz.activities.BillingActivity;
import co.techmagic.mentionmybiz.billing.IabHelper;
import co.techmagic.mentionmybiz.billing.IabResult;
import co.techmagic.mentionmybiz.billing.Purchase;

public abstract class BillingFragment extends BaseFragment {
    private static final String TAG = BillingFragment.class.getCanonicalName();

    // (arbitrary) request code for the purchase flow
    private static final int RC_REQUEST = 10001;
    private BillingActivity billingActivity;
    private BillingListener billingListener;
    private IabHelper mHelper;
    private boolean isBillingSetUp;
    private String base64EncodedPublicKey;

    public void setBillingListener(BillingListener billingListener) {
        this.billingListener = billingListener;
    }

    public void setBase64EncodedPublicKey(String base64EncodedPublicKey) {
        this.base64EncodedPublicKey = base64EncodedPublicKey;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(getActivity() instanceof BillingActivity)) {
            throw new RuntimeException("Activity must be BillingActivity");
        }
        billingActivity = (BillingActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHelper = new IabHelper(getContext(), base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    isBillingSetUp = false;
                    if (billingListener != null) {
                        billingListener.onError("Problem setting up in-app billing: " + result);
                    }
                    return;
                } else {
                    isBillingSetUp = true;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) {
            return;
        }

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    protected void performPurchase(String productSku) {
        if (!isBillingSetUp) {
            if (billingListener != null) {
                billingListener.onError("Error purchasing");
            }
            return;
        }
         /* TODO: for security, generate your payload here for verification. See the comments on
             *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
             *        an empty string, but on a production app you should carefully generate
             *        this. */
        String payload = "";
        mHelper.launchPurchaseFlow(billingActivity, productSku, RC_REQUEST, mPurchaseFinishedProductListener, payload);
    }

    protected void performConsume(Purchase purchase) {
        if (!isBillingSetUp) {
            if (billingListener != null) {
                billingListener.onError("Error consuming");
            }
            return;
        }
        mHelper.consumeAsync(purchase, mConsumeFinishedListener);
    }

    /**
     * Callback for when a purchase is finished
     */
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedProductListener = new IabHelper.OnIabPurchaseFinishedListener() {

        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            if (mHelper == null) {
                return;
            }
            if (result.isFailure()) {
                if (billingListener != null) {
                    billingListener.onError("Error purchasing: " + result);
                }
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                if (billingListener != null) {
                    billingListener.onError("Error purchasing. Authenticity verification failed.");
                }
                return;
            }
            Log.d(TAG, "Purchase successful.");
            if (billingListener != null) {
                billingListener.onPurchaseFinished(purchase, result);
            }
        }

    };

    /**
     * Called when consumption is complete
     */
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {

        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) {
                return;
            }
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item
                Log.d(TAG, "Consumption successful. Provisioning.");
                if (billingListener != null) {
                    billingListener.onConsumeFinished(purchase, result);
                }
            } else {
                if (billingListener != null) {
                    billingListener.onError("Error while consuming: " + result);
                }
            }
            Log.d(TAG, "End consumption flow.");
        }

    };

    /**
     * Verifies the developer payload of a purchase.
     * @param p
     * @return
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    interface BillingListener {
        void onPurchaseFinished(Purchase purchase, IabResult result);
        void onConsumeFinished(Purchase purchase, IabResult result);
        void onError(String message);
    }

}
