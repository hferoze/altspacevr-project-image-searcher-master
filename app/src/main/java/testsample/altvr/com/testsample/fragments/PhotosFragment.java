package testsample.altvr.com.testsample.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import testsample.altvr.com.testsample.R;
import testsample.altvr.com.testsample.adapter.ItemsListAdapter;
import testsample.altvr.com.testsample.events.ApiErrorEvent;
import testsample.altvr.com.testsample.events.PhotosEvent;
import testsample.altvr.com.testsample.events.SavedPhotosEvent;
import testsample.altvr.com.testsample.service.ApiService;
import testsample.altvr.com.testsample.util.DatabaseUtil;
import testsample.altvr.com.testsample.util.ItemListener;
import testsample.altvr.com.testsample.util.LogUtil;
import testsample.altvr.com.testsample.util.ParcelableArray;
import testsample.altvr.com.testsample.util.RecentSearchSuggestionsProvider;
import testsample.altvr.com.testsample.util.Utils;
import testsample.altvr.com.testsample.vo.PhotoVo;

public class PhotosFragment extends Fragment{

    private static PhotosFragment mPhotosFragment = null;

    private LogUtil log = new LogUtil(PhotosFragment.class);

    private RecyclerView mItemsListRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private ApiService mService;

    private ArrayList<PhotoVo> mItemsData = new ArrayList<>();
    private ItemsListAdapter mListAdapter;
    private DatabaseUtil mDatabaseUtil;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private FloatingActionButton mSearchButton;
    private RelativeLayout mSearchLayout;
    private SearchView mSearchView;

    private PhotosEvent mApiResponse = null;

    private Context mContext;

    private View mView;

    //save data
    private int mLastSavedPosition = -1;
    private int mLastSavedPositionOffset = -1;
    private boolean mContentLoaded;
    private boolean mIsSearchTrayOpen;

