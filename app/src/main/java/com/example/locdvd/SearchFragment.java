package com.example.locdvd;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    EditText searchText;
    Button searchButton;
    ListView searchList;
    String api_key = "e5ce2510d979195e6bc73abc3d6d7dd8" ;


    public static class Movie  {
        public String title ;
        public String releaseDate ;
        public String movieId;
        public String overview;

    }

    ImageLoader imageLoader;
    ImageLoader getImageLoader()  {
        if(imageLoader==null) {
            ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {

                        LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(10);

                        public void putBitmap(String url, Bitmap bitmap) {
                            cache.put(url, bitmap);
                        }

                        public Bitmap getBitmap(String url) {
                            return cache.get(url);
                        }
                    };

            imageLoader = new ImageLoader(getRequestQueue(),imageCache);
        }
        return imageLoader;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup
            container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_search, null);
        searchList = (ListView)view.findViewById(R.id.search_List);

        searchText =(EditText)view.findViewById(R.id.search_queryText);
        searchButton=(Button)view.findViewById(R.id.search_queryLaunch);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSearch();
            }
        });

        return view;
    }

    RequestQueue requestQueue;
    RequestQueue getRequestQueue() {
        if(requestQueue==null)
            requestQueue = Volley.newRequestQueue(getActivity());
        return requestQueue;
    }

    private void launchSearch()  {
        try {
            //String api_key="e5ce2510d979195e6bc73abc3d6d7dd8";
            String title = URLEncoder.encode(searchText.getText().toString(),"UTF-8" );
            String url= String.format("https://api.themoviedb.org/3/search/movie?api_key=%s&query=%s&language=fr-FR", api_key,title);
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    jsonRequestListener ,
                    errorListener);
            getRequestQueue().add(request);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<JSONObject> jsonRequestListener =
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        ArrayList<Movie> listOfMovies = new ArrayList<>();
                        JSONArray jsonArray =
                                response.getJSONArray("results");
                        for (int i =0;i<jsonArray.length();i++) {
                            JSONObject jsonObject =
                                    jsonArray.getJSONObject(i);
                            Movie movie = new Movie();
                            movie.title = jsonObject.getString("title");
                            movie.releaseDate =
                                    jsonObject.getString("release_date");
                            movie.movieId = jsonObject.getString("id");
                            movie.overview =
                                    jsonObject.getString("overview");
                            listOfMovies.add(movie);
                        }

                        SearchListAdapter searchListAdapter =
                                new
                                        SearchListAdapter(getActivity(),listOfMovies);
                        searchList.setAdapter(searchListAdapter);


                    } catch (JSONException e) {
                        Log.e("JSON",e.getLocalizedMessage());
                    }
                }
            };
    private Response.ErrorListener errorListener =
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Recherche","Erreur " + error.getMessage());
                }
            };



    class SearchListAdapter extends ArrayAdapter<Movie> {

        Context context;
        public SearchListAdapter(Context context,  List< Movie > movies) {
            super(context, R.layout.listitem_movie, movies);
            this.context = context;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            View view=null;
            if(convertView==null) {
                LayoutInflater layoutInflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.listitem_movie,null);
            } else {
                view = convertView;
            }

            final Movie movie = getItem(pos);
            view.setTag(movie);

            TextView titre =(TextView)view.findViewById(R.id.movie_title);
            TextView dateSortie = (TextView)view.findViewById(R.id.movie_releaseDate);
            final Button detailButton=(Button)view.findViewById(R.id.movie_detail);
            Button closeButton= (Button)view.findViewById(R.id.movie_closeDetail);
            final RelativeLayout detailLayout =  (RelativeLayout)view.findViewById(R.id.movie_detailLayout);
            final NetworkImageView detailPoster = (NetworkImageView)view.findViewById(R.id.movie_poster);
            final TextView detailPlot = (TextView)view.findViewById(R.id.movie_plot);
            detailButton.setVisibility(View.VISIBLE);
            detailLayout.setVisibility(View.GONE);

            titre.setText(movie.title);
            dateSortie.setText(movie.releaseDate);

            detailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detailLayout.setVisibility(View.VISIBLE);
                    detailButton.setVisibility(View.GONE);

                    //String url = String.format("https://api.themoviedb.org/3/movie/%s?api_key=[API_KEY]&language=fr-FR", movie.movieId);
                    String url = String.format("https://api.themoviedb.org/3/movie/%s?api_key="+api_key+"&language=fr-FR", movie.movieId);
                    JsonObjectRequest jsonObjectRequest;
                    jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String posterPath  = response.getString("poster_path");
                                        String plot = response.getString("overview");
                                        detailPlot.setText(plot);

                                        String url ="https://image.tmdb.org/t/p/w500/" + posterPath;

                                        detailPoster.setImageUrl(url, getImageLoader());
                                    } catch (JSONException e) {
                                        Log.e("JSON", e.getLocalizedMessage());
                                    }

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("DETAIL", error.getLocalizedMessage());
                                }
                            }
                    );
                    getRequestQueue().add(jsonObjectRequest);
                }
            });

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detailLayout.setVisibility(View.GONE);
                    detailButton.setVisibility(View.VISIBLE);
                }
            });

            titre.setText(movie.title);
            dateSortie.setText(movie.releaseDate);

            return view;
        }

    }
}
