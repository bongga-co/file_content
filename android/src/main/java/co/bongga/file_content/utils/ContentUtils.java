package co.bongga.file_content.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.io.FileNotFoundException;

public class ContentUtils {

    public static boolean hasAccess(Context context, Uri uri) {
        try {
            context.getContentResolver().openFileDescriptor(uri, "r");
            return true;
        } catch (FileNotFoundException | SecurityException unused) {
            return false;
        }
    }

    public static String getNameFromUri(Context context, Uri uri) {
        Cursor cursor;
        String name = "unnamed";

        ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver == null || uri == null) {
            return null;
        }

        String[] columns = { "_display_name" };
        String authority = uri.getAuthority();

        if (authority != null) {

            if (authority.equalsIgnoreCase("gmail-ls")) {
                columns[0] = "_display_name";
            } else if (authority.contains("com.yahoo.mobile.client.android.mail")) {
                columns[0] = "attachmentName";
            }

        }

        try {
            cursor = contentResolver.query(uri, columns, null, null, null);
        } catch (UnsupportedOperationException e) {
            cursor = null;
        }

        if (cursor != null) {

            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(columns[0]);
            name = cursor.getString(columnIndex);

            if (!cursor.isClosed()) {
                cursor.close();
            }
        }

        return name;
    }

    public static long getFileSize(Context context, Uri uri) {
        Cursor cursor;
        long size = 0;

        ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver == null || uri == null) {
            return size;
        }

        String[] columns = { "_size" };

        try {
            cursor = contentResolver.query(uri, columns, null, null, null);
        } catch (UnsupportedOperationException e) {
            cursor = null;
        }

        if (cursor != null) {

            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(columns[0]);
            size = cursor.getLong(columnIndex);

            if (!cursor.isClosed()) {
                cursor.close();
            }
        }

        return size;
    }
}
