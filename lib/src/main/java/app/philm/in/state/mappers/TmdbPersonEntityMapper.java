package app.philm.in.state.mappers;

import com.uwetrottmann.tmdb.entities.Person;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.philm.in.model.PhilmPerson;
import app.philm.in.state.MoviesState;

@Singleton
public class TmdbPersonEntityMapper extends BaseEntityMapper<Person, PhilmPerson> {

    @Inject
    public TmdbPersonEntityMapper(MoviesState state) {
        super(state);
    }

    @Override
    public PhilmPerson map(Person entity) {
        PhilmPerson item = getEntity(String.valueOf(entity.id));

        if (item == null) {
            // No item, so create one
            item = new PhilmPerson();
        }

        // We already have a movie, so just update it wrapped value
        item.setFromTmdb(entity);
        putEntity(item);

        return item;
    }

    @Override
    PhilmPerson getEntity(String id) {
        return mMoviesState.getPeople().get(id);
    }

    @Override
    void putEntity(PhilmPerson entity) {
        mMoviesState.getPeople().put(String.valueOf(entity.getTmdbId()), entity);
    }
}
