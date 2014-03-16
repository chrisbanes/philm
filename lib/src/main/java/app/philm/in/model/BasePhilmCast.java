package app.philm.in.model;

public abstract class BasePhilmCast implements PhilmModel {

    Integer tmdbId;
    Integer order;
    String name;
    String pictureUrl;

    int pictureType;

    public Integer getTmdbId() {
        return tmdbId;
    }

    public Integer getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public int getPictureType() {
        return pictureType;
    }
}
