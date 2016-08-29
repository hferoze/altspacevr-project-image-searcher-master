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
import testsample.altvr.com.testsample.util.ItemListener;
import testsample.altvr.com.testsample.util.Utils;
import testsample.altvr.com.testsample.vo.PhotoVo;

/**
 * Created by hassan on 8/26/2016.
 */
public class SavedPhotosItemsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ItemListener mListener;
    private List<PhotoVo> mItems;
    private Context mContext;

    public SavedPhotosItemsListAdapter(List<PhotoVo> items,  ItemListener listener, Context context) {
        mItems = items;
        mListener = listener;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_photos_item, viewGroup, false);
        return new SavedItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (mItems!=null
                && !TextUtils.isEmpty(mItems.get(position).webformatURL)) {
            PhotoVo photoVo = mItems.get(position);
            final SavedItemViewHolder viewHolder = (SavedItemViewHolder) holder;

            Utils.loadImage(mContext, photoVo.webformatURL, viewHolder.itemImage);

            String likes = photoVo.likes + " Likes";
            viewHolder.likes.setText(likes);

            String views = ""+photoVo.views;
            viewHolder.views.setText(views);

            String comments = ""+photoVo.comments;
            viewHolder.comments.setText(comments);

            if(!TextUtils.isEmpty(mItems.get(position).tags))
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

            viewHolder.savePhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.heart, null));
            viewHolder.savePhoto.getDrawable().setTint(mContext.getResources().getColor(R.color.colorPrimaryLight));
            viewHolder.savePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mListener.itemClicked(viewHolder, position, viewHolder.savePhoto.getId())){
                        SavedPhotosItemsListAdapter.this.notifyItemRemoved(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mItems!=null && mItems.size()>0) return mItems.size();
        return 0;
    }

    public static class SavedItemViewHolder extends RecyclerView.ViewHolder {
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

        public SavedItemViewHolder(View itemView) {
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
