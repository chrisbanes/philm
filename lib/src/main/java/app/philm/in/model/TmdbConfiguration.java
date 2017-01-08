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

package app.philm.in.model;


import com.google.common.base.Preconditions;
import com.uwetrottmann.tmdb.entities.Configuration;

import java.util.List;

import app.philm.in.util.TextUtils;

public class TmdbConfiguration {

    long lastFetchTime;
    String imagesBaseUrl;
    int[] imagesBackdropSizes;
    int[] imagesPosterSizes;
    int[] imagesProfileSizes;

    public long getLastFetchTime() {
        return lastFetchTime;
    }

    public String getImagesBaseUrl() {
        return imagesBaseUrl;
    }

    public int[] getImagesBackdropSizes() {
        return imagesBackdropSizes;
    }

    public int[] getImagesPosterSizes() {
        return imagesPosterSizes;
    }

    public int[] getImagesProfileSizes() {
        return imagesProfileSizes;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(imagesBaseUrl)
                && imagesBackdropSizes != null
                && imagesPosterSizes != null
                && imagesProfileSizes != null;
    }

    public void setFromTmdb(Configuration configuration) {
        Preconditions.checkNotNull(configuration, "configuration cannot be null");

        lastFetchTime = System.currentTimeMillis();
        imagesBaseUrl = configuration.images.base_url;
        imagesBackdropSizes = convertTmdbImageSizes(configuration.images.backdrop_sizes);
        imagesPosterSizes = convertTmdbImageSizes(configuration.images.poster_sizes);
        imagesProfileSizes = convertTmdbImageSizes(configuration.images.profile_sizes);
    }


    private static int[] convertTmdbImageSizes(List<String> stringSizes) {
        int[] intSizes = new int[stringSizes.size() - 1];
        for (int i = 0; i < intSizes.length; i++) {
            String size = stringSizes.get(i);
            if (size.charAt(0) == 'w') {
                intSizes[i] = Integer.parseInt(size.substring(1));
            }
        }
        return intSizes;
    }
}
