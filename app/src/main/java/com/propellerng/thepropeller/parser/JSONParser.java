package com.propellerng.thepropeller.parser;

import android.util.Log;

import com.propellerng.thepropeller.R;
import com.propellerng.thepropeller.util.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ESIDEM jnr on 5/29/2017.
 */

public class JSONParser {
    private static final String	TAG	= "JSONParser";



    /**
     * Parse JSON data and return an ArrayList of Category objects
     *
     * @param jsonObject JSON data
     * @return A list of Category objects
     */

    /**
     * Parse JSON data and return an ArrayList of Post objects
     *
     * @param jsonObject JSON data
     * @return A list of Post objects
     */
    public static ArrayList<Post> parsePosts(JSONObject jsonObject) {
        ArrayList<Post> posts = new ArrayList<>();

        try{
            JSONArray postArray = jsonObject.getJSONArray("posts");
            // JSONArray categoryArray = jsonObject.getJSONArray("categories");
            // Go through each post
            for (int i = 0; i < postArray.length(); i++) {
                JSONObject postObject = postArray.getJSONObject(i);

                Post post = new Post();
                // Configure the Post object
                post.setTitle(postObject.optString("title", "N/A"));
                // Use a default thumbnail if one doesn't exist
                post.setThumbnailUrl(postObject.optString("thumbnail",
                        Config.DEFAULT_THUMBNAIL_URL));
                post.setCommentCount(postObject.optInt("comment_count", 0));
                //post.setViewCount(postObject.getJSONObject("custom_fields")
                //        .getJSONArray("post_views_count").getString(0));
                //  post.getCategories(postObject.getJSONObject("categories").getJSONArray("title"))

                post.setDate(postObject.optString("date", "N/A"));
                post.setContent(postObject.optString("content", "N/A"));
                post.setAuthor(postObject.getJSONObject("author").optString("name", "N/A"));
                post.setId(postObject.optInt("id"));
                post.setUrl(postObject.optString("url"));
                post.getCategories();

                JSONObject featuredImages = postObject.optJSONObject("thumbnail_images");
                if (featuredImages != null) {
                    post.setFeaturedImageUrl(featuredImages.optJSONObject("medium_large")
                            .optString("url", Config.DEFAULT_THUMBNAIL_URL));
                }

                posts.add(post);
            }
        } catch (JSONException e) {
            Log.d(TAG, "----------------- Json Exception");
            Log.d(TAG, e.getMessage());
            return null;
        }

        return posts;
    }

//    private ArrayList<Category> getPostDescription(JSONObject jsonObject){
//
//        ArrayList<Category> categoryArrayList = new ArrayList<>();
//
//        categoryArrayList = parseCategories(jsonObject);
//
//        return categoryArrayList;
//
//
//    }

}
