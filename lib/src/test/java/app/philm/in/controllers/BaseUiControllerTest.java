package app.philm.in.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BaseUiControllerTest {

    private FakeUiController mController;

    @Mock private FakeUi mUi;

    @Before
    public void setup() {
        mController = new FakeUiController();
    }

    @Test
    public void testAttachUi_setCallback() {
        // When a UI attaches to the controller
        mController.attachUi(mUi);

        // The UI is given a callbacks instance
        verify(mUi).setCallbacks(any(FakeUiCallbacks.class));
    }

    @Test
    public void testAttachUi_populateUi() {
        // Given an init'd controller
        mController.init();

        // When a UI attaches to the controller
        mController.attachUi(mUi);

        // The controller populates the ui
        spy(mController).populateUi(mUi);
    }

    private static class FakeUiController extends BaseUiController<FakeUi, FakeUiCallbacks> {
        @Override
        protected FakeUiCallbacks createUiCallbacks(FakeUi ui) {
            return new FakeUiCallbacks() {};
        }
    }

    private interface FakeUi extends BaseUiController.Ui<FakeUiCallbacks> {}

    private interface FakeUiCallbacks {}

}
