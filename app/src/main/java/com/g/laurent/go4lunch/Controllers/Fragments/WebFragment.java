package com.g.laurent.go4lunch.Controllers.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.g.laurent.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebFragment extends Fragment {

    @BindView(R.id.webview)
    WebView mWebView;

    public WebFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, container, false);
        ButterKnife.bind(this, view);
        String EXTRA_LINK = "linkaddress";
        String link = null;

        if (getArguments() != null) {
            link = getArguments().getString(EXTRA_LINK);
        }
        mWebView.loadUrl(link);
        return view;
    }
}
