package testsample.altvr.com.testsample.util;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

public class FABScrollBehavior extends FloatingActionButton.Behavior {

    public FABScrollBehavior(Context context, AttributeSet attributeSet){
        super();
    }
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout
                                                   coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {

        //hide fab in case of any activity on the list starts
        if(child.getVisibility() == View.VISIBLE)child.hide();

        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, final FloatingActionButton child, View target) {
        super.onStopNestedScroll(coordinatorLayout, child, target);

        //show fab in case of any activity on the list stops
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(child.getVisibility() == View.GONE)child.show();
            }
        }, 800);
    }
}
