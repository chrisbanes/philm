package app.philm.in.view;

import app.philm.in.R;
import app.philm.in.controllers.MainController;
import app.philm.in.controllers.MovieController;

public class StringManager {

    public static int getStringResId(MovieController.MovieQueryType movieQueryType) {
        switch (movieQueryType) {
            case TRENDING:
                return R.string.trending_title;
            case LIBRARY:
                return R.string.library_title;
            case WATCHLIST:
                return R.string.watchlist_title;
            case SEARCH:
                return R.string.search_title;
        }
        return 0;
    }

    public static int getStringResId(MainController.SideMenuItem item) {
        switch (item) {
            case TRENDING:
                return R.string.trending_title;
            case LIBRARY:
                return R.string.library_title;
            case WATCHLIST:
                return R.string.watchlist_title;
            case SEARCH:
                return R.string.search_title;
        }
        return 0;
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

}
