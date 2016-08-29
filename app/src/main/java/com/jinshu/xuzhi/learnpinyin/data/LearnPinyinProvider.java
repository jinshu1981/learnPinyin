package com.jinshu.xuzhi.learnpinyin.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Collections;

/**
 * Created by xuzhi on 2016/8/25.
 */
public class LearnPinyinProvider extends ContentProvider {

    private final String LOG_TAG = this.getClass().getSimpleName();
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private com.jinshu.xuzhi.learnpinyin.data.LearnPinyinDbHelper mOpenHelper;
    static final int LEARN_PINYIN_CHARACTER = 100;
    static final int LEARN_PINYIN_CHARACTER_WITH_NAME = 101;
    static final int LEARN_PINYIN_CHARACTER_WITH_DONE = 102;
    static final int LEARN_PINYIN_CHARACTER_WITH_ID = 103;
    static final int LEARN_PINYIN_CHARACTER_WITH_ID_LIST = 104;
    static final int LEARN_PINYIN_CHARACTER_WITH_NAME_LIST = 105;

    private static final SQLiteQueryBuilder sLearnPinyinQueryBuilder;
    static{
        sLearnPinyinQueryBuilder = new SQLiteQueryBuilder();
    }
    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.PATH_CHARACTER, LEARN_PINYIN_CHARACTER);
        matcher.addURI(authority, com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.PATH_CHARACTER + "/" + com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.COLUMN_ID +"/*", LEARN_PINYIN_CHARACTER_WITH_ID);
        matcher.addURI(authority, com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.PATH_CHARACTER + "/" + com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.PATH_CHARACTER_ID_LIST +"/*", LEARN_PINYIN_CHARACTER_WITH_ID_LIST);
        matcher.addURI(authority, com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.PATH_CHARACTER + "/" + com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.COLUMN_NAME +"/*", LEARN_PINYIN_CHARACTER_WITH_NAME);
        matcher.addURI(authority, com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.PATH_CHARACTER + "/" + com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.PATH_CHARACTER_NAME_LIST +"/*", LEARN_PINYIN_CHARACTER_WITH_NAME_LIST);
        matcher.addURI(authority, com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.PATH_CHARACTER + "/" + com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.COLUMN_DONE +"/*", LEARN_PINYIN_CHARACTER_WITH_DONE);

        return matcher;
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new com.jinshu.xuzhi.learnpinyin.data.LearnPinyinDbHelper(getContext());
        return true;
    }
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case LEARN_PINYIN_CHARACTER:
            case LEARN_PINYIN_CHARACTER_WITH_DONE:
            case LEARN_PINYIN_CHARACTER_WITH_ID_LIST:
            case LEARN_PINYIN_CHARACTER_WITH_NAME_LIST:
                return com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.CONTENT_TYPE;

            case LEARN_PINYIN_CHARACTER_WITH_NAME:
            case LEARN_PINYIN_CHARACTER_WITH_ID:
                return com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
    /***********************************Character Table*********************************************/
    //Character.done = ?
    private static final String sCharacterByDoneSelection =
            com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME +
                    "." +  com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.COLUMN_DONE + " = ? ";
    //Character.name = ?
    private static final String sCharacterByNameSelection =
            com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME +
                    "." +  com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.COLUMN_NAME + " = ? ";
    //Character._id = ?
    private static final String sCharacterByIdSelection =
            com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME +
                    "." +  com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.COLUMN_ID + " = ? ";
    //Character._id IN (?,?,?...?)
    private static final String sCharacterByIdListSelection =
            com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME +
                    "." + com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.COLUMN_ID + " IN ";


    //Character.name IN (?,?,?...?)
    private static final String sCharacterByNameListSelection =
            com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME +
                    "." + com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.COLUMN_NAME + " IN ";
    private Cursor getCharacterByName(
            Uri uri, String[] projection, String sortOrder) {

        String name = com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.getNameFromUri(uri);
        Log.v(LOG_TAG, com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.COLUMN_NAME + " = " + name);
        sLearnPinyinQueryBuilder.setTables(com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME);
        return sLearnPinyinQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCharacterByNameSelection,
                new String[]{name},
                null,
                null,
                sortOrder
        );
    }
    private Cursor getCharacterByDone(
            Uri uri, String[] projection, String sortOrder) {

        String done = com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.getTheSecondPara(uri);
        Log.v(LOG_TAG, com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.COLUMN_DONE + " = " + done);
        sLearnPinyinQueryBuilder.setTables(com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME);
        return sLearnPinyinQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCharacterByDoneSelection,
                new String[]{done},
                null,
                null,
                sortOrder
        );
    }
    private Cursor getCharactersByIdList(
            Uri uri, String[] projection, String sortOrder) {

        String idString = com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.getTheSecondPara(uri).trim();
        String[] idArray = idString.split(",");
        sLearnPinyinQueryBuilder.setTables(com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME);
        return sLearnPinyinQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCharacterByIdListSelection + "(" + TextUtils.join(",", Collections.nCopies(idArray.length, "?")) + ")",/*generate (?,?,?)*/
                idArray,
                null,
                null,
                sortOrder
        );
    }
    private Cursor getCharactersByNameList(
            Uri uri, String[] projection, String sortOrder) {

        String nameString = com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.getTheSecondPara(uri).trim();
        String[] nameArray = nameString.split("");
        sLearnPinyinQueryBuilder.setTables(com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME);
        return sLearnPinyinQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCharacterByNameListSelection + "(" + TextUtils.join(",", Collections.nCopies(nameArray.length, "?")) + ")",/*generate (?,?,?)*/
                nameArray,
                null,
                null,
                sortOrder
        );
    }
    /*****************************************************************************************************************/
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        Log.v(LOG_TAG, "query uri = " + uri.toString());
        switch (sUriMatcher.match(uri)) {
            case LEARN_PINYIN_CHARACTER_WITH_NAME: {
                retCursor = getCharacterByName(uri, projection, sortOrder);
                break;
            }
            case LEARN_PINYIN_CHARACTER_WITH_DONE: {
                retCursor = getCharacterByDone(uri, projection, sortOrder);
                break;
            }
            case LEARN_PINYIN_CHARACTER_WITH_ID_LIST: {
                retCursor = getCharactersByIdList(uri, projection, sortOrder);
                break;
            }
            case LEARN_PINYIN_CHARACTER_WITH_NAME_LIST: {
                retCursor = getCharactersByNameList(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        Log.v(LOG_TAG,"insert uri = " + uri.toString());
        switch (match) {
            case LEARN_PINYIN_CHARACTER: {
                long _id = db.insert(com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.buildCharacterUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case LEARN_PINYIN_CHARACTER:
                rowsDeleted = db.delete(
                        com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME, selection, selectionArgs);
                break;


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }



    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case LEARN_PINYIN_CHARACTER:
                rowsUpdated = db.update(com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LEARN_PINYIN_CHARACTER:{
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(com.jinshu.xuzhi.learnpinyin.data.LearnPinyinContract.Character.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }

            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}