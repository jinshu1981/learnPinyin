package com.jinshu.xuzhi.learnpinyin;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.jinshu.xuzhi.learnpinyin.data.LearnPinyinDbHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by xuzhi on 2016/8/25.
 */
public class Utility {
    private final static String LOG_TAG = Utility.class.getSimpleName();

    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     * */
    static public void copyDataBase(Context c) throws IOException {
        String dbname = "PinyinCharacters.db";
        // Open your local db as the input stream
        InputStream myInput = c.getAssets().open(dbname);

        // Open the empty db as the output stream
        final File dir = new File(c.getFilesDir() + "/data/data/com.jinshu.xuzhi.learnpinyin/databases");
        dir.mkdirs(); //create folders where write files

        /*create empty db to writing*/
        LearnPinyinDbHelper myDbHelper = new LearnPinyinDbHelper(c);
        SQLiteDatabase db = myDbHelper.getReadableDatabase();
        db.close();

        OutputStream myOutput = new FileOutputStream("/data/data/com.jinshu.xuzhi.learnpinyin/databases/PinyinCharacters.db");

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[618496];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
            Log.e(LOG_TAG, "length = " + length + "buffer = " + buffer.toString());
        }
        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

     static boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }
}
