package app.philm.in.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.AboutController;

public class LicencesFragment extends Fragment implements AboutController.AboutOpenSourcesUi {

    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mWebView = new WebView(getActivity());
        mWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return mWebView;
    }

    private AboutController.AboutUiCallbacks mCallbacks;

    @Override
    public void onResume() {
        super.onResume();
        getController().attachUi(this);
    }

    @Override
    public void onPause() {
        getController().detachUi(this);
        super.onPause();
    }

    @Override
    public boolean isModal() {
        return false;
    }

    protected final boolean hasCallbacks() {
        return mCallbacks != null;
    }

    protected final AboutController.AboutUiCallbacks getCallbacks() {
        return mCallbacks;
    }

    @Override
    public void setCallbacks(AboutController.AboutUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    private AboutController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getAboutController();
    }

    @Override
    public void showLicences(String url) {
        mWebView.loadUrl(url);
    }
}
