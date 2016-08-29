package testsample.altvr.com.testsample.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import testsample.altvr.com.testsample.R;
import testsample.altvr.com.testsample.adapter.ImageViewerAdapater;
import testsample.altvr.com.testsample.util.ParcelableArray;
import testsample.altvr.com.testsample.vo.PhotoVo;

/**
 * Created by hassan on 8/28/2016.
 */
public class ImageViewerFragment extends Fragment{

    private static ImageViewerFragment mImageViewerFragment = null;

    private ArrayList<PhotoVo> mItemsData = new ArrayList<>();

    private int mStartPosition = 0;

    private Context mContext;

    private View mView;

    ViewPager viewPager;
    
    public static synchronized ImageViewerFragment getInstance(){
        if (mImageViewerFragment == null) mImageViewerFragment = new ImageViewerFragment();
        return mImageViewerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mView = inflater.inflate(R.layout.image_searcher_fragment_viewer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle extras = getArguments();
        if (extras != null) {
            ParcelableArray pA = extras.getParcelable(getString(R.string.data_parcel_key));
            mItemsData = pA.getDataArray();
            mStartPosition = pA.getStartPosition();
        }
        initViews(view);
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
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void initViews(final View view){
        view.findViewById(R.id.close_viewer_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });
        displayViewPager(view);
    }

    private void displayViewPager(View view ){
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        PagerAdapter adapter = new ImageViewerAdapater(mContext, mItemsData);

        viewPager.setAdapter(adapter);
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(mStartPosition, false);
            }
        });

    }
}
