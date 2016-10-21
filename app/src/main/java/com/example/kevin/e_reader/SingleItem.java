package com.example.kevin.e_reader;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Kevin on 10/21/2016.
 */
public class SingleItem extends Activity{
    // JSON node keys
    private static final String TAG_NAME = "nama";
    private static final String TAG_DESC = "deskripsi";
    private static final String TAG_ISI = "isi";
    private static final String TAG_URL = "url";
    ProgressBar pb;
    Dialog dialog;
    int downloadedSize = 0;
    int totalSize = 0;
    TextView cur_val;
    public static String tempDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_item);

// getting intent data
        Intent in = getIntent();

// Get JSON values from previous intent
        //String name = in.getStringExtra("my tag");
        final String name = in.getStringExtra(TAG_NAME);
        final String desc = in.getStringExtra(TAG_DESC);
        final String isi = in.getStringExtra(TAG_ISI);
        final String url = in.getStringExtra(TAG_URL);

// Displaying all values on the screen
        TextView lblName = (TextView) findViewById(R.id.name);
        TextView lblDesc = (TextView) findViewById(R.id.description);
        TextView lblIsi = (TextView) findViewById(R.id.isi);

        lblName.setText(name);
        lblDesc.setText(desc);
        lblIsi.setText(isi);

        tempDir = Environment.getExternalStorageDirectory() + "/" + "E-Reader" + "/";

        prepareDirectory();

        Button b = (Button) findViewById(R.id.b1);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(url);

                new Thread(new Runnable() {
                    public void run() {
                        downloadFile();
                    }
                }).start();
            }
        });

    }

    void downloadFile(){

        try {
            // getting intent data
            Intent in = getIntent();

            // Get JSON values from previous intent
            //String name = in.getStringExtra("my tag");
            final String name = in.getStringExtra(TAG_NAME);
            final String urlin = in.getStringExtra(TAG_URL);

            URL url = new URL(urlin);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            //connect
            urlConnection.connect();

            //set the path where we want to save the file
            //File SDCardRoot = Environment.getExternalStorageDirectory();
            //create a new file, to save the downloaded file
            final File file = new File(tempDir,name + ".pdf");

            FileOutputStream fileOutput = new FileOutputStream(file);

            //Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //this is the total size of the file which we are downloading
            totalSize = urlConnection.getContentLength();

            runOnUiThread(new Runnable() {
                public void run() {
                    pb.setMax(totalSize);
                }
            });

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                // update the progressbar //
                runOnUiThread(new Runnable() {
                    public void run() {
                        pb.setProgress(downloadedSize);
                        float per = ((float)downloadedSize/totalSize) * 100;
                        cur_val.setText("Downloaded " + downloadedSize + "KB / " + totalSize + "KB (" + (int)per + "%)" );
                    }
                });
            }
            //close the output stream when complete //
            fileOutput.close();
            runOnUiThread(new Runnable() {
                public void run() {
                    // pb.dismiss(); // if you want close it..

                    // Intent to view download PDF
                    //Uri uri  = Uri.fromFile(myActivity.getFileStreamPath(mFileName));
                    Uri uri = Uri.fromFile(file);

                    try
                    {
                        Intent intentUrl = new Intent(Intent.ACTION_VIEW);
                        intentUrl.setDataAndType(uri, "application/pdf");
                        intentUrl.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intentUrl);
                    }
                    catch (ActivityNotFoundException e)
                    {
                        //Toast.makeText(SingleItem, "No PDF Viewer Installed", Toast.LENGTH_LONG)
                    }
                }
            });

        } catch (final MalformedURLException e) {
            showError("Error : MalformedURLException " + e);
            e.printStackTrace();
        } catch (final IOException e) {
            showError("Error : IOException " + e);
            e.printStackTrace();
        }
        catch (final Exception e) {
            showError("Error : Please check your internet connection " + e);
        }
    }

    void showError(final String err){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(SingleItem.this, err, Toast.LENGTH_LONG).show();
            }
        });
    }

    void showProgress(String file_path){
        dialog = new Dialog(SingleItem.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.myprogressdialog);
        dialog.setTitle("Download Progress");

        TextView text = (TextView) dialog.findViewById(R.id.tv1);
        text.setText("Downloading file from ... " + file_path);
        cur_val = (TextView) dialog.findViewById(R.id.cur_pg_tv);
        cur_val.setText("Starting download...");
        dialog.show();

        pb = (ProgressBar)dialog.findViewById(R.id.progress_bar);
        pb.setProgress(0);
        pb.setProgressDrawable(getResources().getDrawable(R.drawable.green_progress));
    }

    private boolean prepareDirectory()
    {
        try
        {
            if (makedirs())
            {
                return true;
            } else {
                return false;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Could not initiate File System.. Is Sdcard mounted properly?", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean makedirs()
    {
        File tempdir = new File(tempDir);

        //cek direktori sudah ada atau belum
        if (!tempdir.exists())
            tempdir.mkdirs();

        return (tempdir.isDirectory());
    }
}
