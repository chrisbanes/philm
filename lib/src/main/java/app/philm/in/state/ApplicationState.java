package app.philm.in.state;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.squareup.otto.Bus;

import android.support.v4.util.ArrayMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.philm.in.controllers.MainController;
import app.philm.in.controllers.MovieController;
import app.philm.in.model.PhilmAccount;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmPerson;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.model.TmdbConfiguration;
import app.philm.in.model.WatchingMovie;
import app.philm.in.util.TextUtils;

public final class ApplicationState implements BaseState, MoviesState, UserState {

    private static final int INITIAL_MOVIE_MAP_CAPACITY = 200;

    private final Bus mEventBus;

    private Map<String, PhilmMovie> mTmdbIdMovies;
    private Map<String, PhilmMovie> mImdbIdMovies;
    private Map<String, PhilmPerson> mPeople;

    private SearchResult mSearchResult;

    private List<PhilmMovie> mLibrary;
    private List<PhilmMovie> mTrending;
    private MoviePaginatedResult mPopular;
    private MoviePaginatedResult mNowPlaying;
    private MoviePaginatedResult mUpcoming;
    private List<PhilmMovie> mWatchlist;
    private List<PhilmMovie> mRecommended;

    private WatchingMovie mWatching;

    private Set<MovieController.MovieFilter> mFilters;

    private PhilmAccount mAccount;
    private PhilmUserProfile mUserProfile;
    private String mUsername;
    private MainController.SideMenuItem mSelectedSideMenuItem;

    private TmdbConfiguration mTmdbConfiguration;

    public ApplicationState(Bus eventBus) {
        mEventBus = Preconditions.checkNotNull(eventBus, "eventBus cannot null");

        mTmdbIdMovies = new ArrayMap<>(INITIAL_MOVIE_MAP_CAPACITY);
        mImdbIdMovies = new ArrayMap<>(INITIAL_MOVIE_MAP_CAPACITY);
        mPeople = new ArrayMap<>();
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
    public Map<String, PhilmMovie> getTmdbIdMovies() {
        return mTmdbIdMovies;
    }

    @Override
    public Map<String, PhilmMovie> getImdbIdMovies() {
        return mImdbIdMovies;
    }

    @Override
    public PhilmMovie getMovie(final String id) {
        PhilmMovie movie = mTmdbIdMovies.get(id);

        if (movie == null) {
            movie = mImdbIdMovies.get(id);
        }

        return movie;
    }

    @Override
    public PhilmMovie getMovie(int id) {
        return getMovie(String.valueOf(id));
    }

    @Override
    public void putMovie(PhilmMovie movie) {
        if (!TextUtils.isEmpty(movie.getImdbId())) {
            mImdbIdMovies.put(movie.getImdbId(), movie);
        }
        if (movie.getTmdbId() != null) {
            mTmdbIdMovies.put(String.valueOf(movie.getTmdbId()), movie);
        }
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
    public MoviePaginatedResult getPopular() {
        return mPopular;
    }

    @Override
    public void setPopular(MoviePaginatedResult items) {
        mPopular = items;
        mEventBus.post(new PopularChangedEvent());
    }

    @Override
    public MoviePaginatedResult getNowPlaying() {
        return mNowPlaying;
    }

    @Override
    public void setNowPlaying(MoviePaginatedResult nowPlaying) {
        mNowPlaying = nowPlaying;
        mEventBus.post(new InTheatresChangedEvent());
    }

    @Override
    public Set<MovieController.MovieFilter> getFilters() {
        if (mFilters == null) {
            mFilters = new HashSet<>();
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
    public void setRecommended(List<PhilmMovie> recommended) {
        if (!Objects.equal(mRecommended, recommended)) {
            mRecommended = recommended;
            mEventBus.post(new RecommendedChangedEvent());
        }
    }

    @Override
    public List<PhilmMovie> getRecommended() {
        return mRecommended;
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
    public TmdbConfiguration getTmdbConfiguration() {
        return mTmdbConfiguration;
    }

    @Override
    public void setTmdbConfiguration(TmdbConfiguration configuration) {
        if (!Objects.equal(configuration, mTmdbConfiguration)) {
            mTmdbConfiguration = configuration;
            mEventBus.post(new TmdbConfigurationChangedEvent());
        }
    }

    @Override
    public MoviePaginatedResult getUpcoming() {
        return mUpcoming;
    }

    @Override
    public void setUpcoming(MoviePaginatedResult upcoming) {
        mUpcoming = upcoming;
        mEventBus.post(new UpcomingChangedEvent());
    }

    @Override
    public WatchingMovie getWatchingMovie() {
        return mWatching;
    }

    @Override
    public void setWatchingMovie(WatchingMovie movie) {
        mWatching = movie;
        mEventBus.post(new WatchingMovieUpdatedEvent());
    }

    @Override
    public Map<String, PhilmPerson> getPeople() {
        return mPeople;
    }

    @Override
    public PhilmPerson getPerson(int id) {
        return getPerson(String.valueOf(id));
    }

    @Override
    public PhilmPerson getPerson(String id) {
        return mPeople.get(id);
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
