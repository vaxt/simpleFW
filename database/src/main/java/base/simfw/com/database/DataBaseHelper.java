package base.simfw.com.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zdmy on 2017/11/13.
 */

public abstract class DataBaseHelper {

    protected DBHelper mDbHelper;

    protected SQLiteDatabase mDb;

    private int mDbVersion;

    private String mDbName;

    private String[] mDbCreateSql;

    private String[] mDbUpdateSql;

    protected abstract int getmDbVersion(Context context);

    protected abstract String getDbName(Context context);

    protected abstract String[] getDbCreateSql(Context context);

    protected abstract String[] getDbUpdateSql(Context context);

    public DataBaseHelper(Context context) {
        this.mDbVersion = this.getmDbVersion(context);
        mDbName = getDbName(context);
        mDbCreateSql = getDbCreateSql(context);
        mDbUpdateSql = getDbUpdateSql(context);
        mDbHelper = new DBHelper(context, mDbName, null, mDbVersion);
        mDbHelper.setCreateSqls(mDbCreateSql);
        mDbHelper.setUpdateSqls(mDbUpdateSql);
    }

    protected void open() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mDb = mDbHelper.getWritableDatabase();
            }
        }).start();
    }

    protected SQLiteDatabase getDB() {
        return mDb;
    }

    public void close() {
        mDb.close();
        mDbHelper.close();
    }

    private void ContentValuesPut(ContentValues contentValues, String key, Object value) {
        if (value == null) {
            contentValues.put(key, "");
        } else {
            String className = value.getClass().getName();
            switch (className) {
                case "java.lang.String":
                    contentValues.put(key, value.toString());
                    break;
                case "java.lang.Integer":
                    contentValues.put(key, Integer.valueOf(value.toString()));
                    break;
                case "java.lang.Float":
                    contentValues.put(key, Float.valueOf(value.toString()));
                    break;
                case "java.lang.Double":
                    contentValues.put(key, Double.valueOf(value.toString()));
                    break;
                case "java.lang.Boolean":
                    contentValues.put(key, Boolean.valueOf(value.toString()));
                    break;
                case "java.lang.Long":
                    contentValues.put(key, Long.valueOf(value.toString()));
                    break;
                case "java.lang.Short":
                    contentValues.put(key, Short.valueOf(value.toString()));
                    break;
            }
        }
    }

    public boolean insert(String tableName, String[] columns, Object[] values) {
        ContentValues contentValues = new ContentValues();
        for (int rows = 0; rows < columns.length; ++rows) {
            ContentValuesPut(contentValues, columns[rows], values[rows]);
        }
        long rowId = this.mDb.insert(tableName, null, contentValues);
        return rowId != -1;
    }

    public boolean insert(String tableName, Map<String, Object> columnValues) {
        ContentValues contentValues = new ContentValues();
        Iterator iterator = columnValues.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            this.ContentValuesPut(contentValues, key, columnValues.get(key));
        }
        long rowId = mDb.insert(tableName, null, contentValues);
        return rowId != -1;
    }


    /**
     * 对where条件字段进行拼接
     *
     * @param whereColumns 条件字段，如 id, name
     * @return 拼接后的字符串 ，如，"id = ? and name = ?"
     */
    private String initWhereSqlFromArray(String[] whereColumns) {
        StringBuffer whereStr = new StringBuffer();
        for (int i = 0; i < whereColumns.length; i++) {
            whereStr.append(whereColumns[i]).append(" = ? ");
            if (i < whereColumns.length - 1) {
                whereStr.append(" and ");
            }
        }
        return whereStr.toString();
    }

    private Map<String, Object> initWhereSqlFromMap(Map<String, String> whereParams) {
        Set set = whereParams.keySet();
        String[] temp = new String[whereParams.size()];
        int i = 0;
        Iterator iterator = set.iterator();
        StringBuffer whereStr = new StringBuffer();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            whereStr.append(key).append(" = ? ");
            temp[i] = whereParams.get(key);
            if (i < set.size() - 1) {
                whereStr.append(" and ");
            }
            i++;
        }
        HashMap result = new HashMap();
        result.put("whereSql", whereStr);
        result.put("whereSqlParam", temp);
        return result;
    }


    public boolean update(String tableName, String[] cloumns, Object[] values, String[] whereColumns, String[] whereArgs) {
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < cloumns.length; i++) {
            ContentValuesPut(contentValues, cloumns[i], values[i]);
        }

        String whereClause = initWhereSqlFromArray(whereColumns);
        int rowNumber = mDb.update(tableName, contentValues, whereClause, whereArgs);
        return rowNumber > 0;
    }

    public boolean update(String tableName, Map<String, Object> columnValues, Map<String, String> whereParam) {
        ContentValues contentValues = new ContentValues();
        Iterator iterator = columnValues.keySet().iterator();
        String cloumns;
        while (iterator.hasNext()) {
            cloumns = (String) iterator.next();
            ContentValuesPut(contentValues, cloumns, columnValues.get(cloumns));
        }

        Map map = initWhereSqlFromMap(whereParam);
        int rowNumber = mDb.update(tableName, contentValues, (String) map.get("whereSql"),
                (String[]) map.get("whereSqlParam"));
        return rowNumber > 0;
    }

    public boolean delete(String tableName, String[] whereColumns, String[] whereParam) {
        String whereStr = initWhereSqlFromArray(whereColumns);
        int rowNumber = mDb.delete(tableName, whereStr, whereParam);
        return rowNumber > 0;
    }

    public boolean delete(String tableName, Map<String, String> whereParms) {
        Map map = initWhereSqlFromMap(whereParms);
        int rowNumber = mDb.delete(tableName, map.get("whereSql").toString(), (String[]) map.get("whereSqlParam"));
        return rowNumber > 0;
    }


    public List<Map> queryListMap(String sql, String[] params) {
        ArrayList list = new ArrayList();
        Cursor cursor = mDb.rawQuery(sql, params);
        int columnCount = cursor.getColumnCount();
        while (cursor.moveToNext()) {
            HashMap item = new HashMap();
            for (int i = 0; i < columnCount; i++) {
                int type = cursor.getType(i);
                switch (type) {
                    case 0:
                        item.put(cursor.getColumnName(i), null);
                        break;
                    case 1:
                        item.put(cursor.getColumnName(i), cursor.getInt(i));
                        break;
                    case 2:
                        item.put(cursor.getColumnName(i), cursor.getFloat(i));
                        break;
                    case 3:
                        item.put(cursor.getColumnName(i), cursor.getString(i));
                        break;
                }
            }
            list.add(item);
        }
        cursor.close();
        return list;
    }

    /**
     * 查询单条数据返回map
     * @param sql
     * @param params
     * @return
     */
    public Map queryItemMap(String sql,String[] params){
        Cursor cursor = this.mDb.rawQuery(sql,params);
        HashMap map = new HashMap();
        if (cursor.moveToNext()){
            for (int i = 0;i < cursor.getColumnCount();++i){
                int type = cursor.getType(i);
                switch (type){
                    case 0:
                        map.put(cursor.getColumnName(i),null);
                        break;
                    case 1:
                        map.put(cursor.getColumnName(i),cursor.getInt(i));
                        break;
                    case 2:
                        map.put(cursor.getColumnName(i),cursor.getFloat(i));
                        break;
                    case 3:
                        map.put(cursor.getColumnName(i),cursor.getString(i));
                        break;
                }
            }
        }
        cursor.close();
        return map;
    }

    public void execSQL(String sql){
        this.mDb.execSQL(sql);
    }

    public void execSQL(String sql,Object[] params){
        this.mDb.execSQL(sql,params);
    }
}
