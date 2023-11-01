package com.bolddriver.contactshooker.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bolddriver.contactshooker.database.ContactsDBHelper;


public class ContactsInfoProvider extends ContentProvider {

    private ContactsDBHelper dbHelper;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int CONTACTS = 1;
    private static final int CONTACT = 2;

    static {
        // 往Uri匹配器中添加指定的数据路径
        URI_MATCHER.addURI(ContactsInfoContent.AUTHORITIES, "/contacts", CONTACTS);
        URI_MATCHER.addURI(ContactsInfoContent.AUTHORITIES, "/contacts/#", CONTACT);
    }

    @Override
    public boolean onCreate() {
        dbHelper = ContactsDBHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (URI_MATCHER.match(uri) == CONTACTS) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            return db.query(ContactsDBHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        if (URI_MATCHER.match(uri) == CONTACTS) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long rowId = db.insert(ContactsDBHelper.TABLE_NAME, null, values);
        }
        return uri;


    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (URI_MATCHER.match(uri)) {
            //删除多行
            case CONTACTS:
                SQLiteDatabase db1 = dbHelper.getWritableDatabase();
                count = db1.delete(ContactsDBHelper.TABLE_NAME, selection, selectionArgs);
                db1.close();
                break;
            //删除单行
            case CONTACT:
                String id = uri.getLastPathSegment();
                SQLiteDatabase db2 = dbHelper.getWritableDatabase();
                count = db2.delete(ContactsDBHelper.TABLE_NAME, "_id=?", new String[]{id});
                db2.close();
                break;
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}