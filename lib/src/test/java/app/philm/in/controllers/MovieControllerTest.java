package app.philm.in.controllers;

import com.squareup.otto.Bus;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import app.philm.in.state.ApplicationState;
import app.philm.in.state.AsyncDatabaseHelper;
import app.philm.in.state.MoviesState;
import app.philm.in.test.FakeBackgroundExecutor;
import app.philm.in.trakt.Trakt;
import app.philm.in.util.BackgroundExecutor;
import app.philm.in.util.Logger;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MovieControllerTest {

    private MovieController mController;
    private MoviesState mState;
    private BackgroundExecutor mExecutor;

    @Mock private Bus mEventBus;
    @Mock private Trakt mTraktClient;
    @Mock private AsyncDatabaseHelper mDbHelper;
    @Mock private Logger mLogger;

    @Mock private MovieController.MovieUi mUi;

    @Captor private ArgumentCaptor<MovieController.MovieUiCallbacks> mUiCallbacksCaptor;
    private MovieController.MovieUiCallbacks mUiCallbacks;

    @Before
    public void setup() {
        mState = new ApplicationState(mEventBus);
        mExecutor = new FakeBackgroundExecutor();

        mController = new MovieController(mState, mTraktClient, mExecutor, mDbHelper, mLogger);
    }

    private void attachedUi() {
        mController.attachUi(mUi);
        verify(mUi).setCallbacks(mUiCallbacksCaptor.capture());
        mUiCallbacks = mUiCallbacksCaptor.getValue();
    }

}
