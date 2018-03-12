package fr.correntin.android.toilets_paris.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.util.TypedValue;

import fr.correntin.android.toilets_paris.R;

/**
 * Created by Corentin on 09/03/2018.
 */

public class Utils {

    public static String formatStreet(String street) {

        if (street != null && street.length() > 0) {
            street = street.trim();
            String[] streetWords = street.trim().split(" ");
            StringBuilder streetFormatted = new StringBuilder();

            for (int i = 0; i < streetWords.length; i++) {
                String streetWord = streetWords[i];
                streetFormatted.append(streetWord.substring(0, 1).toUpperCase());

                if (streetWord.length() > 1)
                    streetFormatted.append(streetWord.substring(1).toLowerCase());

                if (i < streetWords.length - 1)
                    streetFormatted.append(" ");
            }

            return streetFormatted.toString();
        }

        return street;
    }

    public static boolean startNavigation(Context context, double latitude, double longitude) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=" + latitude + "," + longitude));

        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException exc) {
            return false;
        }
    }

    public static boolean startActivity(Context context, Intent intent, boolean clearTask) {

        if (clearTask)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException exc) {
            return false;
        }
    }

    public static boolean startActivity(Context context, Intent intent) {
        return startActivity(context, intent, false);
    }

    public static float convertDpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    private static int sSelectableItemBackground = -1;

    public static int getSelectableItemBackground(Context context) {

        if (sSelectableItemBackground == -1) {
            int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray typedArray = context.obtainStyledAttributes(attrs);
            sSelectableItemBackground = typedArray.getResourceId(0, 0);
            typedArray.recycle();
        }

        return sSelectableItemBackground;
    }

    public static void showMessageDialog(Context context, String title, String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(R.drawable.ic_drawer_info)
                .show();
    }
}
