package app.philm.in.model;

public class ColorScheme {

    public final int primaryAccent;
    public final int secondaryAccent;
    public final int tertiaryAccent;

    public final int primaryText;
    public final int secondaryText;

    public ColorScheme(int primaryAccent, int secondaryAccent, int tertiaryAccent,
            int primaryText, int secondaryText) {
        this.primaryAccent = primaryAccent;
        this.secondaryAccent = secondaryAccent;
        this.tertiaryAccent = tertiaryAccent;
        this.primaryText = primaryText;
        this.secondaryText = secondaryText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ColorScheme that = (ColorScheme) o;

        if (primaryAccent != that.primaryAccent) {
            return false;
        }
        if (primaryText != that.primaryText) {
            return false;
        }
        if (secondaryAccent != that.secondaryAccent) {
            return false;
        }
        if (secondaryText != that.secondaryText) {
            return false;
        }
        if (tertiaryAccent != that.tertiaryAccent) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = primaryAccent;
        result = 31 * result + secondaryAccent;
        result = 31 * result + tertiaryAccent;
        result = 31 * result + primaryText;
        result = 31 * result + secondaryText;
        return result;
    }
}
