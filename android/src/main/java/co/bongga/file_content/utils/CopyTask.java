package co.bongga.file_content.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import co.bongga.file_content.interfaces.CopyTaskListener;

public class CopyTask extends AsyncTask<Uri, Integer, String> {

    private CopyTaskListener callback = null;
    private ProgressDialog dialog = null;
    private long size = 0;
    private String name = "unnamed";
    private Context context;

    public CopyTask(Context context, String name, long size, CopyTaskListener callback) {
        super();

        this.context = context;
        this.size = size;
        this.name = name;
        this.callback = callback;
    }

    public CopyTask(Context context, String name, long size) {
        super();

        this.context = context;
        this.size = size;
        this.name = name;
    }

    public void setProgressDialog(ProgressDialog dialog) {
        this.dialog = dialog;
    }

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

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if(dialog != null) {
            this.dialog.setProgress((int) ((long) values[0]));
        }
    }

    @Override
    protected String doInBackground(Uri... params) {
        String path = null;
        File dir = context.getExternalCacheDir();
        File file;
        Uri uri = params[0];

        if(dir != null) {
            file = new File(dir, name);
        } else {
            file = context.getExternalFilesDir("pdfs");
        }

        if(file != null && file.exists()) {
            return file.getPath();
        }

        if(uri.getScheme() != null) {

            InputStream input = null;
            FileOutputStream output = null;

            try {

                if(uri.getScheme().equals(Schema.CONTENT)) {
                    input = context.getContentResolver().openInputStream(uri);
                } else {
                    input = new FileInputStream(new File(uri.getPath()));
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

                if(file != null) {
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

        return path;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
