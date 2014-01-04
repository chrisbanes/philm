package app.philm.in.controllers;

import android.content.Intent;

import java.util.Arrays;
import java.util.List;

import app.philm.in.AboutActivity;
import app.philm.in.BuildConfig;
import app.philm.in.Display;
import app.philm.in.R;
import app.philm.in.util.AppUtils;

public class AboutController extends BaseUiController<AboutController.AboutUi,
        AboutController.AboutUiCallbacks> {

    public static enum AboutItem {

        BUILD_VERSION(R.string.about_build_version_title, AppUtils.getVersionName()),
        BUILD_TIME(R.string.about_build_time_title, BuildConfig.BUILD_TIME),
        OPEN_SOURCE(R.string.about_open_source_title, R.string.about_open_source_content);

        private int mTitleId;
        private int mContentTextId;
        private String mContentText;

        private AboutItem(int titleId, String contentText) {
            mTitleId = titleId;
            mContentTextId = 0;
            mContentText = contentText;
        }

        private AboutItem(int titleId, int contentTextId) {
            mTitleId = titleId;
            mContentTextId = contentTextId;
            mContentText = null;
        }

        public int getTitleId() {
            return mTitleId;
        }

        public String getContentText() {
            return mContentText;
        }

        public int getContentTextId() {
            return mContentTextId;
        }
    }

    public interface AboutUi extends BaseUiController.Ui<AboutUiCallbacks> {}

    public interface AboutListUi extends AboutUi {
        void setItems(List<AboutItem> items);
    }

    public interface AboutOpenSourcesUi extends AboutUi {
        void showLicences(String url);
    }

    public interface AboutUiCallbacks {
        void onItemClick(AboutItem item);
    }

    @Override
    public boolean handleIntent(String intentAction) {
        final Display display = getDisplay();
        if (AboutActivity.ACTION_ABOUT.equals(intentAction)) {
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
            public void onItemClick(AboutItem item) {
                switch (item) {
                    case BUILD_VERSION:
                        break;
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
    protected void populateUi(AboutUi ui) {
        Display display = getDisplay();

        if (ui instanceof AboutListUi) {
            ((AboutListUi) ui).setItems(Arrays.asList(AboutItem.values()));
            if (display != null) {
                display.setActionBarTitle(R.string.about_title);
            }
        } else if (ui instanceof AboutOpenSourcesUi) {
            ((AboutOpenSourcesUi) ui).showLicences("file:///android_asset/licences.html");
            if (display != null) {
                display.setActionBarTitle(R.string.about_open_source_title);
            }
        }
    }
}
