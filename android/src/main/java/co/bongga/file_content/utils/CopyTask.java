package co.bongga.file_content.utils;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import co.bongga.file_content.interfaces.CopyTaskListener;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class CopyTask extends AsyncTask<Uri, Integer, String> {

    private CopyTaskListener callback = null;
    private ProgressDialog dialog = null;
    private long size;
    private String name;
    private WeakReference<Context> context;

    public CopyTask(Context context, String name, long size, CopyTaskListener callback) {
        super();

        this.context = new WeakReference<>(context);
        this.size = size;
        this.name = name;
        this.callback = callback;
    }

    public CopyTask(Context context, String name, long size) {
        super();

        this.context = new WeakReference<>(context);
        this.size = size;
        this.name = name;
    }

    public void setProgressDialog(ProgressDialog dialog) {
        this.dialog = dialog;
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (this.dialog != null) {
            this.dialog.dismiss();
        }

        if (this.callback != null) {
            this.callback.onCopyFileFinished(result);
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if(dialog != null) {
            this.dialog.setProgress((int) ((long) values[0]));
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    protected String doInBackground(Uri... params) {
        String path = null;

        if(this.context != null) {
            File dir = context.get().getCacheDir();

            File file = null;
            Uri uri = params[0];

            if(dir != null) {

                String filePath = dir.getPath() + File.separator + name;
                file = new File(filePath);

                if(file.exists()) {
                    return file.getAbsolutePath();
                }
            }

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

                    if(file != null) {
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
                    }

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

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
