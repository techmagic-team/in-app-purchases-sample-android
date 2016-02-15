package co.techmagic.inapppurchasesintegrationsample.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;

import co.techmagic.inapppurchasesintegrationsample.fragments.BillingFragment;

public abstract class BillingActivity extends BaseActivity {

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getTopFragment();
        //need to pass the result to the fragment
        if (fragment instanceof BillingFragment) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

}
