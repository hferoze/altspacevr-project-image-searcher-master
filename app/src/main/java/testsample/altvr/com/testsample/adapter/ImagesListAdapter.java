package testsample.altvr.com.testsample.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import testsample.altvr.com.testsample.R;
import testsample.altvr.com.testsample.util.DatabaseUtil;
import testsample.altvr.com.testsample.util.ItemListener;
import testsample.altvr.com.testsample.util.Utils;
import testsample.altvr.com.testsample.vo.PhotoVo;

/**
 * Created by hassan on 8/29/2016.
 */
public class ImagesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ItemListener mListener;
    private List<PhotoVo> mItems;
    private Context mContext;
    private DatabaseUtil mDbUtil;

    ItemViewHolder itemViewHolder;

    public ImagesListAdapter(List<PhotoVo> items, ItemListener listener, Context context) {
        this.mItems = items;
        this.mListener = listener;
        mContext = context;
        mDbUtil = new DatabaseUtil(mContext);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_photos_item, viewGroup, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder  holder, final int position) {
      	/*
         * YOUR CODE HERE
         *
         * For Part 1a, you should get the proper PhotoVo instance from the mItems collection,
         * image, text, etc, into the ViewHolder (which will be an ItemViewHolder.)
         *
         * For part 1b, you should attach a click listener to the save label so users can save
         * or delete photos from their local db.
         */

        if (mItems!=null
                && !TextUtils.isEmpty(mItems.get(position).webformatURL)) {
            final ItemViewHolder viewHolder = (ItemViewHolder) holder;
            itemViewHolder = viewHolder;

            PhotoVo photoVo = mItems.get(position);

            Utils.loadImage(mContext, photoVo.webformatURL, viewHolder.itemImage);

            final String likes = photoVo.likes + " Likes";
            viewHolder.likes.setText(likes);

            String views = ""+photoVo.views;
            viewHolder.views.setText(views);

            String comments = ""+photoVo.comments;
            viewHolder.comments.setText(comments);

            if(!TextUtils.isEmpty(photoVo.tags))
                viewHolder.itemTags.setText(photoVo.tags);
            else
                viewHolder.itemTags.setText("");

            if(!TextUtils.isEmpty(photoVo.userImageURL))
                Utils.loadImage(mContext, photoVo.userImageURL, viewHolder.userImageView);
            else
                Utils.loadImage(mContext, R.drawable.placeholder, viewHolder.userImageView);

            if(!TextUtils.isEmpty(photoVo.user))
                viewHolder.userName.setText(photoVo.user);
            else
                viewHolder.userName.setText("");

            setPhotoSaved(viewHolder, position, mDbUtil.checkRecordExists(photoVo.id), false);

            viewHolder.itemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.itemClicked(viewHolder, position, viewHolder.itemImage.getId());
                }
            });

            viewHolder.sharePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.itemClicked(viewHolder, position, viewHolder.sharePhoto.getId());
                }
            });

            viewHolder.savePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setPhotoSaved(viewHolder, position, mListener.itemClicked(viewHolder, position, viewHolder.savePhoto.getId()), true);
                }
            });
        }
    }

    /**
     * changes state of savePhoto button
     * @param viewHolder current view holder
     * @param saved true or false
     */
    private void setPhotoSaved(RecyclerView.ViewHolder viewHolder, int position, boolean saved, boolean changed){
        if (saved) {
            ((ItemViewHolder) viewHolder).savePhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.heart, null));
        } else {
            ((ItemViewHolder) viewHolder).savePhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.heart_outline, null));
            if(changed)notifyItemRemoved(position);
        }
        ((ItemViewHolder) viewHolder).savePhoto.getDrawable().setTint(mContext.getResources().getColor(R.color.colorPrimaryLight));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView itemImage;
        public TextView itemTags;
        public TextView likes;
        public TextView views;
        public TextView userName;
        public TextView comments;
        public ImageView savePhoto;
        public ImageView sharePhoto;
        public ImageView userImageView;
        public ProgressBar imageLoadingProgressBar;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemTags = (TextView) itemView.findViewById(R.id.tags_text_view);
            likes = (TextView) itemView.findViewById(R.id.likes_text_view);
            views = (TextView) itemView.findViewById(R.id.v_text_view);
            userName = (TextView) itemView.findViewById(R.id.user_name_text_view);
            comments = (TextView) itemView.findViewById(R.id.comments_text_view);

            itemImage = (ImageView) itemView.findViewById(R.id.item_image_view);
            savePhoto = (ImageView) itemView.findViewById(R.id.save_photo_image_view);
            sharePhoto = (ImageView) itemView.findViewById(R.id.share_photo_image_view);
            userImageView = (ImageView) itemView.findViewById(R.id.user_image_image_view);
            imageLoadingProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_progress_bar);
        }
    }
}
