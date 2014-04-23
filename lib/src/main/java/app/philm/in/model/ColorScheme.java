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
}
