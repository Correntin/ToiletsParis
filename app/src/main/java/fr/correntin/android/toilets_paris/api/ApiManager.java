package fr.correntin.android.toilets_paris.api;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import fr.correntin.android.toilets_paris.models.ToiletRecord;
import fr.correntin.android.toilets_paris.models.ToiletResponse;
import fr.correntin.android.toilets_paris.utils.NoNetworkAndNoCacheDataException;
import fr.correntin.android.toilets_paris.utils.Preferences;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by Corentin on 08/03/2018.
 */

public class ApiManager {

    private static final String DOWNLOAD_TOILET_LIST_URL = "https://data.ratp.fr/api/records/1.0/search/?dataset=sanisettesparis2011&rows=1000";
    private static final String DISK_CACHE_FILE_NAME = "bodyToiletsList.json";

    private static void saveInDiskCache(Context context, String body) {
        File file = new File(context.getFilesDir(), DISK_CACHE_FILE_NAME);

        if (file.exists() == true)
            file.delete();

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(body.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getSavedBodyInDiskCache(Context context) {

        File file = new File(context.getFilesDir(), DISK_CACHE_FILE_NAME);

        if (file.exists() == false)
            return null;

        StringBuilder body = new StringBuilder();
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;

        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);

            String sCurrentLine;
            while ((sCurrentLine = bufferedReader.readLine()) != null) {
                body.append(sCurrentLine);
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();

                if (fileReader != null)
                    fileReader.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

        if (body.length() > 0)
            return body.toString();
        return null;
    }

    private static ToiletResponse parseBody(String body) {
        Gson gson = new Gson();
        ToiletResponse toiletResponse = gson.fromJson(body, ToiletResponse.class);
        toiletResponse.sortToiletRecords();

        return toiletResponse;
    }

    /**
     * Get toilets<br/>
     *
     * @param context
     * @param forceUpdate
     * @param onlyFavorites
     * @return Cache list if network call already called
     */
    public static Observable<ToiletResponse> getToilets(final Context context, final boolean forceUpdate, final boolean onlyFavorites) {
        return Observable.create(new Observable.OnSubscribe<ToiletResponse>() {

            @Override
            public void call(Subscriber<? super ToiletResponse> subscriber) {

                String body = null;
                ToiletResponse toiletResponse = null;

                if (forceUpdate == false)
                    body = getSavedBodyInDiskCache(context);

                if (body != null) {
                    toiletResponse = parseBody(body);

                    if (onlyFavorites)
                        extractFavorites(toiletResponse);

                    subscriber.onNext(toiletResponse);
                } else {

                    final Request request = new Request.Builder()
                            .url(DOWNLOAD_TOILET_LIST_URL)
                            .build();

                    try {
                        OkHttpClient okHttpClient = new OkHttpClient();
                        Response response = okHttpClient.newCall(request).execute();

                        body = response.body().string();
                        saveInDiskCache(context, body);
                        toiletResponse = parseBody(body);

                        if (onlyFavorites)
                            extractFavorites(toiletResponse);

                        subscriber.onNext(toiletResponse);
                    } catch (IOException e) {
                        e.printStackTrace();

                        if (getSavedBodyInDiskCache(context) == null)
                            subscriber.onError(new NoNetworkAndNoCacheDataException());
                        else
                            subscriber.onError(e);
                    }
                }

                subscriber.onCompleted();
            }
        });
    }

    /**
     * Get toilets<br/>
     *
     * @param context
     * @param forceUpdate
     * @return Cache list if network call already called
     */
    public static Observable<ToiletResponse> getToilets(final Context context, final boolean forceUpdate) {
        return getToilets(context, forceUpdate, false);
    }

    private static void extractFavorites(ToiletResponse toiletResponse) {

        for (Iterator<ToiletRecord> toiletRecordIterator = toiletResponse.getToiletRecords().iterator(); toiletRecordIterator.hasNext(); ) {

            ToiletRecord toiletRecord = toiletRecordIterator.next();

            if (Preferences.i().isFavoriteToilet(toiletRecord.getToiletDetails().getId()) == false)
                toiletRecordIterator.remove();
        }
    }
}
