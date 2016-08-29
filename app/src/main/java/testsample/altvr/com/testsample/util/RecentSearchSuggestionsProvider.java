package testsample.altvr.com.testsample.util;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by hassan on 8/26/2016.
 * Class to save recent search in db
 */
public class RecentSearchSuggestionsProvider extends SearchRecentSuggestionsProvider {

    public static final String AUTHORITY =
            RecentSearchSuggestionsProvider.class.getName();

    public static final int MODE = DATABASE_MODE_QUERIES;

    public RecentSearchSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
