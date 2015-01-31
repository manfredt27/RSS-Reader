package com.stbam.rssnewsreader.logic;

import android.content.SearchRecentSuggestionsProvider;
import android.provider.SearchRecentSuggestions;

/**
 * Created by Esteban on 11/15/2014.
 */

// basado en el codigo de http://developer.android.com/guide/topics/search/adding-recent-query-suggestions.html
// sirve como un proveedor de busquedas recientes
public class SugerenciasRecientes extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.stbam.rssnewsreader.logic.SugerenciasRecientes";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SugerenciasRecientes() {
        setupSuggestions(AUTHORITY, MODE);
    }



}