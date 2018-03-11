package fr.correntin.android.toilets_paris.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.correntin.android.toilets_paris.R;
import fr.correntin.android.toilets_paris.map.MapsActivity;
import fr.correntin.android.toilets_paris.models.Toilet;
import fr.correntin.android.toilets_paris.utils.Preferences;
import fr.correntin.android.toilets_paris.utils.Utils;

/**
 * Created by Corentin on 09/03/2018.
 */

public class ToiletDetailsView extends CardView
        implements View.OnClickListener {

    public interface ToiletDetailsViewListener {
        void onToiletAddedToFavorites(Toilet toilet);

        void onToiletRemovedFromFavorites(Toilet toilet);
    }

    private final static int VISIBILITY_ANIMATION_DURATION = 400;

    public ToiletDetailsView(@NonNull Context context) {
        super(context);
        this.init();
    }

    public ToiletDetailsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public ToiletDetailsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @BindView(R.id.view_toilet_details_street_TextView)
    TextView mStreetTextView;

    @BindView(R.id.view_toilet_details_district_TextView)
    TextView mDistrictTextView;

    @BindView(R.id.view_toilet_details_opening_hours_TextView)
    TextView mOpeningHoursTextView;

    @BindView(R.id.view_toilet_details_owner_TextView)
    TextView mOwnerTextView;

    @BindView(R.id.view_toilet_details_favorite_Button)
    ImageButton mFavoriteButton;

    @BindView(R.id.view_toilet_details_hide_ImageView)
    ImageView mHideImageView;

    @BindView(R.id.view_toilet_details_share_Button)
    ImageButton mShareButton;

    private Toilet mToilet;
    private ToiletDetailsViewListener mToiletDetailsViewListener;

    private void init() {

        LayoutInflater.from(this.getContext()).inflate(R.layout.view_toilet_details, this, true);
        ButterKnife.bind(this);

        mStreetTextView.setTypeface(null, Typeface.BOLD);
        mOpeningHoursTextView.setTypeface(null, Typeface.BOLD);
        mOwnerTextView.setTypeface(null, Typeface.ITALIC);

        setRadius(Utils.convertDpToPx(getContext(), 12));
        setCardElevation(Utils.convertDpToPx(getContext(), 6));
        setUseCompatPadding(true);
        setOnClickListener(this);
        setClickable(false);
    }

    public void update(Toilet toilet) {

        mToilet = toilet;
        mStreetTextView.setText(toilet.getStreet());
        mDistrictTextView.setText(getContext().getResources().getQuantityString(R.plurals.toilet_details_view_district, Integer.valueOf(toilet.getDistrict()).intValue(), toilet.getDistrict()));
        mOpeningHoursTextView.setText(toilet.getOpeningHours());
        mOwnerTextView.setText(toilet.getOwner());

        if (Preferences.i().isFavoriteToilet(toilet.getId()))
            mFavoriteButton.setImageResource(R.drawable.ic_selected_star);
        else
            mFavoriteButton.setImageResource(R.drawable.ic_unselected_star);
    }

    @OnClick(R.id.view_toilet_details_go_Button)
    public void onClickOnGoButton() {
        Utils.startNavigation(this.getContext(), mToilet.getPositions()[0], mToilet.getPositions()[1]);
    }

    @OnClick(R.id.view_toilet_details_favorite_Button)
    public void onClickOnFavoriteButton() {

        int newIcon, toastMessageId;

        if (Preferences.i().isFavoriteToilet(mToilet.getId())) {
            Preferences.i().removeFavoriteToilet(mToilet.getId());
            newIcon = R.drawable.ic_unselected_star;
            toastMessageId = R.string.toilet_details_view_removed_to_favorite;

            if (mToiletDetailsViewListener != null)
                mToiletDetailsViewListener.onToiletRemovedFromFavorites(mToilet);
        } else {
            Preferences.i().addFavoriteToilet(mToilet.getId());
            newIcon = R.drawable.ic_selected_star;
            toastMessageId = R.string.toilet_details_view_added_to_favorite;

            if (mToiletDetailsViewListener != null)
                mToiletDetailsViewListener.onToiletAddedToFavorites(mToilet);
        }

        mFavoriteButton.setImageResource(newIcon);
        Toast.makeText(getContext(), getContext().getString(toastMessageId), Toast.LENGTH_SHORT).show();
    }

    public void showHideButton(boolean showHideButton) {
        mHideImageView.setVisibility(showHideButton ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.view_toilet_details_share_Button)
    public void onClickOnShareButton() {
        String shareBody = getContext().getString(R.string.share_intent_message, mStreetTextView.getText(),
                mDistrictTextView.getText(), mOpeningHoursTextView.getText(), String.valueOf(mToilet.getPositions()[0]), String.valueOf(mToilet.getPositions()[1]));
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getContext().getString(R.string.share_intent_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        Utils.startActivity(this.getContext(), Intent.createChooser(sharingIntent, getResources().getString(R.string.share_intent_title)));
    }

    @Override
    public void setVisibility(final int visibility) {

        if (visibility == VISIBLE) {

            super.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    0, 0, this.getHeight(), 0);
            animate.setDuration(VISIBILITY_ANIMATION_DURATION);
            animate.setFillAfter(true);
            this.startAnimation(animate);
        } else {
            TranslateAnimation animate = new TranslateAnimation(
                    0, 0, 0, this.getHeight());
            animate.setDuration(VISIBILITY_ANIMATION_DURATION);
            animate.setFillAfter(true);
            animate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ToiletDetailsView.super.setVisibility(visibility);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            this.startAnimation(animate);
        }
    }

    public void setToiletDetailsViewListener(ToiletDetailsViewListener toiletDetailsViewListener) {
        this.mToiletDetailsViewListener = toiletDetailsViewListener;
    }

    @Override
    public void onClick(View v) {

        if (isClickable())
            MapsActivity.start(this.getContext(), mToilet.getId());
    }

    @Override
    public void setClickable(boolean clickable) {

        if (clickable)
            setForeground(ContextCompat.getDrawable(this.getContext(), Utils.getSelectableItemBackground(this.getContext())));

        super.setFocusable(clickable);
        super.setClickable(clickable);
    }

    @OnClick(R.id.view_toilet_details_hide_ImageView)
    public void onClickOnHideToiletDetailsImageView() {
        this.setVisibility(View.GONE);
    }

}
