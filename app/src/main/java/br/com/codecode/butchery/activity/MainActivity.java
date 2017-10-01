package br.com.codecode.butchery.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import br.com.codecode.butchery.R;
import br.com.codecode.butchery.adapter.GalleryAdapter;
import br.com.codecode.butchery.app.AppController;
import br.com.codecode.butchery.model.Image;

public class MainActivity extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();

    private static final String ENDPOINT = "http://www.codecode.com.br/json/butchery.json";

    private ArrayList<Image> images;

    private ProgressDialog pDialog;

    private GalleryAdapter mAdapter;

    private RecyclerView recyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        context = getApplicationContext();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        pDialog = new ProgressDialog(this);

        images = new ArrayList<>();

        mAdapter = new GalleryAdapter(context, images);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 2);

        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("Swipe", "on Refresh event called");
                fetchImages();
            }
        });

        recyclerView.addOnItemTouchListener(new GalleryAdapter.RecyclerTouchListener(context,
                recyclerView, new GalleryAdapter.ClickListener() {

            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("images", images);
                bundle.putInt("position", position);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {
                Toast.makeText(context,"Clique longo! Não faço nada!",Toast.LENGTH_LONG).show();
            }
        }));

        fetchImages();
    }

    private void fetchImages() {

        pDialog.setMessage("Descarregando carnes...");
        pDialog.show();
        // Signal SwipeRefreshLayout to start the progress indicator
        swipeRefreshLayout.setRefreshing(true);

        JsonArrayRequest req = new JsonArrayRequest(ENDPOINT,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        pDialog.hide();

                        images.clear();

                        for (int i = 0; i < response.length(); i++) {

                            try {

                                JSONObject object = response.getJSONObject(i);

                                Image image = new Image();

                                image.setName(object.getString("name"));

                                image.setPrice(object.getDouble("price"));

                                JSONObject url = object.getJSONObject("url");

                                image.setSmall(url.getString("small"));

                                image.setMedium(url.getString("medium"));

                                image.setLarge(url.getString("large"));

                                image.setTimestamp(object.getString("timestamp"));

                                images.add(image);

                            } catch (JSONException e) {
                                Log.e(TAG, "Json parsing error: " + e.getMessage());
                            }
                        }

                        mAdapter.notifyDataSetChanged();

                        // Signal SwipeRefreshLayout to start the progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e(TAG, "Error: " + error.getMessage());

                pDialog.hide();

                // Signal SwipeRefreshLayout to start the progress indicator
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }
}