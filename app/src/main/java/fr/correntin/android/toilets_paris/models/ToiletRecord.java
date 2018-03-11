package fr.correntin.android.toilets_paris.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Corentin on 08/03/2018.
 */

public class ToiletRecord {

    @SerializedName("recordid")
    private String mId;

    @SerializedName("fields")
    private Toilet mToiletDetails;

    @SerializedName("record_timestamp")
    private String mTimestamp;

    public Toilet getToiletDetails() {
        return mToiletDetails;
    }

    @Override
    public String toString() {
        return "ToiletRecord{" +
                "mId='" + mId + '\'' +
                ", mToiletDetails=" + mToiletDetails +
                ", mTimestamp='" + mTimestamp + '\'' +
                '}';
    }
}
