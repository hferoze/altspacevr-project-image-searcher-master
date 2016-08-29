package testsample.altvr.com.testsample.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import testsample.altvr.com.testsample.R;
import testsample.altvr.com.testsample.adapter.SavedPhotosItemsListAdapter;
import testsample.altvr.com.testsample.events.ApiErrorEvent;
import testsample.altvr.com.testsample.events.PhotosEvent;
import testsample.altvr.com.testsample.events.SavedPhotosEvent;
import testsample.altvr.com.testsample.util.DatabaseUtil;
import testsample.altvr.com.testsample.util.ItemListener;
import testsample.altvr.com.testsample.util.LogUtil;
import testsample.altvr.com.testsample.util.ParcelableArray;
import testsample.altvr.com.testsample.util.Utils;
import testsample.altvr.com.testsample.vo.PhotoVo;

/**
 * Created by hassan on 8/25/2016.
 */
public class SavedPhotosFragment extends Fragment {

    private static SavedPhotosFragment mSavedPhotosFragment = null;

    private LogUtil log = new LogUtil(SavedPhotosFragment.class);

    private GridLayoutManager mGridLayoutManager;
    private RecyclerView mSavedItemsListRecyclerView;
    private SavedPhotosItemsListAdapter mListAdapter;

    private ArrayList<PhotoVo> mItemsData = new ArrayList<>();

    private DatabaseUtil mDatabaseUtil;

    int mLastSavedPosition = -1, mLastSavedPositionOffset = -1;

    public static synchronized SavedPhotosFragment getInstance(){
        if (mSavedPhotosFragment == null) mSavedPhotosFragment = new SavedPhotosFragment();
        return mSavedPhotosFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseUtil = new DatabaseUtil(getActivity());
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_searcher_fragment_saved_photos, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        loadSavedStates(savedInstanceState);
        initViews(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (mDatabaseUtil==null)mDatabaseUtil = new DatabaseUtil(getActivity());
        mDatabaseUtil.readAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mItemsData.clear();
        mListAdapter = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    @Subscribe
    public void onEvent(PhotosEvent event){
        mListAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onEvent(SavedPhotosEvent event) {
        if (event.data.size()<1)
            log.e("No results found");

        mItemsData.clear();
        mItemsData.addAll(event.data);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mListAdapter.notifyDataSetChanged();
            }
        }, 350); //wait a little for animation to finish
    }

    @Subscribe
    public void onEvent(ApiErrorEvent event) {
        log.e(event.errorDescription);
        Toast.makeText(getContext(), event.errorDescription, Toast.LENGTH_SHORT).show();
    }

    /**
     * Loads app info that was saved in case of any change in life cycle or phone configuration
     * @param savedInstanceState Saved app info
     */
    private void loadSavedStates(Bundle savedInstanceState){
        if (savedInstanceState!=null){
            if(savedInstanceState.containsKey(getString(R.string.main_fragment_last_pos_key))
                    && savedInstanceState.containsKey(getString(R.string.main_fragment_last_offset_key))) {
                mLastSavedPosition = savedInstanceState.getInt(getString(R.string.main_fragment_last_pos_key));
                mLastSavedPositionOffset = savedInstanceState.getInt(getString(R.string.main_fragment_last_offset_key));
            }
        }
    }

    /**
     * Save app info in case of any change in life cycle
     * @param outState app info to be saved
     */
    public void saveState(Bundle outState){
        mLastSavedPosition = mGridLayoutManager.findFirstVisibleItemPosition();
        View v = mGridLayoutManager.getChildAt(0);
        mLastSavedPositionOffset = (v == null) ? 0 : (v.getTop() - mGridLayoutManager.getPaddingTop());

        if (outState!=null) {
            outState.putInt(getString(R.string.saved_photos_fragment_last_pos_key), mLastSavedPosition);
            outState.putInt(getString(R.string.saved_photos_fragment_last_offset_key), mLastSavedPositionOffset);
        }
    }

    private void initViews(View view) {
        setupRecyclerView(view);
        EventBus.getDefault().register(this);
    }

    private void setupRecyclerView(View view){
        mSavedItemsListRecyclerView = (RecyclerView) view.findViewById(R.id.saved_photos_list_recycler_view);
        mSavedItemsListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        setupItemsList();
    }

    private void setupItemsList() {
        mGridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mSavedItemsListRecyclerView.setLayoutManager(mGridLayoutManager);
        mSavedItemsListRecyclerView.setHasFixedSize(true);
        mListAdapter = new SavedPhotosItemsListAdapter(mItemsData, new ItemClickedListener(), getContext());
        mSavedItemsListRecyclerView.setAdapter(mListAdapter);
        mGridLayoutManager.scrollToPositionWithOffset(mLastSavedPosition != -1 ? mLastSavedPosition : 0, mLastSavedPositionOffset);
    }

    private void launchImageViewer(ArrayList<PhotoVo> itemsData, int position) {
        Fragment fragment = ImageViewerFragment.getInstance();

        fragment.setEnterTransition(new Slide(Gravity.RIGHT).setDuration(getResources().getInteger(R.integer.animation_delay)));
        fragment.setReturnTransition(new Slide(Gravity.RIGHT).setDuration(getResources().getInteger(R.integer.animation_delay)));

        Bundle bundle = new Bundle();
        ParcelableArray pA = new ParcelableArray();
        pA.setDataArray(itemsData);
        pA.setStartPosition(position);
        bundle.putParcelable(getString(R.string.data_parcel_key), pA);

        fragment.setArguments(bundle);

        getFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, fragment)
                .addToBackStack(getString(R.string.main_list_fragment_tag))
                .commit();
    }

    private class ItemClickedListener implements ItemListener {
        @Override
        public boolean itemClicked(RecyclerView.ViewHolder holder, int position, int resId) {
            boolean result = false;
            switch (resId){
                case R.id.save_photo_image_view:
                    log.d(mItemsData.get(position).id + " exists... deleting");
                    mDatabaseUtil.delete(mItemsData.get(position));
                    mDatabaseUtil.readAll();
                    result = false;
                    break;
                case R.id.share_photo_image_view:
                    result = Utils.shareImage(getContext(), getActivity(), mItemsData.get(position).webformatURL);
                    break;
                case R.id.item_image_view:
                    launchImageViewer(mItemsData, position);
                    break;
            }
            return result;
        }
    }
}
