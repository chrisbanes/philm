package app.philm.in;

import com.squareup.otto.Bus;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import java.util.concurrent.ExecutorService;

import app.philm.in.controllers.MainController;
import app.philm.in.controllers.MovieController;
import app.philm.in.controllers.UserController;
import app.philm.in.state.ApplicationState;
import app.philm.in.trakt.Trakt;


public class PhilmActivity extends Activity implements MovieController.MovieControllerProvider,
        UserController.UserControllerProvider, MainController.MainControllerProvider {

    private ActionBarDrawerToggle mDrawerToggle;

    private MainController mMainController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Bus bus = Container.getInstance(this).getEventBus();
        Trakt trakt = Container.getInstance(this).getTraktClient();
        ExecutorService service = Container.getInstance(this).getExecutor();

        Display display = new Display(this);
        ApplicationState state = new ApplicationState(bus);

        UserController userController = new UserController(display, state);
        MovieController movieController = new MovieController(display, state, trakt, service);

        mMainController = new MainController(userController, movieController, display);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer,
                    R.string.drawer_open_content_desc, R.string.drawer_closed_content_desc);

            drawerLayout.setDrawerListener(mDrawerToggle);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mMainController.init();
    }

    @Override
    protected void onPause() {
        mMainController.suspend();
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public MovieController getMovieController() {
        return mMainController.getMovieController();
    }

    @Override
    public UserController getUserController() {
        return mMainController.getUserController();
    }

    @Override
    public MainController getMainController() {
        return mMainController;
    }
}
