package testsample.altvr.com.testsample.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.Gravity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import testsample.altvr.com.testsample.R;
import testsample.altvr.com.testsample.util.DatabaseUtil;
import testsample.altvr.com.testsample.util.ItemListener;
import testsample.altvr.com.testsample.util.LogUtil;
import testsample.altvr.com.testsample.util.ParcelableArray;
import testsample.altvr.com.testsample.util.Utils;
import testsample.altvr.com.testsample.vo.PhotoVo;

/**
 * Created by hassan on 8/29/2016.
 */
public abstract class BaseFragment extends Fragment {

    private static LogUtil log = new LogUtil(BaseFragment.class);

    protected ArrayList<PhotoVo> itemsData = new ArrayList<>();
    protected DatabaseUtil databaseUtil;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseUtil = new DatabaseUtil(getActivity());
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (databaseUtil==null)databaseUtil = new DatabaseUtil(getActivity());
        databaseUtil.readAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        itemsData.clear();
    }

    protected void launchImageViewer(ArrayList<PhotoVo> itemsData, int position) {
        Fragment fragment = ImageViewerFragment.getInstance();

        fragment.setEnterTransition(new Slide(Gravity.END).setDuration(getResources().getInteger(R.integer.animation_delay)));
        fragment.setReturnTransition(new Slide(Gravity.END).setDuration(getResources().getInteger(R.integer.animation_delay)));

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

    protected boolean updatePhotosData(int position){
        if (!databaseUtil.checkRecordExists(itemsData.get(position).id)) {
            log.d("Adding to db " + itemsData.get(position).id);
            databaseUtil.create(itemsData.get(position));
            return true;
        }
        log.d(itemsData.get(position).id + " exists... deleting");
        databaseUtil.delete(itemsData.get(position));
        databaseUtil.readAll();
        return false;
    }

    protected class ItemClickedListener implements ItemListener {
        @Override
        public boolean itemClicked(RecyclerView.ViewHolder holder, int position, int resId) {
            boolean result = false;
            switch (resId){
                case R.id.save_photo_image_view:
                    result = updatePhotosData(position);
                    break;
                case R.id.share_photo_image_view:
                    result = Utils.shareImage(getContext(), getActivity(), itemsData.get(position).webformatURL);
                    break;
                case R.id.item_image_view:
                    launchImageViewer(itemsData, position);
                    break;
            }
            return result;
        }
    }
}
