package co.bongga.file_content.utils;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

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

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected String doInBackground(Uri... uris) {
        String path = null;

        if(context != null) {
            Uri uri = uris[0];
            File dir = context.get().getExternalCacheDir();
            File file = new File(dir, name);

            if(uri.getScheme() != null) {

                InputStream input = null;
                FileOutputStream output = null;

                try {

                    if(uri.getScheme().equals(Schema.CONTENT)) {
                        input = context.get().getContentResolver().openInputStream(uri);
                    } else {
                        if(uri.getPath() != null) {
                            input = new FileInputStream(new File(uri.getPath()));
                        }
                    }

                    output = new FileOutputStream(file);

                    byte[] buffer = new byte[4 * 1024];
                    int read;

                    if(input == null || input.read(buffer) == -1) return null;

                    long readCount = 0;

                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);

                        readCount += (long) read;

                        int progress = (int) ((((float) readCount) / ((float) this.size)) * 100.0f);

                        publishProgress(progress);
                    }

                    output.flush();

                    path = file.getAbsolutePath();

                } catch(Exception e) {
                    path = null;
                } finally {
                    try {
                        if (output != null) {
                            output.close();
                        }

                        if(input != null) {
                            input.close();
                        }

                    } catch (IOException e) {
                        path = null;
                    }
                }
            }
        }

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
