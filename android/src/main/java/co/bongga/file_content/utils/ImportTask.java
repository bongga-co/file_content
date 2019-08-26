package co.bongga.file_content.utils;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.Calendar;

import co.bongga.file_content.interfaces.CopyTaskListener;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class ImportTask extends AsyncTask<Uri, Integer, String>  {

    private WeakReference<Context> context;
    private CopyTaskListener callback = null;
    private ProgressDialog dialog;
    private ProgressBar progressBar;
    private String name;
    private long size;

    public ImportTask(Context context, String name, long size, ProgressDialog dialog, CopyTaskListener callback) {
        this.context = new WeakReference<>(context);
        this.size = size;
        this.name = name;
        this.callback = callback;
        this.dialog = dialog;
    }

    public ImportTask(Context context, String name, long size, ProgressDialog dialog) {
        this.context = new WeakReference<>(context);
        this.size = size;
        this.name = name;
        this.dialog = dialog;
    }

    public ImportTask(Context context, String name, long size, ProgressBar dialog, CopyTaskListener callback) {
        this.context = new WeakReference<>(context);
        this.size = size;
        this.name = name;
        this.callback = callback;
        this.progressBar = dialog;
    }

    public ImportTask(Context context, String name, long size, ProgressBar dialog) {
        this.context = new WeakReference<>(context);
        this.size = size;
        this.name = name;
        this.progressBar = dialog;
    }

    private final String copyToTemp(Uri uri) {
        StringBuilder strBuilder = new StringBuilder();
        Context ctx = context.get();

        File baseDir = ctx.getCacheDir();

        if(baseDir == null) return null;

        Calendar cal = Calendar.getInstance();
        String prefix = String.valueOf(cal.getTimeInMillis());

        name = prefix.concat(name);

        String filePath = strBuilder.append(baseDir.getPath()).append(File.separator).append(name).toString();
        File file = new File(filePath);

        BufferedInputStream inputStream;
        BufferedOutputStream outputStream;

        try {
            inputStream = new BufferedInputStream(ctx.getContentResolver().openInputStream(uri));
            outputStream = new BufferedOutputStream(new FileOutputStream(file));

            byte[] buf = new byte[1024];
            int len = inputStream.read(buf);

            while (len != -1) {
                outputStream.write(buf, 0, len);
                len = inputStream.read(buf);
            }

            outputStream.flush();

            inputStream.close();
            outputStream.close();

        } catch(Exception e) {
            return null;
        }

        return file.getAbsolutePath();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected String doInBackground(Uri... uris) {
        Uri uri = uris[0];
        String path = copyToTemp(uri);

        return path;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if(dialog != null) {
            dialog.setProgress((int) ((long) values[0]));
        }

        if(progressBar != null) {
            progressBar.setProgress((int) ((long) values[0]));
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (dialog != null) {
            dialog.dismiss();
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (callback != null) {
            try {
                Thread.sleep(3000);
                callback.onCopyFileFinished(result);
            } catch(Exception e) {
                callback.onCopyFileFinished(result);
            }
        }
    }
}
