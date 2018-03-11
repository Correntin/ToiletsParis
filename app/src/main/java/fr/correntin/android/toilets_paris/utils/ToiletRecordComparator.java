package fr.correntin.android.toilets_paris.utils;

import java.util.Comparator;

import fr.correntin.android.toilets_paris.models.ToiletRecord;

/**
 * Created by Corentin on 11/03/2018.
 */

public class ToiletRecordComparator implements Comparator<ToiletRecord> {

    @Override
    public int compare(ToiletRecord toiletRecord1, ToiletRecord toiletRecord2) {

        if (toiletRecord1 != null
                && toiletRecord1.getToiletDetails() != null
                && toiletRecord2 != null
                && toiletRecord2.getToiletDetails() != null) {
            return toiletRecord1.getToiletDetails().getDistrict()
                    .compareTo(toiletRecord2.getToiletDetails().getDistrict());
        }
        return 0;
    }
}
