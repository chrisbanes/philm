package app.philm.in.state;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.squareup.otto.Bus;
import com.uwetrottmann.tmdb.entities.Configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.philm.in.controllers.MainController;
import app.philm.in.controllers.MovieController;
import app.philm.in.model.PhilmAccount;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.model.SearchResult;

public final class ApplicationState implements BaseState, MoviesState, UserState {

    private static final int INITIAL_MOVIE_MAP_CAPACITY = 200;

    private final Bus mEventBus;

    private Map<String, PhilmMovie> mMovies;
    private SearchResult mSearchResult;

    private List<PhilmMovie> mLibrary;
    private List<PhilmMovie> mTrending;
    private List<PhilmMovie> mWatchlist;
    private Set<MovieController.Filter> mFilters;

    private PhilmAccount mAccount;
    private PhilmUserProfile mUserProfile;
    private String mUsername;
    private MainController.SideMenuItem mSelectedSideMenuItem;

    private Configuration mTmdbConfiguration;

    public ApplicationState(Bus eventBus) {
        mEventBus = Preconditions.checkNotNull(eventBus, "eventBus cannot null");
    }

    @Override
     public void registerForEvents(Object receiver) {
        mEventBus.register(receiver);
    }

    @Override
    public void unregisterForEvents(Object receiver) {
        mEventBus.unregister(receiver);
    }

    @Override
    public MainController.SideMenuItem getSelectedSideMenuItem() {
        return mSelectedSideMenuItem;
    }

    @Override
    public void setSelectedSideMenuItem(MainController.SideMenuItem item) {
        mSelectedSideMenuItem = item;
    }

    ///////////////////////////
    //
    // MovieState
    //
    ///////////////////////////

    @Override
    public Map<String, PhilmMovie> getMovies() {
        if (mMovies == null) {
            mMovies = new HashMap<String, PhilmMovie>(INITIAL_MOVIE_MAP_CAPACITY);
        }
        return mMovies;
    }

    @Override
    public List<PhilmMovie> getLibrary() {
        return mLibrary;
    }

    @Override
    public void setLibrary(List<PhilmMovie> items) {
        if (!Objects.equal(items, mLibrary)) {
            mLibrary = items;
            mEventBus.post(new LibraryChangedEvent());
        }
    }

    @Override
    public List<PhilmMovie> getTrending() {
        return mTrending;
    }

    @Override
    public void setTrending(List<PhilmMovie> items) {
        if (!Objects.equal(items, mTrending)) {
            mTrending = items;
            mEventBus.post(new TrendingChangedEvent());
        }
    }

    @Override
    public Set<MovieController.Filter> getFilters() {
        if (mFilters == null) {
            mFilters = new HashSet<MovieController.Filter>();
        }
        return mFilters;
    }

    @Override
    public List<PhilmMovie> getWatchlist() {
        return mWatchlist;
    }

    @Override
    public void setWatchlist(List<PhilmMovie> watchlist) {
        if (!Objects.equal(mWatchlist, watchlist)) {
            mWatchlist = watchlist;
            mEventBus.post(new WatchlistChangedEvent());
        }
    }

    @Override
    public SearchResult getSearchResult() {
        return mSearchResult;
    }

    @Override
    public void setSearchResult(SearchResult result) {
        mSearchResult = result;
        mEventBus.post(new SearchResultChangedEvent());
    }

    @Override
    public Configuration getTmdbConfiguration() {
        return mTmdbConfiguration;
    }

    @Override
    public void setTmdbConfiguration(Configuration configuration) {
        if (!Objects.equal(configuration, mTmdbConfiguration)) {
            mTmdbConfiguration = configuration;
            mEventBus.post(new TmdbConfigurationChangedEvent());
        }
    }

    ///////////////////////////
    //
    // UserState
    //
    ///////////////////////////

    @Override
    public void setCurrentAccount(PhilmAccount account) {
        if (!Objects.equal(mAccount, account)) {
            mAccount = account;
            mEventBus.post(new AccountChangedEvent());
        }
    }

    @Override
    public PhilmAccount getCurrentAccount() {
        return mAccount;
    }

    @Override
    public String getUsername() {
        return mUsername;
    }

    @Override
    public void setUsername(String username) {
        mUsername = username;
    }

    @Override
    public void setUserProfile(PhilmUserProfile profile) {
        if (!Objects.equal(profile, mUserProfile)) {
            mUserProfile = profile;
            mEventBus.post(new UserProfileChangedEvent());
        }
    }

    @Override
    public PhilmUserProfile getUserProfile() {
        return mUserProfile;
    }
}
