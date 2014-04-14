package app.philm.in.lib.state.mappers;

import com.uwetrottmann.tmdb.entities.Credits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.philm.in.lib.model.PhilmPerson;
import app.philm.in.lib.model.PhilmMovieCredit;
import app.philm.in.lib.state.MoviesState;

@Singleton
public class TmdbCastEntityMapper extends BaseEntityMapper<Credits.CastMember, PhilmPerson> {

    @Inject
    public TmdbCastEntityMapper(MoviesState state) {
        super(state);
    }

    @Override
    public PhilmPerson map(Credits.CastMember entity) {
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

    public List<PhilmMovieCredit> mapCredits(List<Credits.CastMember> entities) {
        final ArrayList<PhilmMovieCredit> credits = new ArrayList<>(entities.size());
        for (Credits.CastMember entity : entities) {
            credits.add(new PhilmMovieCredit(map(entity), entity.character, entity.order));
        }
        Collections.sort(credits);
        return credits;
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
