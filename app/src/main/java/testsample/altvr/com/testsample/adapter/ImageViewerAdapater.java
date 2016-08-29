package testsample.altvr.com.testsample.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import testsample.altvr.com.testsample.R;
import testsample.altvr.com.testsample.util.Utils;
import testsample.altvr.com.testsample.vo.PhotoVo;

/**
 * Created by hassan on 8/28/2016.
 */
public class ImageViewerAdapater extends PagerAdapter {

    private ArrayList<PhotoVo> mItemData = new ArrayList<>();
    private Context mContext;

    public ImageViewerAdapater(Context ctx, ArrayList<PhotoVo> itemData){
        mContext = ctx;
        mItemData = itemData;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();

        View viewItem = inflater.inflate(R.layout.viewer_image_item, container, false);
        ImageView imageView = (ImageView) viewItem.findViewById(R.id.imageView);
        Utils.loadImage(mContext, mItemData.get(position).webformatURL, imageView, false);
        ((ViewPager)container).addView(viewItem);

        return viewItem;
    }


    @Override
    public int getCount() {
        return mItemData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View)object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
