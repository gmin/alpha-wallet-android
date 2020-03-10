package com.alphawallet.app.ui;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.MediaLinks;
import com.alphawallet.app.repository.EthereumNetworkRepository;
import com.alphawallet.app.router.HelpRouter;
import com.alphawallet.app.util.LocaleUtils;
import com.alphawallet.app.viewmodel.AdvancedSettingsViewModel;
import com.alphawallet.app.viewmodel.AdvancedSettingsViewModelFactory;
import com.alphawallet.app.viewmodel.MyAddressViewModel;
import com.alphawallet.app.viewmodel.MyAddressViewModelFactory;
import com.alphawallet.app.widget.AWalletConfirmationDialog;
import com.alphawallet.app.widget.SettingsItemView;
import com.crashlytics.android.Crashlytics;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

import static com.alphawallet.app.C.CHANGED_LOCALE;
import static com.alphawallet.app.C.EXTRA_LOCALE;
import static com.alphawallet.app.C.EXTRA_STATE;

public class AdvancedSettingsActivity extends BaseActivity {
    @Inject
    AdvancedSettingsViewModelFactory viewModelFactory;
    private AdvancedSettingsViewModel viewModel;

    private SettingsItemView console;
    private SettingsItemView clearBrowserCache;
    private SettingsItemView tokenScript;
    private SettingsItemView changeLanguage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AdvancedSettingsViewModel.class);

        setContentView(R.layout.activity_generic_settings);
        toolbar();
        setTitle(getString(R.string.title_advanced));

        viewModel.setLocale(this);

        initializeSettings();

        addSettingsToLayout();
    }

    private void initializeSettings() {
        console = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_settings_console)
                .withTitle(R.string.title_console)
                .withListener(this::onConsoleClicked)
                .build();

        clearBrowserCache = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_settings_cache)
                .withTitle(R.string.title_clear_browser_cache)
                .withListener(this::onClearBrowserCacheClicked)
                .build();

        tokenScript = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_settings_tokenscript)
                .withTitle(R.string.title_tokenscript)
                .withListener(this::onTokenScriptClicked)
                .build();

        changeLanguage = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_settings_language)
                .withTitle(R.string.title_change_language)
                .withListener(this::onChangeLanguageClicked)
                .build();

        //TODO: add change currency here. Use R.drawable.ic_currency for the icon

        changeLanguage.setSubtitle(LocaleUtils.getDisplayLanguage(viewModel.getDefaultLocale(), viewModel.getDefaultLocale()));
    }

    private void addSettingsToLayout() {
        LinearLayout advancedSettingsLayout = findViewById(R.id.layout);
        advancedSettingsLayout.addView(console);
        advancedSettingsLayout.addView(clearBrowserCache);

        if (!checkWritePermission() && EthereumNetworkRepository.extraChains() == null)
            advancedSettingsLayout.addView(tokenScript);

        advancedSettingsLayout.addView(changeLanguage);
    }

    private void onConsoleClicked() {
        // TODO: Implementation
    }

    private void onClearBrowserCacheClicked() {
        // TODO: Implementation
    }

    private void onTokenScriptClicked() {
        showXMLOverrideDialog();
    }

    private void onChangeLanguageClicked() {
        Intent intent = new Intent(this, SelectLocaleActivity.class);
        String currentLocale = viewModel.getDefaultLocale();
        intent.putExtra(EXTRA_LOCALE, currentLocale);
        intent.putParcelableArrayListExtra(EXTRA_STATE, viewModel.getLocaleList(this));
        startActivityForResult(intent, C.UPDATE_LOCALE);
    }

    private void showXMLOverrideDialog() {
        AWalletConfirmationDialog cDialog = new AWalletConfirmationDialog(this);
        cDialog.setTitle(R.string.enable_xml_override_dir);
        cDialog.setSmallText(R.string.explain_xml_override);
        cDialog.setMediumText(R.string.ask_user_about_xml_override);
        cDialog.setPrimaryButtonText(R.string.dialog_ok);
        cDialog.setPrimaryButtonListener(v -> {
            //ask for OS permission and write directory
            askWritePermission();
            cDialog.dismiss();
        });
        cDialog.setSecondaryButtonText(R.string.dialog_cancel_back);
        cDialog.setSecondaryButtonListener(v -> {
            cDialog.dismiss();
        });
        cDialog.show();
    }

    private void askWritePermission() {
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        Log.w(AdvancedSettingsActivity.class.getSimpleName(), "Folder write permission is not granted. Requesting permission");
        ActivityCompat.requestPermissions(this, permissions, HomeActivity.RC_ASSET_EXTERNAL_WRITE_PERM);
    }

    private boolean checkWritePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void updateLocale(Intent data) {
        if (data != null) {
            String newLocale = data.getStringExtra(C.EXTRA_LOCALE);
            sendBroadcast(new Intent(CHANGED_LOCALE));
            viewModel.updateLocale(newLocale, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case C.UPDATE_LOCALE: {
                updateLocale(data);
            }
            default: {
                super.onActivityResult(requestCode, resultCode, data);
                break;
            }
        }
    }
}
