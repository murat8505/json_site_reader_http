package com.teamtreehouse.ribbit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class NewsFragment extends ListFragment{
	
	public static final int NUMBER_OF_POSTS = 10;
	public static final String TAG = NewsFragment.class.getSimpleName();
	protected JSONObject mBlogData;
	protected ProgressBar mProgressBar;
	
	private final String KEY_TITLE = "title";
	private final String KEY_DATE = "date";
	private final String KEY_THUMBNAIL = "thumbnail";
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news_list,
                container, false);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar1);

        if(NetworkIsAvailable())    {
            mProgressBar.setVisibility(View.VISIBLE);
            GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
            getBlogPostsTask.execute();
        }
        else {
            Toast.makeText(getActivity(), "Network is unavailable!", Toast.LENGTH_LONG).show();
        }

        //String message = getString(R.string.no_items);
        //Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        return rootView;
    }
	
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	try {
    	JSONArray jsonPosts = mBlogData.getJSONArray("posts");
    	JSONObject jsonPost = jsonPosts.getJSONObject(position);
    	String blogUrl = jsonPost.getString("url");
    	
    	Intent intent = new Intent(getActivity(),BlogWebViewActivity.class);
    	intent.setData(Uri.parse(blogUrl));
    	startActivity(intent);
    }
    	catch (JSONException e) {
    		logException(e);
    	}
    }

	private void logException(Exception e) {
		Log.e(TAG, "Exception caught!", e);
	}

    private boolean NetworkIsAvailable() {
		ConnectivityManager manager = (ConnectivityManager) 
		getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		
		boolean isAvailable = false;
		if(networkInfo != null && networkInfo.isConnected()) {
			isAvailable = true;
		}
		return isAvailable;
	}

	public void handleBlogResponse() {
		mProgressBar.setVisibility(View.INVISIBLE);
		if (mBlogData == null) {
			updateDisplayForError();
		}
		else { 
			try {
				JSONArray jsonPosts = mBlogData.getJSONArray("posts");
				ArrayList<HashMap<String, String>> blogPosts = new ArrayList<HashMap<String, String>>();
				for (int i = 0; i < jsonPosts.length(); i++) {
					JSONObject post = jsonPosts.getJSONObject(i);
					String title = post.getString(KEY_TITLE);
					title = Html.fromHtml(title).toString();
					String date = post.getString(KEY_DATE);
					date = Html.fromHtml(date).toString();
					String thumbnail = post.getString(KEY_THUMBNAIL);
					thumbnail = Html.fromHtml(thumbnail).toString();
					
					HashMap<String, String> blogPost = new HashMap<String, String>();
					blogPost.put(KEY_TITLE, title);
					blogPost.put(KEY_DATE, date);
					blogPost.put(KEY_THUMBNAIL, thumbnail);
					
					blogPosts.add(blogPost);
				}
				String[] keys = { KEY_TITLE, KEY_DATE, KEY_THUMBNAIL };
				int[] ids = { android.R.id.text1, android.R.id.text2 };
				SimpleAdapter adapter = new SimpleAdapter(getActivity(), blogPosts, android.R.layout.simple_list_item_2, keys, ids);
				setListAdapter(adapter);
			} catch (JSONException e) {
				logException(e);
			}
		}
		
	}


	private void updateDisplayForError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); 	
		builder.setTitle(getString(R.string.error_title));
		builder.setMessage(getString(R.string.error_message));
		builder.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
		
		TextView emptyTextView = (TextView) getListView().getEmptyView();
		emptyTextView.setText(getString(R.string.no_items));
	}
    
    private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject> {

    	@Override
    	protected JSONObject doInBackground(Object... params) {
    	    int responseCode = -1;
    	    JSONObject jsonResponse = null;
    	    StringBuilder builder = new StringBuilder();
    	    HttpClient client = new DefaultHttpClient();
    	    HttpGet httpget = new HttpGet("http://www.ilovelimerick.ie/?json=1&count=10");

    	    try {
    	        HttpResponse response = client.execute(httpget);
    	        StatusLine statusLine = response.getStatusLine();
    	        responseCode = statusLine.getStatusCode();

    	        if (responseCode == HttpURLConnection.HTTP_OK) {
    	            HttpEntity entity = response.getEntity();
    	            InputStream content = entity.getContent();
    	            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
    	            String line;
    	            while((line = reader.readLine()) != null){
    	                builder.append(line);
    	            }

    	            jsonResponse = new JSONObject(builder.toString());
    	        }
    	        else {
    	            Log.i(TAG, String.format("Unsuccessful HTTP response code: %d", responseCode));
    	        }
    	    }
    	    catch (JSONException e) {
    	        logException(e);
    	    }
    	    catch (Exception e) {
    	        logException(e);
    	    }           

    	    return jsonResponse;
    	}   
		
		@Override
		protected void onPostExecute(JSONObject result) {
			mBlogData = result;
			handleBlogResponse();
		}
    	
    }
}
