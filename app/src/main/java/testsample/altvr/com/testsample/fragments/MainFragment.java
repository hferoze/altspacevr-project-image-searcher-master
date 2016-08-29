package testsample.altvr.com.testsample.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import testsample.altvr.com.testsample.R;
import testsample.altvr.com.testsample.util.DatabaseUtil;

/**
 * Created by hassan on 8/25/2016.
 */
public class MainFragment extends Fragment{

    private static final int PHOTOS_FRAGMENT_ID = 0;
    private static final int SAVED_PHOTOS_FRAGMENT_ID = 1;

    private static final int NUM_OF_TABS = 2;

    private static MainFragment mMainFragment = null;

    private ViewPager mPager;
    private TabLayout mTabLayout;

    private DatabaseUtil mDatabaseUtil;

    private View mView;

    public static synchronized MainFragment getInstance() {
        if (mMainFragment == null) mMainFragment = new MainFragment();
        return mMainFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseUtil = new DatabaseUtil(getActivity());
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.image_searcher_view_pager_fragment, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewPager(view);
    }

    private void initViewPager(View view){

        ImageSearcherTabsAdapter adapter = new ImageSearcherTabsAdapter(getActivity().getSupportFragmentManager());
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setAdapter(adapter);

        mTabLayout = (TabLayout) view.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mPager);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab t =  mTabLayout.getTabAt(i);
            switch (i) {
                case PHOTOS_FRAGMENT_ID:
                    if (t!=null) t.setIcon(R.drawable.home);
                    break;
                case SAVED_PHOTOS_FRAGMENT_ID:
                    if (t!=null) t.setIcon(R.drawable.heart);
                    break;
            }
        }

        int id = mPager.getCurrentItem();
        setTabIconColor(mTabLayout.getTabAt(id), R.color.colorAccent);
        setTabIconColor(mTabLayout.getTabAt((id+1)%NUM_OF_TABS), R.color.colorPrimaryLight);

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTabIconColor(tab, R.color.colorAccent);
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setTabIconColor(tab, R.color.colorPrimaryLight);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                if (position == SAVED_PHOTOS_FRAGMENT_ID) {
                    mDatabaseUtil.readAll();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (mPager.getCurrentItem() == PHOTOS_FRAGMENT_ID) {
                    if (state == ViewPager.SCROLL_STATE_DRAGGING)
                        PhotosFragment.getInstance().saveState(null);
                }else if (mPager.getCurrentItem() == SAVED_PHOTOS_FRAGMENT_ID) {
                    if (state == ViewPager.SCROLL_STATE_DRAGGING)
                        SavedPhotosFragment.getInstance().saveState(null);
                }
            }
        });
    }

    private void setTabIconColor(TabLayout.Tab tab, int color){
        Drawable icon = null;
        if (tab!=null) icon = tab.getIcon();
        if (icon != null) icon.setTint(ContextCompat.getColor(getContext(), color));
    }

    private class ImageSearcherTabsAdapter extends FragmentPagerAdapter {

        public ImageSearcherTabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_OF_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        @Override
        public Fragment getItem(int position) {
           switch(position) {
               case PHOTOS_FRAGMENT_ID:
                   return PhotosFragment.getInstance();
               case SAVED_PHOTOS_FRAGMENT_ID:
                   return SavedPhotosFragment.getInstance();
               default:
                   return PhotosFragment.getInstance();
           }
        }
    }

}
