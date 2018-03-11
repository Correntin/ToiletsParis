package fr.correntin.android.toilets_paris.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fr.correntin.android.toilets_paris.models.Toilet;
import fr.correntin.android.toilets_paris.models.ToiletRecord;
import fr.correntin.android.toilets_paris.views.ToiletDetailsView;

/**
 * Created by Corentin on 10/03/2018.
 */

public class ToiletsListAdapter extends RecyclerView.Adapter
        implements ToiletDetailsView.ToiletDetailsViewListener {

    public interface ToiletListAdapterListener {
        void onContentChanged(List<ToiletRecord> toiletRecords);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ToiletDetailsView toiletDetailsView = new ToiletDetailsView(parent.getContext());
        toiletDetailsView.showHideButton(false);
        toiletDetailsView.setToiletDetailsViewListener(this);
        toiletDetailsView.setClickable(true);

        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toiletDetailsView.setLayoutParams(layoutParams);

        return new ViewHolder(toiletDetailsView);
    }

    private final List<ToiletRecord> mToiletRecords = new ArrayList<>();
    private ToiletListAdapterListener mToiletListAdapterListener;
    private boolean mIsOnlyFavorites = false;

    public void setToiletRecords(List<ToiletRecord> toiletRecords) {
        mToiletRecords.clear();
        mToiletRecords.addAll(toiletRecords);
        this.notifyDataSetChanged();

        if (mToiletListAdapterListener != null)
            mToiletListAdapterListener.onContentChanged(mToiletRecords);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ToiletDetailsView) holder.itemView).update(mToiletRecords.get(position).getToiletDetails());
    }

    @Override
    public int getItemCount() {
        return mToiletRecords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public void onToiletAddedToFavorites(Toilet toilet) {

    }

    @Override
    public void onToiletRemovedFromFavorites(Toilet toilet) {

        if (mIsOnlyFavorites) {
            int position = 0;

            for (ToiletRecord toiletRecord : mToiletRecords) {
                if (toiletRecord.getToiletDetails().equals(toilet))
                    break;
                position += 1;
            }

            mToiletRecords.remove(position);
            this.notifyItemRemoved(position);

            if (mToiletListAdapterListener != null)
                mToiletListAdapterListener.onContentChanged(mToiletRecords);
        }
    }

    public void setOnlyFavorites(boolean onlyFavorites) {
        mIsOnlyFavorites = onlyFavorites;
    }

    public void setToiletListAdapterListener(ToiletListAdapterListener toiletListAdapterListener) {
        this.mToiletListAdapterListener = toiletListAdapterListener;
    }
}
