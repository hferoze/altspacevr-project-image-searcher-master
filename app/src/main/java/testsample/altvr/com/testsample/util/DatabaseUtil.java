package testsample.altvr.com.testsample.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import testsample.altvr.com.testsample.events.SavedPhotosEvent;
import testsample.altvr.com.testsample.vo.PhotoVo;

/**
 * Created by tejus on 4/14/2016.
 */
public class DatabaseUtil extends SQLiteOpenHelper {
    private LogUtil log = new LogUtil(DatabaseUtil.class);

    private static final int DATABASE_VERSION = 2;
    //DB and tables
    private static final String DATABASE_NAME = "imagesearcher.db";
    private static final String TABLE_PHOTOS = "photos";

    //Columns for Images table
    private static final String KEY_PHOTO_ID = "id";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_TAGS = "tags";
    private static final String KEY_LIKES = "likes";
    private static final String KEY_VIEWS = "views";
    private static final String KEY_USER_PHOTO = "userphoto";
    private static final String KEY_USER_NAME = "username";
    private static final String KEY_COMMENTS = "comments";

    SQLiteDatabase mDb;

    public DatabaseUtil(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String IMAGES_TABLE = "CREATE TABLE " + TABLE_PHOTOS + "("
                            + KEY_PHOTO_ID + " INTEGER PRIMARY KEY,"
                            + KEY_PHOTO + " TEXT,"
                            + KEY_TAGS + " TEXT,"
                            + KEY_LIKES + " TEXT,"
                            + KEY_VIEWS + " TEXT,"
                            + KEY_USER_PHOTO + " TEXT,"
                            + KEY_USER_NAME + " TEXT,"
                            + KEY_COMMENTS + " TEXT" +")";
        db.execSQL(IMAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
        onCreate(db);
    }

    /**
     * YOUR CODE HERE
     *
     * For part 1b, you should fill in the various CRUD operations below to manipulate the db
     * returned by getWritableDatabase() to store/load photos.
     */
    public boolean create(PhotoVo photoVo) {

        log.d("creating data for " + photoVo.id);

        ContentValues values = new ContentValues();

        values.put(KEY_PHOTO_ID, photoVo.id);
        values.put(KEY_PHOTO, photoVo.webformatURL);
        values.put(KEY_TAGS, photoVo.tags);
        values.put(KEY_LIKES, photoVo.likes);
        values.put(KEY_VIEWS, photoVo.views);
        values.put(KEY_USER_PHOTO, photoVo.userImageURL);
        values.put(KEY_USER_NAME, photoVo.user);
        values.put(KEY_COMMENTS, photoVo.comments);

        mDb = getWritableDatabase();
        long id = mDb.insert(TABLE_PHOTOS, null, values);
        mDb.close();

        return id>0;
    }

    public boolean checkRecordExists(String photoId) {

        log.d("check if photo id " + photoId + " exists");

        String sql ="SELECT "+KEY_PHOTO_ID+" FROM "+TABLE_PHOTOS+" WHERE "+KEY_PHOTO_ID+"="+photoId;

        mDb = getWritableDatabase();
        Cursor cursor = mDb.rawQuery(sql, null);
        boolean exists = cursor.getCount()>0;
        cursor.close();
        mDb.close();
        return exists;
    }

    public boolean delete(PhotoVo photoVo) {

        boolean deleteSuccessful = false;

        mDb = getWritableDatabase();
        deleteSuccessful = mDb.delete(TABLE_PHOTOS, KEY_PHOTO_ID+" ='" + photoVo.id + "'", null) > 0;
        mDb.close();

        return deleteSuccessful;
    }

    public boolean update(PhotoVo photoVo) {

        ContentValues values = new ContentValues();

        values.put(KEY_PHOTO_ID, photoVo.id);
        values.put(KEY_PHOTO, photoVo.webformatURL);
        values.put(KEY_TAGS, photoVo.tags);
        values.put(KEY_LIKES, photoVo.likes);
        values.put(KEY_VIEWS, photoVo.views);
        values.put(KEY_USER_PHOTO, photoVo.userImageURL);
        values.put(KEY_USER_NAME, photoVo.user);
        values.put(KEY_COMMENTS, photoVo.comments);

        String where = KEY_PHOTO_ID+" = ?";

        String[] whereArgs = { photoVo.id };

        mDb = getWritableDatabase();

        boolean updateSuccessful = mDb.update(TABLE_PHOTOS, values, where, whereArgs) > 0;
        mDb.close();

        return updateSuccessful;
    }

    public List<PhotoVo> readAll() {

        log.d("Read all data from db ");

        List<PhotoVo> recordsList = new ArrayList<>();

        String sql = "SELECT * FROM "+TABLE_PHOTOS+" ORDER BY "+KEY_PHOTO_ID+" DESC";

        mDb = getWritableDatabase();
        Cursor cursor = mDb.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_PHOTO_ID));
                String photo = cursor.getString(cursor.getColumnIndex(KEY_PHOTO));
                String tags = cursor.getString(cursor.getColumnIndex(KEY_TAGS));
                String likes = cursor.getString(cursor.getColumnIndex(KEY_LIKES));
                String views = cursor.getString(cursor.getColumnIndex(KEY_VIEWS));
                String user_photo = cursor.getString(cursor.getColumnIndex(KEY_USER_PHOTO));
                String user_name = cursor.getString(cursor.getColumnIndex(KEY_USER_NAME));
                String comments_count = cursor.getString(cursor.getColumnIndex(KEY_COMMENTS));

                PhotoVo photoVo = new PhotoVo();
                photoVo.id = Integer.toString(id);
                photoVo.webformatURL = photo;
                photoVo.tags = tags;
                photoVo.likes = likes!=null&&likes!=""?Integer.parseInt(likes):0;
                photoVo.views = views!=null&&views!=""?Integer.parseInt(views):0;
                photoVo.userImageURL = user_photo;
                photoVo.user = user_name;
                photoVo.comments = comments_count!=null&&comments_count!=""?Integer.parseInt(comments_count):0;

                recordsList.add(photoVo);

            } while (cursor.moveToNext());
        }

        cursor.close();
        mDb.close();

        SavedPhotosEvent savedPhotosEvent = new SavedPhotosEvent(recordsList);
        EventBus.getDefault().post(savedPhotosEvent);

        return recordsList;
    }

    public int getCount() {

        mDb = getWritableDatabase();

        String sql = "SELECT * FROM "+TABLE_PHOTOS;
        int recordCount = mDb.rawQuery(sql, null).getCount();
        mDb.close();

        return recordCount;
    }
}
