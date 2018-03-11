package fr.correntin.android.toilets_paris.models;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

import fr.correntin.android.toilets_paris.utils.ToiletRecordComparator;

/**
 * Created by Corentin on 08/03/2018.
 */

public class ToiletResponse {

    @SerializedName("records")
    private List<ToiletRecord> mToiletRecords;

    public List<ToiletRecord> getToiletRecords() {
        return mToiletRecords;
    }

    public void sortToiletRecords() {
        Collections.sort(mToiletRecords, new ToiletRecordComparator());
    }

    @Override
    public String toString() {
        return "ToiletResponse{" +
                "mToiletRecords=" + mToiletRecords +
                '}';
    }
}
