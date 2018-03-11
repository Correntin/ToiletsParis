package fr.correntin.android.toilets_paris.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import fr.correntin.android.toilets_paris.BuildConfig;

/**
 * Created by Corentin on 10/03/2018.
 */

public class Preferences {

    private static final String PREFERENCES_FILENAME = BuildConfig.APPLICATION_ID + ".preferences";
    private static final String FAVORITE_TOILET_LIST_KEY = "favoriteToiletList";

    private static final Preferences sPreferences = new Preferences();

    private Preferences() {
    }

    private SharedPreferences mSharedPreferences;
    private Set<String> mFavoriteToiletListCache = null;

    public static Preferences i() {
        return sPreferences;
    }

    public void initialize(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    }

    public void addFavoriteToilet(String id) {

        mFavoriteToiletListCache = null;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Set<String> favoriteToiletListSp = mSharedPreferences.getStringSet(FAVORITE_TOILET_LIST_KEY, new HashSet<String>());
        Set<String> favoriteToiletList = new HashSet<>();
        favoriteToiletList.addAll(favoriteToiletListSp);

        favoriteToiletList.add(id);

        editor.putStringSet(FAVORITE_TOILET_LIST_KEY, favoriteToiletList);
        editor.commit();
    }

    public void removeFavoriteToilet(String id) {

        mFavoriteToiletListCache = null;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Set<String> favoriteToiletListSp = mSharedPreferences.getStringSet(FAVORITE_TOILET_LIST_KEY, new HashSet<String>());
        Set<String> favoriteToiletList = new HashSet<>();
        favoriteToiletList.addAll(favoriteToiletListSp);

        favoriteToiletList.remove(id);

        editor.putStringSet(FAVORITE_TOILET_LIST_KEY, favoriteToiletList);
        editor.commit();
    }

    public boolean isFavoriteToilet(String id) {

        if (mFavoriteToiletListCache == null)
            mFavoriteToiletListCache = mSharedPreferences.getStringSet(FAVORITE_TOILET_LIST_KEY, new HashSet<String>());

        return mFavoriteToiletListCache.contains(id);
    }
}
