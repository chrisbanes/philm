/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.philm.in.tasks;

import com.google.gson.Gson;

import com.uwetrottmann.tmdb.Tmdb;
import com.uwetrottmann.tmdb.entities.Configuration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.inject.Inject;

import app.philm.in.Constants;
import app.philm.in.model.TmdbConfiguration;
import app.philm.in.network.NetworkCallRunnable;
import app.philm.in.state.MoviesState;
import app.philm.in.util.FileManager;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.Logger;
import retrofit.RetrofitError;

public class FetchTmdbConfigurationRunnable extends NetworkCallRunnable<TmdbConfiguration> {

    private static final String LOG_TAG = FetchTmdbConfigurationRunnable.class.getSimpleName();
    private static final String FILENAME_TMDB_CONFIG = "tmdb.config";

    @Inject Logger mLogger;
    @Inject Tmdb mTmdbClient;
    @Inject ImageHelper mImageHelper;
    @Inject MoviesState mMoviesState;
    @Inject FileManager mFileManager;

    @Override
    public TmdbConfiguration doBackgroundCall() throws RetrofitError {
        TmdbConfiguration configuration = getConfigFromFile();

        if (configuration != null && configuration.isValid()) {
            if (Constants.DEBUG) {
                mLogger.d(LOG_TAG, "Got valid TMDB config from file");
            }
        } else {
            if (Constants.DEBUG) {
                mLogger.d(LOG_TAG, "Fetching TMDB config from network");
            }

            // No config in file, so download from web
            Configuration tmdbConfig = mTmdbClient.configurationService().configuration();

            if (tmdbConfig != null) {
                // Downloaded config from web so file it to file
                configuration = new TmdbConfiguration();
                configuration.setFromTmdb(tmdbConfig);
                writeConfigToFile(configuration);
            } else {
                configuration = null;
            }
        }

        return configuration;
    }

    @Override
    public void onSuccess(TmdbConfiguration result) {
        if (result != null) {
            mImageHelper.setTmdbBaseUrl(result.getImagesBaseUrl());
            mImageHelper.setTmdbBackdropSizes(result.getImagesBackdropSizes());
            mImageHelper.setTmdbPosterSizes(result.getImagesPosterSizes());
            mImageHelper.setTmdbProfileSizes(result.getImagesProfileSizes());
        }

        mMoviesState.setTmdbConfiguration(result);
    }

    @Override
    public void onError(RetrofitError re) {
        // Ignore
    }

    private TmdbConfiguration getConfigFromFile() {
        File file = mFileManager.getFile(FILENAME_TMDB_CONFIG);
        if (file.exists()) {
            FileReader reader = null;
            try {
                reader = new FileReader(file);
                Gson gson = new Gson();
                return gson.fromJson(reader, TmdbConfiguration.class);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private void writeConfigToFile(TmdbConfiguration configuration) {
        FileWriter writer = null;

        try {
            File file = mFileManager.getFile(FILENAME_TMDB_CONFIG);
            if (!file.exists()) {
                file.createNewFile();
            }

            writer = new FileWriter(file, false);
            Gson gson = new Gson();
            gson.toJson(configuration, writer);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}