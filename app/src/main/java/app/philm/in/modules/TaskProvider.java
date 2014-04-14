package app.philm.in.modules;

import app.philm.in.modules.library.NetworkProvider;
import app.philm.in.modules.library.PersistenceProvider;
import app.philm.in.modules.library.StateProvider;
import app.philm.in.modules.library.UtilProvider;
import app.philm.in.lib.tasks.AddToTraktCollectionRunnable;
import app.philm.in.lib.tasks.AddToTraktWatchlistRunnable;
import app.philm.in.lib.tasks.CancelCheckinTraktRunnable;
import app.philm.in.lib.tasks.CheckinTraktRunnable;
import app.philm.in.lib.tasks.FetchTmdbConfigurationRunnable;
import app.philm.in.lib.tasks.FetchTmdbDetailMovieRunnable;
import app.philm.in.lib.tasks.FetchTmdbMovieCreditsRunnable;
import app.philm.in.lib.tasks.FetchTmdbMovieTrailersRunnable;
import app.philm.in.lib.tasks.FetchTmdbMoviesReleasesRunnable;
import app.philm.in.lib.tasks.FetchTmdbNowPlayingRunnable;
import app.philm.in.lib.tasks.FetchTmdbPersonCreditsRunnable;
import app.philm.in.lib.tasks.FetchTmdbPersonRunnable;
import app.philm.in.lib.tasks.FetchTmdbPopularRunnable;
import app.philm.in.lib.tasks.FetchTmdbRelatedMoviesRunnable;
import app.philm.in.lib.tasks.FetchTmdbSearchMoviesRunnable;
import app.philm.in.lib.tasks.FetchTmdbSearchPeopleRunnable;
import app.philm.in.lib.tasks.FetchTmdbUpcomingRunnable;
import app.philm.in.lib.tasks.FetchTraktDetailMovieRunnable;
import app.philm.in.lib.tasks.FetchTraktLibraryRunnable;
import app.philm.in.lib.tasks.FetchTraktRecommendationsRunnable;
import app.philm.in.lib.tasks.FetchTraktRelatedMoviesRunnable;
import app.philm.in.lib.tasks.FetchTraktTrendingRunnable;
import app.philm.in.lib.tasks.FetchTraktWatchingRunnable;
import app.philm.in.lib.tasks.FetchTraktWatchlistRunnable;
import app.philm.in.lib.tasks.MarkTraktMovieSeenRunnable;
import app.philm.in.lib.tasks.MarkTraktMovieUnseenRunnable;
import app.philm.in.lib.tasks.RemoveFromTraktCollectionRunnable;
import app.philm.in.lib.tasks.RemoveFromTraktWatchlistRunnable;
import app.philm.in.lib.tasks.SubmitTraktMovieRatingRunnable;
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
                FetchTmdbSearchPeopleRunnable.class
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