    public static synchronized PhotosFragment getInstance() {
        if (mPhotosFragment == null) mPhotosFragment = new PhotosFragment();
        return mPhotosFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mService = new ApiService(getActivity());
        mDatabaseUtil = new DatabaseUtil(getActivity());
        mContext = getContext();
        setRetainInstance(true);
        setContentLoaded(false);
        setIsSearchTrayOpen(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_searcher_fragment_main, container, false);
        mView = view;
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        loadSavedStates(savedInstanceState);
        initViews(view);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mView.setFocusableInTouchMode(true);
        mView.requestFocus();
        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK){
                        //Over ride back keypress when search layout is visible.
                        if (mSearchLayout.getTranslationY() == 0) {
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    showSearchLayout(false);
                                }
                            }, 5);
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
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
    }

    @Subscribe
    public void onEvent(PhotosEvent event) {
        /**
         * YOUR CODE HERE
         *
         * This will be the event posted via the EventBus when a photo has been fetched for display.
         *
         * For part 1a you should update the data for this fragment (or notify the user no results
         * were found) and redraw the list.
         *
         * For part 2b you should update this to handle the case where the user has saved photos.
         */
        mApiResponse = event;
        updateData();
    }

    @Subscribe
    public void onEvent(SavedPhotosEvent event) {
        mListAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onEvent(ApiErrorEvent event) {
        /**
         * YOUR CODE HERE
         *
         * This will be the event posted via the EventBus when an API error has occured.
         *
         * For part 1a you should clear the fragment and notify the user of the error.
         */
        log.e(event.errorDescription);
        Toast.makeText(getContext(), event.errorDescription, Toast.LENGTH_SHORT).show();
        if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
        displayErrorText(event.errorDescription);
    }

    /**
     * Loads saved data in case of any change in life cycle or phone configuration
     * @param savedInstanceState Saved data
     */
    private void loadSavedStates(Bundle savedInstanceState){
        if (savedInstanceState!=null){
            if(savedInstanceState.containsKey(getString(R.string.main_fragment_last_pos_key))
                    && savedInstanceState.containsKey(getString(R.string.main_fragment_last_offset_key))) {
                mLastSavedPosition = savedInstanceState.getInt(getString(R.string.main_fragment_last_pos_key));
                mLastSavedPositionOffset = savedInstanceState.getInt(getString(R.string.main_fragment_last_offset_key));
            }
            if (savedInstanceState.containsKey(getString(R.string.main_fragment_content_loaded_key))){
                setContentLoaded(savedInstanceState.getBoolean(getString(R.string.main_fragment_content_loaded_key)));
            }
            if (savedInstanceState.containsKey(getString(R.string.main_fragment_is_search_tray_open_key))){
                setIsSearchTrayOpen(savedInstanceState.getBoolean(getString(R.string.main_fragment_is_search_tray_open_key)));
            }
        }
    }

    /**
     * Save data in case of any change in life cycle
     * @param outState Data to be saved
     */
    public void saveState(Bundle outState){
        mLastSavedPosition = mGridLayoutManager.findFirstVisibleItemPosition();
        View v = mGridLayoutManager.getChildAt(0);
        mLastSavedPositionOffset = (v == null) ? 0 : (v.getTop() - mGridLayoutManager.getPaddingTop());
        if (outState!=null) {
            outState.putInt(getString(R.string.main_fragment_last_pos_key), mLastSavedPosition);
            outState.putInt(getString(R.string.main_fragment_last_offset_key), mLastSavedPositionOffset);
            outState.putBoolean(getString(R.string.main_fragment_content_loaded_key), getContentLoaded());
            outState.putBoolean(getString(R.string.main_fragment_is_search_tray_open_key), getIsSearchTrayOpen());
        }
    }

    /**
     * Updates data after PhotoEvent
     */
    public void updateData(){
        if (mApiResponse!=null && mListAdapter!=null) {
            if (mApiResponse.data.size()<1)displayErrorText(getString(R.string.no_results_found));

            mItemsData.clear();
            mItemsData.addAll(mApiResponse.data);
            mListAdapter.notifyDataSetChanged();
            mGridLayoutManager.scrollToPositionWithOffset(mLastSavedPosition != -1 ? mLastSavedPosition : 0, mLastSavedPositionOffset);

            if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Initializes views after OnViewCreated
     * @param view this view
     */
    private void initViews(View view) {
        setupRecyclerView(view);
        setupRefreshLayout(view);
        setupSearchButton(view);
        setupSearchLayout(view);
        EventBus.getDefault().register(this);
    }

    private void setupRefreshLayout(View view){
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //gets the default photos on refresh
                serviceRequest(null);
            }
        });

        if(!getContentLoaded()){
            serviceRequest(null);
            setContentLoaded(true);
        }
    }

    private void setupRecyclerView(View view){
        mItemsListRecyclerView = (RecyclerView) view.findViewById(R.id.photos_list_recycler_view);
        mItemsListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        setupItemsList();
    }

    private void setupItemsList() {
        mGridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mItemsListRecyclerView.setLayoutManager(mGridLayoutManager);
        mItemsListRecyclerView.setHasFixedSize(true);
        mListAdapter = new ItemsListAdapter(mItemsData, new ItemClickedListener(), mContext);
        mItemsListRecyclerView.setAdapter(mListAdapter);
    }

    private void setupSearchButton(View view){
        mSearchButton = (FloatingActionButton) view.findViewById(R.id.search_btn_image_view);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchLayout(true);
            }
        });
    }

    private void setupSearchLayout(View view){
        mSearchLayout = (RelativeLayout) view.findViewById(R.id.search_relative_layout);

        if (!getIsSearchTrayOpen())mSearchLayout.setTranslationY(Utils.getScreenHeight(mContext));
        else mSearchLayout.setTranslationY(0);

        final FrameLayout searchFrame = (FrameLayout) mSearchLayout.findViewById(R.id.search_box);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) mSearchLayout.findViewById(R.id.search_view);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        mSearchView.setImeOptions(mSearchView.getImeOptions()  | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }
            @Override
            public boolean onSuggestionClick(int position) {
                //Get suggestion and set it as search query
                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(
                        position);
                String suggestion = cursor.getString(cursor
                        .getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));

                mSearchView.setQuery(suggestion, true);
                return true;
            }
        });

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    mSearchView.setIconified(false);
                    ((TextView) searchFrame.findViewById(R.id.search_hint_text_view)).setText(" ");
                }
                if (!hasFocus){
                    mSearchView.setIconified(true);
                   ((TextView) searchFrame.findViewById(R.id.search_hint_text_view)).setText(getString(R.string.search_hint));
                }
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if( ! mSearchView.isIconified()) {
                    mSearchView.setIconified(true);
                }
                mSearchView.setIconified(true);
                mSearchView.clearFocus();
                serviceRequest(query); //launch request for searched string
                showSearchLayout(false);
                saveSearch(query); //save search string in db
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        searchFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.setQuery("", true);
                mSearchView.setFocusable(true);
                mSearchView.setIconified(false);
            }
        });

        view.findViewById(R.id.close_search_tray_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.clearFocus();
                showSearchLayout(false);
            }
        });

        mSearchLayout.findViewById(R.id.clear_search_history_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSearchHistory();
            }
        });
    }

    /**
     * Brings search layout in user's view or out based on show
     * @param show true or false value to decide whether search layout should be visible to user or not
     */
    private void showSearchLayout(boolean show){
        if (show){
            if(mSearchButton.getVisibility() == View.VISIBLE) mSearchButton.hide();
            if(mSearchButton.getVisibility() == View.VISIBLE)mSearchLayout.animate().translationY(0);
            setIsSearchTrayOpen(true);
        }else{
            if(mSearchButton.getVisibility() == View.GONE) mSearchButton.show();
            mSearchLayout.animate().translationY(Utils.getScreenHeight(getContext()));
            setIsSearchTrayOpen(false);
        }
    }

    /**
     * Saves search term in database
     * @param query search string
     */
    private void saveSearch(String query){
        SearchRecentSuggestions suggestions =
                new SearchRecentSuggestions(getActivity(),
                        RecentSearchSuggestionsProvider.AUTHORITY,
                        RecentSearchSuggestionsProvider.MODE);
        suggestions.saveRecentQuery(query, null);
    }

    /**
     * Clears suggestion database
     */
    private void clearSearchHistory(){
        new SearchRecentSuggestions(getActivity(),
                RecentSearchSuggestionsProvider.AUTHORITY,
                RecentSearchSuggestionsProvider.MODE).clearHistory();
    }

    /**
     * Launches request to get photos data
     * @param query search string, can be null
     */
    private void serviceRequest(String query){
        saveState(null);
        displayErrorText(null);
        mItemsData.clear();
        mListAdapter.notifyDataSetChanged();
        if(mSwipeRefreshLayout!=null){
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }
        mService.getPhotos(query);
    }

    /**
     * Used to set app state, whether data has been loaded or not
     * @param loaded true or false
     */
    private void setContentLoaded(boolean loaded){
        mContentLoaded = loaded;
    }
    private boolean getContentLoaded(){
        return mContentLoaded;
    }

    private void setIsSearchTrayOpen(boolean open){
        mIsSearchTrayOpen = open;
    }
    private boolean getIsSearchTrayOpen(){
        return mIsSearchTrayOpen;
    }

    /**
     * Shows error to the user
     * @param error error description
     */
    private void displayErrorText(String error){
        if(error!=null){
            mView.findViewById(R.id.error_layout).setVisibility(View.VISIBLE);
            ((TextView) mView.findViewById(R.id.error_layout).findViewById(R.id.error_text_view)).setText(error);
            if (!error.equals(getString(R.string.no_results_found)))
                ((ImageView) mView.findViewById(R.id.error_layout).findViewById(R.id.error_image_view)).setImageResource(R.drawable.no_data);
            else
                ((ImageView) mView.findViewById(R.id.error_layout).findViewById(R.id.error_image_view)).setImageResource(R.drawable.no_search_result);
        }else{
            mView.findViewById(R.id.error_layout).setVisibility(View.INVISIBLE);
        }
    }

    private boolean savePhotoData(int position) {
        if (!mDatabaseUtil.checkRecordExists(mItemsData.get(position).id)) {
            log.d("Adding to db " + mItemsData.get(position).id);
            mDatabaseUtil.create(mItemsData.get(position));
            return true;
        }
        log.d(mItemsData.get(position).id + " exists... deleting");
        mDatabaseUtil.delete(mItemsData.get(position));
        return false;
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
                    result = savePhotoData(position);
                    break;
                case R.id.share_photo_image_view:
                    result = Utils.shareImage(mContext, getActivity(), mItemsData.get(position).webformatURL);
                    break;
                case R.id.item_image_view:
                    launchImageViewer(mItemsData, position);
                    break;
            }

            return result;
        }
    }
}
