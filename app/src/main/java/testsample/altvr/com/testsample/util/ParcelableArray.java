package testsample.altvr.com.testsample.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import testsample.altvr.com.testsample.vo.PhotoVo;

/**
 * Created by hassan on 8/28/2016.
 */
public class ParcelableArray implements Parcelable {

    private ArrayList<PhotoVo> mDataItemsArray;
    private int mStartPosition;

    public ParcelableArray(){}

    private ParcelableArray(Parcel in) {
        mStartPosition = in.readInt();
    }

    public void setStartPosition(int position){
        mStartPosition = position;
    }

    public int getStartPosition() {
        return mStartPosition;
    }

    public ArrayList<PhotoVo> getDataArray() {
        return mDataItemsArray;
    }

    public void setDataArray(ArrayList<PhotoVo> dataArray) {
        mDataItemsArray = dataArray;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mStartPosition);
    }

    public static final Parcelable.Creator<ParcelableArray> CREATOR = new Parcelable.Creator<ParcelableArray>() {
        public ParcelableArray createFromParcel(Parcel in) {
            return new ParcelableArray(in);
        }

        public ParcelableArray[] newArray(int size) {
            return new ParcelableArray[size];
        }
    };
}
