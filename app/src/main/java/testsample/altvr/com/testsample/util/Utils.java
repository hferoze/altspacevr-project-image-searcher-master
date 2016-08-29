package testsample.altvr.com.testsample.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import testsample.altvr.com.testsample.R;

/**
 * Created by hassan on 8/25/2016.
 */
public class Utils {

    private static final LogUtil log = new LogUtil(Utils.class.getSimpleName());

    private static boolean sResult = false;

    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public static int getRandomColor(Context context){
        String[] colorList  = context.getResources().getStringArray(R.array.colorsmore);
        return Color.parseColor(colorList[Utils.randInt(0,colorList.length-1)]);
    }

    public static Drawable getPlaceHolderImage(Context context){
        Drawable placeHolder = ContextCompat.getDrawable(context, R.drawable.placeholder);
        Drawable wrapDrawable = DrawableCompat.wrap(placeHolder);
        ColorFilter filter = new LightingColorFilter( getRandomColor(context), getRandomColor(context));
        wrapDrawable.setColorFilter(filter);
        return wrapDrawable;
    }

    public static float getScreenHeight(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    public static void loadImage(Context ctx, String path, ImageView target){
        Picasso.
                with(ctx).
                load(path).
                placeholder(getPlaceHolderImage(ctx)).
                into(target);
    }

    public static void loadImage(Context ctx, String path, ImageView target, boolean placeHolder){
        Picasso.
                with(ctx).
                load(path).
                placeholder(placeHolder?getPlaceHolderImage(ctx):null).
                into(target);
    }

    public static void loadImage(Context ctx, int resId, ImageView target){
        Picasso.
                with(ctx).
                load(resId).
                placeholder(getPlaceHolderImage(ctx)).
                into(target);
    }

    public static boolean saveImageToTemp(final Context ctx, String path, final String fileName){
        sResult = true;
        Picasso.with(ctx)
            .load(path)
            .into(new Target() {
                @Override
                public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
                    cleanInternalTempFolder(ctx);
                    File file = new File(getInternalTempFolder(ctx)+fileName);//ctx.getString(R.string.internal_temp_file_name));
                    try {
                        log.d("creating file " + getInternalTempFolder(ctx)+fileName);//ctx.getString(R.string.internal_temp_file_name));
                        if (file.createNewFile()) {
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 65, ostream);
                            ostream.close();
                            log.e("Image saved successfully");
                        }else{
                            log.e("Failed to save image!!");
                            sResult = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        sResult = false;
                    }
                }
                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    log.d("Saving image...");
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    log.e("Failed to load image!");
                    sResult = false;
                }
            });
        return sResult;
    }

    public static boolean createPrivateDirs(Context context) {
        boolean success = true;
        //create internal storage folder for temp files
        ArrayList<String> folders = new ArrayList<>();
        folders.add(getInternalTempFolder(context)); // /data/com.doodlefun/temp_image
        for (String s : folders) {
            File folder = new File(s);
            if (!folder.exists()) {
                log.d("Creating: " + folder);
                success = folder.mkdir();
            } else {
                log.d("Folder exists: " + folder);
            }
        }

        return success;
    }

    public static boolean shareImage(final Context ctx, final Activity activity,  final String imageUrl){
        final String fileName = ctx.getString(R.string.internal_temp_file_name)
                + UUID.randomUUID().toString()
                + ctx.getString(R.string.file_default_etx);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if(Utils.saveImageToTemp(ctx, imageUrl, fileName)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            log.d("sharing ... ");
                            sharingIntent.setType("image/jpeg");
                            sharingIntent.putExtra(Intent.EXTRA_STREAM,  Utils.getFileProviderImage(ctx, fileName));
                            activity.startActivityForResult(Intent.createChooser(sharingIntent, ctx.getString(R.string.share_title)), 0);
                        }
                    }, 200);
                    sResult = true;
                }else{
                    log.e("Failed to share image...");
                    sResult = false;
                }
            }
        });
        return sResult;
    }

    /**
     * Gives the uri of the file provider image location
     * @param context
     * @return uri of the file provider image location content://com.example.myapp.fileprovider/temp_image/temp.jpg
     */
    public static Uri getFileProviderImage(Context context, String fileName){
        File output=new File(getInternalTempFolder(context), fileName);
        return FileProvider.getUriForFile(context, context.getString(R.string.file_provider), output);
    }

    /**
     * Clean temp folder in internal memory
     * @param context
     * @return true if successful, else false
     */

    public static boolean cleanInternalTempFolder(Context context){
        boolean success = true;

        File folder = new File(getInternalTempFolder(context));
        log.d("deleting folder: " + folder.toString());
        String[] children = folder.list();
        if (children!=null) {
            for (int i = 0; i < children.length; i++) {
                File f = new File(folder, children[i]);
                success = f.delete();
                log.d("deleting internal copy: " + f.toString());
            }
        }
        return success;
    }

    public static String getInternalTempFolder(Context context){
        return context.getFilesDir()+context.getString(R.string.internal_temp_folder);
    }
}
