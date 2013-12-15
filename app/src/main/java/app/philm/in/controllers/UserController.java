package app.philm.in.controllers;

import com.jakewharton.trakt.entities.Movie;

import java.util.List;

/**
 * Created by chris on 15/12/2013.
 */
public class UserController extends BaseUiController<UserController.UserUi,
        UserController.UserUiCallbacks> {

    public interface UserUi extends BaseUiController.Ui<UserUiCallbacks> {
    }

    public interface UserUiCallbacks {
    }

    public UserController() {
        super();
    }

    @Override
    protected UserUiCallbacks createUiCallbacks() {
        return new UserUiCallbacks() {
        };
    }


}
