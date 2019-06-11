package com.example.carriboulou;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 2;
    VideoView videoView;
    ImageView iv;
    int counter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

        dispatchTakeVideoIntent();
        videoView = (VideoView) findViewById(R.id.vv);
        iv = (ImageView) findViewById(R.id.iv);
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            final Uri videoUri = intent.getData();

            videoView.setVideoURI(videoUri);
            videoView.start();
            Log.e("ERIC", videoUri.getPath());

            //iv.setImageAlpha(0); // 0 = transparent
            //iv.setVisibility(View.INVISIBLE);

            videoView.setVisibility(View.INVISIBLE);

            getFrame(videoUri, counter);
            counter++;

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            Log.i("tag", "This'll run 2000 milliseconds later");
                            getFrame(videoUri, counter);
                        }
                    },
                    2000);
        }
    }

    void getFrame(Uri uri, int seconds)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(this, uri);

            Bitmap test = retriever.getFrameAtTime(seconds*1000000,MediaMetadataRetriever.OPTION_CLOSEST);
            iv.setImageBitmap(test);

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
    }
}