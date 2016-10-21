package com.example.kevin.e_reader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kevin.parser.JSONParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    ListView mListView;
    // A list object to store the parsed contents list
    List<HashMap<String, Object>> contents = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_peta);

        // URL to the JSON data
        //String strUrl = "http://smartkos.hol.es/android/kos.php?min=" +exMin+ "&max=" +exMax+ "&jenis=" +exKat;
        //String strUrl = "http://wptrafficanalyzer.in/p/demo1/first.php/countries";
        String strUrl = "http://68a0192b.ngrok.io/android/konten.php";

        // Creating a new non-ui thread task to download json data
        DownloadTask downloadTask = new DownloadTask();

        // Starting the download process
        downloadTask.execute(strUrl);

        // Getting a reference to ListView of activity_main
        mListView = (ListView) findViewById(R.id.lv_kos);

        // Set new Adapter from listView
        SimpleAdapter adapter = (SimpleAdapter ) mListView.getAdapter();

        // Setting the adapter to the listView
        mListView.setAdapter(adapter);

        // Click Listener + Intent DisplayInfo
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View container, int position, long id) {
                // Getting the Container Layout of the ListView
                //LinearLayout linearLayoutParent = (LinearLayout) container;

                // Getting the inner Linear Layout
                //LinearLayout linearLayoutChild = (LinearLayout ) linearLayoutParent.getChildAt(1);

                // Getting the Content TextView
                //TextView nama = (TextView) linearLayoutChild.getChildAt(0);
                String nama = ((TextView) container.findViewById(R.id.nama)).getText().toString();
                String deskripsi = ((TextView) container.findViewById(R.id.deskripsi)).getText().toString();
                String isi = ((TextView) container.findViewById(R.id.isi)).getText().toString();
                String url = ((TextView) container.findViewById(R.id.url)).getText().toString();
                String gambar_path = ((TextView) container.findViewById(R.id.gambar_path)).getText().toString();

                //Toast.makeText(getBaseContext(), nama.getText().toString(), Toast.LENGTH_SHORT).show();
                Toast.makeText(getBaseContext(), nama, Toast.LENGTH_SHORT).show();
                // Memulai memanggil ke class KontakActivity dengan data
                Intent in = new Intent(getApplicationContext(),SingleItem.class);
                in.putExtra("nama", nama);
                in.putExtra("deskripsi", deskripsi);
                in.putExtra("isi", isi);
                in.putExtra("url", url);
                in.putExtra("gambar_path", gambar_path);
                startActivity(in);
            }
        };
        // Setting the item click listener for the listview
        mListView.setOnItemClickListener(itemClickListener);

    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception download url", e.toString());
        }finally{
            iStream.close();
        }

        return data;
    }

    /** AsyncTask to download json data */
    private class DownloadTask extends AsyncTask<String, Integer, String> {
        String data = null;
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);

            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {

            // The parsing of the xml data is done in a non-ui thread
            ListViewLoaderTask listViewLoaderTask = new ListViewLoaderTask();

            // Start parsing xml data
            listViewLoaderTask.execute(result);

        }
    }

    /** AsyncTask to parse json data and load ListView */
    private class ListViewLoaderTask extends AsyncTask<String, Void, SimpleAdapter>{

        JSONObject jObject;
        // Doing the parsing of xml data in a non-ui thread
        @Override
        protected SimpleAdapter doInBackground(String... strJson) {


            try{
                jObject = new JSONObject(strJson[0]);
                JSONParser jsonParser = new JSONParser();
                jsonParser.parse(jObject);
            }catch(Exception e){
                Log.d("JSON Exception1",e.toString());
            }

            // Instantiating json parser class
            JSONParser jsonParser = new JSONParser();

            //List<HashMap<String, Object>> countries = null;

            try{
                // Getting the parsed data as a List construct
                contents= jsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }

            //NGELEBOKNE LATITUDE LONGITUDE NENG MAPS, COBA LEWAT KENE


            // Keys used in Hashmap
            String[] from = { "nama", "flag", "deskripsi" ,"isi", "url", "gambar_path"
            };

            // Ids of views in listview_layout
            int[] to = { R.id.nama,R.id.gambar,R.id.deskripsi,R.id.isi, R.id.url, R.id.gambar_path
            };

            // Instantiating an adapter to store each items
            // R.layout.listview_layout defines the layout of each item
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), contents, R.layout.listview_layout, from, to);

            return adapter;

        }


        // Invoked by the Android on "doInBackground" is executed
        @Override
        protected void onPostExecute(SimpleAdapter adapter) {

            // Setting adapter for the listview
            mListView.setAdapter(adapter);

            for(int i=0;i<adapter.getCount();i++){
                HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(i);
                String imgUrl = (String) hm.get("gambar_path");
                ImageLoaderTask imageLoaderTask = new ImageLoaderTask();

                HashMap<String, Object> hmDownload = new HashMap<String, Object>();
                hm.put("gambar_path",imgUrl);
                hm.put("position", i);


                // Starting ImageLoaderTask to download and populate image in the listview
                imageLoaderTask.execute(hm);
            }


        }
    }

    // AsyncTask to download and load an image in ListView
    private class ImageLoaderTask extends AsyncTask<HashMap<String, Object>, Void, HashMap<String, Object>>{

        @Override
        protected HashMap<String, Object> doInBackground(HashMap<String, Object>... hm) {

            InputStream iStream=null;
            String imgUrl = (String) hm[0].get("gambar_path");
            int position = (Integer) hm[0].get("position");

            URL url;
            try {
                url = new URL(imgUrl);

                // Creating an http connection to communicate with url
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                // Getting Caching directory
                File cacheDirectory = getBaseContext().getCacheDir();

                // Temporary file to store the downloaded image
                File tmpFile = new File(cacheDirectory.getPath() + "/wpta_"+position+".png");

                // The FileOutputStream to the temporary file
                FileOutputStream fOutStream = new FileOutputStream(tmpFile);

                // Creating a bitmap from the downloaded inputstream
                Bitmap b = BitmapFactory.decodeStream(iStream);

                // Writing the bitmap to the temporary file as png file
                b.compress(Bitmap.CompressFormat.PNG,100, fOutStream);

                // Flush the FileOutputStream
                fOutStream.flush();

                //Close the FileOutputStream
                fOutStream.close();

                // Create a hashmap object to store image path and its position in the listview
                HashMap<String, Object> hmBitmap = new HashMap<String, Object>();

                // Storing the path to the temporary image file
                hmBitmap.put("flag",tmpFile.getPath());

                // Storing the position of the image in the listview
                hmBitmap.put("position",position);

                // Returning the HashMap object containing the image path and position
                return hmBitmap;


            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> result) {
            // Getting the path to the downloaded image
            String path = (String) result.get("flag");

            // Getting the position of the downloaded image
            int position = (Integer) result.get("position");

            // Getting adapter of the listview
            SimpleAdapter adapter = (SimpleAdapter ) mListView.getAdapter();

            // Getting the hashmap object at the specified position of the listview
            HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(position);

            // Overwriting the existing path in the adapter
            hm.put("flag",path);

            // Noticing listview about the dataset changes
            adapter.notifyDataSetChanged();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
