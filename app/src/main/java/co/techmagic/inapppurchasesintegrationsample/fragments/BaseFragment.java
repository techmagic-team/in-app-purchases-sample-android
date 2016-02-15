package co.techmagic.inapppurchasesintegrationsample.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class BaseFragment extends Fragment {

    private ProgressDialog progressDialog;

    protected void showProgressDialog(String message) {
        progressDialog = ProgressDialog.show(getActivity(), "", message, true, false);
    }

    protected void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    protected void dismissKeyboard() {
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    protected void showMessage(int id) {
        Toast.makeText(getContext(), id, Toast.LENGTH_SHORT).show();
    }

    protected void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
