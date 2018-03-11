package fr.correntin.android.toilets_paris;

import android.app.Application;

import fr.correntin.android.toilets_paris.utils.Preferences;

/**
 * Created by Corentin on 08/03/2018.
 */

public class ToiletParisApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Preferences.i().initialize(this);
    }
}
