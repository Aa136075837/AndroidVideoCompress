package com.mac.compressjava.videocompressor;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.mac.compressjava.videocompressor.videocompression.MediaController;

import java.io.File;
import java.net.URISyntaxException;

public class VideoCompressAsyncTask extends AsyncTask<String, Float, String> {

    Context mContext;
    CompressListener mListener;

    public VideoCompressAsyncTask(CompressListener listener, Context context) {
        mListener = listener;
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mListener != null) {
            mListener.onCompressStart();
        }
    }

    @Override
    protected String doInBackground(final String... paths) {
        String filePath = null;
        try {
            filePath = SiliCompressor.with(mContext).compressVideo(paths[0], paths[1], new MediaController.CompressProgressListener() {
                @Override
                public void onProgress(float percent) {
                    publishProgress(percent);
                }
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return filePath;

    }

    @Override
    protected void onProgressUpdate(Float... values) {
        super.onProgressUpdate(values);
        if (mListener != null) {
            mListener.onProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(String compressedFilePath) {
        super.onPostExecute(compressedFilePath);
        File imageFile = new File(compressedFilePath);
        float length = imageFile.length() / 1024f; // Size in KB
        String value;
        if (length >= 1024) {
            value = length / 1024f + " MB";
        } else {
            value = length + " KB";
        }
        if (mListener != null) {
            if (imageFile.length() > 0) {
                mListener.onSuccess(imageFile);
            } else {
                mListener.onFail();
            }
        }
        Log.i("Silicompressor", "Path: " + compressedFilePath);
    }

    public interface CompressListener {
        void onCompressStart();

        void onSuccess(File compressedFile);

        void onFail();

        void onProgress(float percent);
    }
}