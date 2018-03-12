package fr.correntin.android.toilets_paris.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.geronimostudios.coffeescene.SceneCreator;
import com.geronimostudios.coffeescene.SceneManager;
import com.geronimostudios.coffeescene.annotations.Scene;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.correntin.android.toilets_paris.R;
import fr.correntin.android.toilets_paris.ToiletParisActivity;
import fr.correntin.android.toilets_paris.api.ApiManager;
import fr.correntin.android.toilets_paris.map.MapsActivity;
import fr.correntin.android.toilets_paris.models.ToiletRecord;
import fr.correntin.android.toilets_paris.models.ToiletResponse;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * This activity is used to list toilets (ful list or just favorites)<br/>
 * If it's favorite toilets, when a toilete is deleted from favorites, the item is deleted too<br/>
 * <p>
 * Created by Corentin on 10/03/2018.
 */

public class ToiletsListActivity extends ToiletParisActivity
        implements ToiletsListAdapter.ToiletListAdapterListener {

    private static String SHOW_FAVORITES_ONLY = "showFavoritesOnly";

    public static void start(Context context, boolean showFavoritesOnly) {

        Intent intent = new Intent(context, ToiletsListActivity.class);
        intent.putExtra(SHOW_FAVORITES_ONLY, showFavoritesOnly);
        context.startActivity(intent);
    }

    @BindView(R.id.activity_list_RecyclerView)
    RecyclerView mRecyclerView;

    private ToiletsListAdapter mToiletsListAdapter;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Activity cycle life
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

        SceneManager.create(SceneCreator.with(this)
                .add(Scene.MAIN_CONTENT, R.id.activity_list_RecyclerView)
                .add(Scene.LOADER, R.id.activity_list_ProgressBar)
                .add(Scene.PLACEHOLDER, R.id.activity_list_empty_Layout)
                .main(Scene.LOADER));

        mToiletsListAdapter = new ToiletsListAdapter();
        mToiletsListAdapter.setToiletListAdapterListener(this);

        mRecyclerView.setAdapter(mToiletsListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        this.init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        this.init();
    }

    private void init() {

        Intent intent = this.getIntent();
        boolean showFavoritesOnly = false;

        if (intent != null)
            showFavoritesOnly = intent.getBooleanExtra(SHOW_FAVORITES_ONLY, false);

        mToiletsListAdapter.setOnlyFavorites(showFavoritesOnly);
        int titleId = showFavoritesOnly ? R.string.activity_list_title_favorites : R.string.activity_list_title_all;
        getSupportActionBar().setTitle(titleId);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ApiManager.getToilets(this, false, showFavoritesOnly)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onToiletListResponse());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // View events
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @OnClick(R.id.activity_list_add_favorites_Button)
    public void onClickOnAddFavoritesButton() {
        MapsActivity.start(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Network events
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private Subscriber<ToiletResponse> onToiletListResponse() {
        return new Subscriber<ToiletResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(ToiletResponse toiletResponse) {
                mToiletsListAdapter.setToiletRecords(toiletResponse.getToiletRecords());
                SceneManager.scene(ToiletsListActivity.this, toiletResponse.getToiletRecords().size() > 0 ?
                        Scene.MAIN_CONTENT : Scene.PLACEHOLDER);
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ToiletsListAdapter.ToiletListAdapterListener interface
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onContentChanged(List<ToiletRecord> toiletRecords) {

        if (toiletRecords.size() == 0)
            SceneManager.scene(this, Scene.PLACEHOLDER);
    }
}
