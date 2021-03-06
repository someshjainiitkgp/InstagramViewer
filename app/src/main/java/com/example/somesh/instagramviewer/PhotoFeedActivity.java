package com.example.somesh.instagramviewer;

import android.location.Address;
import android.location.Geocoder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PhotoFeedActivity extends ActionBarActivity {

    private final String clientId="b0ddc9bc7657454989b02c70c9b2c9b6";
    private ArrayList<Photo> photoArrayList;
    private PhotoAdapter photoAdapter;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_feed);
        //populatePhotos();

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(false);
                populatePhotos();

            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


    }

    private void populatePhotos() {
        String instagramPhotoStreamUrl = "https://api.instagram.com/v1/media/popular?client_id="+clientId;
        AsyncHttpClient httpClient = new AsyncHttpClient();
        photoArrayList = new ArrayList<Photo>();
        photoAdapter= new PhotoAdapter(this, photoArrayList);
        ListView lvPhotos = (ListView)findViewById(R.id.lvItems);
        lvPhotos.setAdapter(photoAdapter)   ;

        httpClient.get(instagramPhotoStreamUrl, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                JSONArray jsonPhotos = null;

                try{
                    jsonPhotos = response.getJSONArray("data");
                    photoArrayList.clear();
                    for (int i=0;i<jsonPhotos.length();i++){

                        JSONObject jsonPhoto = jsonPhotos.getJSONObject(i);
                        Photo photo = new Photo();
                        photo.setUsername(jsonPhoto.getJSONObject("user").getString("username"));
                        photo.setProfilePictureUrl(jsonPhoto.getJSONObject("user").getString("profile_picture"));
                        if(jsonPhoto.getString("caption")!=null && jsonPhoto.getJSONObject("caption")!=null) photo.setCaption(jsonPhoto.getJSONObject("caption").getString("text"));
                        photo.setPhotoUrl(jsonPhoto.getJSONObject("images").getJSONObject("standard_resolution").getString("url"));
                        photo.setTimeElapsed(Long.valueOf(jsonPhoto.getString("created_time")));
                        photo.setPhotoHeight(jsonPhoto.getJSONObject("images").getJSONObject("standard_resolution").getInt("height"));
                        photo.setLikesCount(jsonPhoto.getJSONObject("likes").getInt("count"));
                        if(jsonPhoto.getString("comments")!=null && jsonPhoto.getJSONObject("comments")!=null){
                            JSONArray jsonComments = jsonPhoto.getJSONObject("comments").getJSONArray("data");
                            ArrayList<Comment> comments = new ArrayList<Comment>();

                            for (int j=0;j<jsonComments.length();j++){
                                JSONObject jsonComment = jsonComments.getJSONObject(j);
                                Comment comment = new Comment(jsonComment.getString("text"), jsonComment.getJSONObject("from").getString("username"),
                                        Long.valueOf(jsonComment.getString("created_time")));
                                comments.add(comment);
                            }

                            Comment recentComment = Comment.getLatestComment(comments);
                            photo.setComment(recentComment);

                        }

                        photoArrayList.add(photo);

                    }
                    photoAdapter.notifyDataSetChanged();
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_feed, menu);
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
