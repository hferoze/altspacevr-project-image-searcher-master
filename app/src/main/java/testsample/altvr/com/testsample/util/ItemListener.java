package testsample.altvr.com.testsample.util;

import android.support.v7.widget.RecyclerView;

/**
 * Created by hassan on 8/26/2016.
 */
public interface ItemListener {
    boolean itemClicked(RecyclerView.ViewHolder holder, int position, int resId);
}
