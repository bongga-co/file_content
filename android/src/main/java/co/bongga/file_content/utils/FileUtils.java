package co.bongga.file_content.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;

public class FileUtils {
    public static String getNameFromUri(Context context, Uri uri) {
        String str;

        if (uri == null || uri.getScheme() == null) {
            return null;
        }

        if (uri.getScheme().equalsIgnoreCase(Schema.CONTENT)) {
            str = ContentUtils.getNameFromUri(context, uri);
        } else {
            str = Uri.decode(uri.getLastPathSegment());
        }

        return str;
    }

    public static long getFileSize(Context context, Uri uri) {
        long size = 0;

        if (uri == null || context == null || uri.getScheme() == null) {
            return size;
        }

        if (uri.getScheme().equalsIgnoreCase(Schema.CONTENT)) {
            size = ContentUtils.getFileSize(context, uri);
        } else {
            size = new File(uri.getPath()).length();
        }

        return size;
    }
}
