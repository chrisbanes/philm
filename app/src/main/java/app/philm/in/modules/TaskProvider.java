package app.philm.in.modules;

import app.philm.in.modules.library.NetworkProvider;
import app.philm.in.modules.library.PersistenceProvider;
import app.philm.in.modules.library.StateProvider;
import app.philm.in.modules.library.UtilProvider;
import app.philm.in.tasks.AddToTraktCollectionRunnable;
import app.philm.in.tasks.AddToTraktWatchlistRunnable;
import app.philm.in.tasks.CancelCheckinTraktRunnable;
import app.philm.in.tasks.CheckinTraktRunnable;
import app.philm.in.tasks.FetchTmdbConfigurationRunnable;
import app.philm.in.tasks.FetchTmdbDetailMovieRunnable;
import app.philm.in.tasks.FetchTmdbMovieCreditsRunnable;
import app.philm.in.tasks.FetchTmdbMovieImagesRunnable;
import app.philm.in.tasks.FetchTmdbMovieTrailersRunnable;
import app.philm.in.tasks.FetchTmdbMoviesReleasesRunnable;
import app.philm.in.tasks.FetchTmdbNowPlayingRunnable;
import app.philm.in.tasks.FetchTmdbPersonCreditsRunnable;
import app.philm.in.tasks.FetchTmdbPersonRunnable;
import app.philm.in.tasks.FetchTmdbPopularRunnable;
import app.philm.in.tasks.FetchTmdbRelatedMoviesRunnable;
import app.philm.in.tasks.FetchTmdbSearchMoviesRunnable;
import app.philm.in.tasks.FetchTmdbSearchPeopleRunnable;
import app.philm.in.tasks.FetchTmdbUpcomingRunnable;
import app.philm.in.tasks.FetchTraktDetailMovieRunnable;
import app.philm.in.tasks.FetchTraktLibraryRunnable;
import app.philm.in.tasks.FetchTraktRecommendationsRunnable;
import app.philm.in.tasks.FetchTraktRelatedMoviesRunnable;
import app.philm.in.tasks.FetchTraktTrendingRunnable;
import app.philm.in.tasks.FetchTraktWatchingRunnable;
import app.philm.in.tasks.FetchTraktWatchlistRunnable;
import app.philm.in.tasks.MarkTraktMovieSeenRunnable;
import app.philm.in.tasks.MarkTraktMovieUnseenRunnable;
import app.philm.in.tasks.RemoveFromTraktCollectionRunnable;
import app.philm.in.tasks.RemoveFromTraktWatchlistRunnable;
import app.philm.in.tasks.SubmitTraktMovieRatingRunnable;
import dagger.Module;

@Module(
        injects = {
                AddToTraktCollectionRunnable.class,
                AddToTraktWatchlistRunnable.class,
                FetchTmdbConfigurationRunnable.class,
                FetchTmdbDetailMovieRunnable.class,
                FetchTmdbMoviesReleasesRunnable.class,
                FetchTmdbRelatedMoviesRunnable.class,
                FetchTmdbNowPlayingRunnable.class,
                FetchTmdbPopularRunnable.class,
                FetchTmdbRelatedMoviesRunnable.class,
                FetchTmdbSearchMoviesRunnable.class,
                FetchTmdbUpcomingRunnable.class,
                FetchTraktDetailMovieRunnable.class,
                FetchTraktLibraryRunnable.class,
                FetchTraktRecommendationsRunnable.class,
                FetchTraktRelatedMoviesRunnable.class,
                FetchTraktTrendingRunnable.class,
                FetchTraktWatchlistRunnable.class,
                MarkTraktMovieSeenRunnable.class,
                MarkTraktMovieUnseenRunnable.class,
                RemoveFromTraktCollectionRunnable.class,
                RemoveFromTraktWatchlistRunnable.class,
                SubmitTraktMovieRatingRunnable.class,
                FetchTmdbMovieCreditsRunnable.class,
                FetchTmdbMovieTrailersRunnable.class,
                FetchTraktWatchingRunnable.class,
                CheckinTraktRunnable.class,
                CancelCheckinTraktRunnable.class,
                FetchTmdbPersonCreditsRunnable.class,
                FetchTmdbPersonRunnable.class,
                FetchTmdbSearchPeopleRunnable.class,
                FetchTmdbMovieImagesRunnable.class
        },
        includes = {
                PersistenceProvider.class,
                StateProvider.class,
                UtilProvider.class,
                NetworkProvider.class
        }
)
public class TaskProvider {
}
