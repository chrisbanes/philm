package app.philm.in.lib.controllers;


import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.philm.in.lib.Display;

@Singleton
public class AboutController extends BaseUiController<AboutController.AboutUi,
        AboutController.AboutUiCallbacks> {

    public static enum AboutItem {
        BUILD_VERSION, BUILD_TIME, OPEN_SOURCE, POWERED_BY_TMDB, POWERED_BY_TRAKT;
    }

    public interface AboutUi extends BaseUiController.Ui<AboutUiCallbacks> {}

    public interface AboutListUi extends AboutUi {
        void setItems(List<AboutItem> items);
    }

    public interface AboutOpenSourcesUi extends AboutUi {
        void showLicences(String url);
    }

    public interface AboutUiCallbacks {
        void onTitleChanged(String newTitle);
        void onItemClick(AboutItem item);
    }

    @Inject
    public AboutController() {
    }

    @Override
    public boolean handleIntent(String intentAction) {
        final Display display = getDisplay();
        if (Display.ACTION_ABOUT.equals(intentAction)) {
            if (display != null && !display.hasMainFragment()) {
                display.showAboutFragment();
            }
            return true;
        }
        return super.handleIntent(intentAction);
    }

    @Override
    protected AboutUiCallbacks createUiCallbacks(AboutUi ui) {
        return new AboutUiCallbacks() {
            @Override
            public void onTitleChanged(String newTitle) {
                updateDisplayTitle(newTitle);
            }

            @Override
            public void onItemClick(AboutItem item) {
                switch (item) {
                    case OPEN_SOURCE:
                        Display display = getDisplay();
                        if (display != null) {
                            display.showLicencesFragment();
                        }
                        break;
                }
            }
        };
    }

    @Override
    protected void onUiAttached(AboutUi ui) {
        super.onUiAttached(ui);
    }

    @Override
    protected void populateUi(AboutUi ui) {
        if (ui instanceof AboutListUi) {
            ((AboutListUi) ui).setItems(Arrays.asList(AboutItem.values()));
        } else if (ui instanceof AboutOpenSourcesUi) {
            ((AboutOpenSourcesUi) ui).showLicences("file:///android_asset/licences.html");
        }
    }
}
