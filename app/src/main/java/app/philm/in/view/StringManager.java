package app.philm.in.view;

import android.content.Context;

import app.philm.in.BuildConfig;
import app.philm.in.R;
import app.philm.in.controllers.AboutController;
import app.philm.in.controllers.MainController;
import app.philm.in.controllers.MovieController;
import app.philm.in.network.NetworkError;
import app.philm.in.util.AppUtils;

public class StringManager {

    public static int getStringResId(MovieController.MovieQueryType movieQueryType) {
        switch (movieQueryType) {
            case POPULAR:
                return R.string.popular_title;
            case TRENDING:
                return R.string.trending_title;
            case LIBRARY:
                return R.string.library_title;
            case WATCHLIST:
                return R.string.watchlist_title;
            case SEARCH:
                return R.string.search_title;
            case UPCOMING:
                return R.string.upcoming_title;
            case RECOMMENDED:
                return R.string.recommended_title;
            case NOW_PLAYING:
                return R.string.in_theatres_title;
            case RELATED:
                return R.string.related_movies;
            case CAST:
                return R.string.cast_movies;
        }
        return R.string.app_name;
    }

    public static int getStringResId(MovieController.DiscoverTab tab) {
        switch (tab) {
            case POPULAR:
                return R.string.popular_title;
            case IN_THEATRES:
                return R.string.in_theatres_title;
            case UPCOMING:
                return R.string.upcoming_title;
            case RECOMMENDED:
                return R.string.recommended_title;
        }
        return 0;
    }

    public static int getStringResId(MainController.SideMenuItem item) {
        switch (item) {
            case DISCOVER:
                return R.string.discover_title;
            case TRENDING:
                return R.string.trending_title;
            case LIBRARY:
                return R.string.library_title;
            case WATCHLIST:
                return R.string.watchlist_title;
            case SEARCH:
                return R.string.search_title;
        }
        return R.string.app_name;
    }

    public static int getStringResId(MovieController.Filter filter) {
        switch (filter) {
            case COLLECTION:
                return R.string.filter_collection;
            case SEEN:
                return R.string.filter_seen;
            case UNSEEN:
                return R.string.filter_unseen;
            case NOT_RELEASED:
                return R.string.filter_upcoming;
            case RELEASED:
                return R.string.filter_released;
            case UPCOMING:
                return R.string.filter_upcoming;
            case SOON:
                return R.string.filter_soon;
            case HIGHLY_RATED:
                return R.string.filter_highly_rated;
        }
        return 0;
    }

    public static int getStringResId(NetworkError error) {
        switch (error) {
            case UNAUTHORIZED:
                return R.string.error_unauthorized;
            case NETWORK_ERROR:
                return R.string.error_network;
            case NOT_FOUND_TRAKT:
                return R.string.error_movie_not_found_trakt;
            case NOT_FOUND_TMDB:
                return R.string.error_movie_not_found_tmdb;
            case UNKNOWN:
            default:
                return R.string.error_unknown;
        }
    }

    public static int getTitleResId(AboutController.AboutItem item) {
        switch (item) {
            case BUILD_VERSION:
                return R.string.about_build_version_title;
            case BUILD_TIME:
                return R.string.about_build_time_title;
            case OPEN_SOURCE:
                return R.string.about_open_source_title;
            case POWERED_BY_TMDB:
                return R.string.about_powered_tmdb_title;
            case POWERED_BY_TRAKT:
                return R.string.about_powered_trakt_title;
        }
        return 0;
    }

    public static String getSubtitle(Context context, AboutController.AboutItem item) {
        switch (item) {
            case BUILD_VERSION:
                return AppUtils.getVersionName();
            case BUILD_TIME:
                return BuildConfig.BUILD_TIME;
            case OPEN_SOURCE:
                return context.getString(R.string.about_open_source_content);
            case POWERED_BY_TMDB:
                return context.getString(R.string.about_powered_tmdb_content);
            case POWERED_BY_TRAKT:
                return context.getString(R.string.about_powered_trakt_content);
        }
        return null;
    }

}
