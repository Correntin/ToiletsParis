package fr.correntin.android.toilets_paris.models;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

import fr.correntin.android.toilets_paris.utils.Utils;


/**
 * Created by Corentin on 08/03/2018.
 */

public class Toilet {

    @SerializedName("recordid")
    private String mRecordId;

    @SerializedName("objectid")
    private String mObjectId;

    @SerializedName("arrondissement")
    private String mDistrict;

    @SerializedName("nom_voie")
    private String mStreet;

    @SerializedName("gestionnaire")
    private String mOwner;

    @SerializedName("geom_x_y")
    private double[] mPositions;

    @SerializedName("x")
    private double mX;

    @SerializedName("y")
    private double mY;

    @SerializedName("horaires_ouverture")
    private String mOpeningHours;

    @SerializedName("identifiant")
    private String mId;

    public String getObjectId() {
        return mObjectId;
    }

    public String getDistrict() {
        return mDistrict;
    }

    public String getStreet() {

        if (mStreet != null)
            mStreet = Utils.formatStreet(mStreet);
        return mStreet;
    }

    public String getOwner() {
        return mOwner;
    }

    public double[] getPositions() {
        return mPositions;
    }

    public double getX() {
        return mX;
    }

    public double getY() {
        return mY;
    }

    public String getOpeningHours() {
        return mOpeningHours;
    }

    public String getId() {
        return mId;
    }

    @Override
    public String toString() {
        return "Toilet{" +
                "mRecordId=" + mRecordId + " " +
                "mObjectId='" + mObjectId + '\'' +
                ", district='" + mDistrict + '\'' +
                ", mStreet='" + mStreet + '\'' +
                ", mOwner='" + mOwner + '\'' +
                ", mPositions=" + Arrays.toString(mPositions) +
                ", mX=" + mX +
                ", mY=" + mY +
                ", mOpeningHours='" + mOpeningHours + '\'' +
                ", mId='" + mId + '\'' +
                '}';
    }
}
