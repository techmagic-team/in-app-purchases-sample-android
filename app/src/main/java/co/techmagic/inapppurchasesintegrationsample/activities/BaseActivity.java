package co.techmagic.inapppurchasesintegrationsample.activities;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import co.techmagic.inapppurchasesintegrationsample.R;
import co.techmagic.inapppurchasesintegrationsample.fragments.BaseFragment;


public class BaseActivity extends AppCompatActivity {

    protected void replaceFragment(@NonNull BaseFragment fragment, boolean shouldAddToBackStack) {
        replaceFragment(fragment, shouldAddToBackStack, null);
    }

    protected void replaceFragment(@NonNull BaseFragment fragment, boolean shouldAddToBackStack, String tag) {
        if (tag == null) {
            tag = fragment.getClass().getCanonicalName();
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragments_container, fragment, tag);
        if (shouldAddToBackStack) {
            fragmentTransaction.addToBackStack(tag);
        }
        fragmentTransaction.commit();
    }

    protected Fragment getTopFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragments_container);
    }

    protected Fragment getFragmentByTag(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    protected void clearFragmentsBackStack(FragmentActivity fragmentActivity) {
        FragmentManager manager = fragmentActivity.getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
