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
