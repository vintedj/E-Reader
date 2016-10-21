package com.example.kevin.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kevin on 10/21/2016.
 */
public class JSONParser {
    List<HashMap<String, Object>> contentList = new ArrayList<HashMap<String,Object>>();
    HashMap<String, Object> content = null;
    // Receives a JSONObject and returns a list
    public List<HashMap<String,Object>> parse(JSONObject jObject){

        JSONArray jContents = null;
        try {
            // Retrieves all the elements in the 'konten' array
            jContents = jObject.getJSONArray("konten");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Invoking getContent with the array of json object
        // where each json object represent a content
        return getContents(jContents);
    }


    private List<HashMap<String, Object>> getContents(JSONArray jContents){
        int contentCount = jContents.length();

        // Taking each content, parses and adds to list object
        for(int i=0; i<contentCount;i++){
            try {
                // Call getContent with content JSON object to parse the content
                content = getContent((JSONObject)jContents.get(i));
                contentList.add(content);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return contentList;
    }

    // Parsing the Content JSON object
    private HashMap<String, Object> getContent(JSONObject jContent){

        HashMap<String, Object> content = new HashMap<String, Object>();
        String nama = "";
        String deskripsi  = "";
        String isi = "";
        String gambar_path = "";
        String url = "";


        try {
            nama = jContent.getString("nama");
            deskripsi  = jContent.getString("deskripsi");
            isi = jContent.getString("isi");
            gambar_path = jContent.getString("gambar");
            url = jContent.getString("url");

            content.put("nama", nama);
            content.put("deskripsi", deskripsi);
            content.put("isi", isi);
            content.put("gambar_path", gambar_path);
            content.put("url", url);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return content;
    }
}
