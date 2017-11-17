package base.simfw.com.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zdmy on 2017/11/14.
 */

public class DBHelper extends SQLiteOpenHelper {

    String[] createSqls;

    public void setUpdateSqls(String[] updateSqls) {
        this.updateSqls = updateSqls;
    }

    public void setCreateSqls(String[] createSqls) {
        this.createSqls = createSqls;
    }

    String[] updateSqls;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String[] arr = createSqls;
        //创建表
        for (int i = 0; i < arr.length; i++) {
            String sql = arr[i];
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String[] arr = updateSqls;
        //执行更新语句
        for (int i = 0; i < arr.length; i++) {
            String sql = arr[i];
            db.execSQL(sql);
        }
    }
}
