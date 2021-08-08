package fi.jesunmaailma.tvapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.tvapp.R;
import fi.jesunmaailma.tvapp.adapters.CategoryAdapter;
import fi.jesunmaailma.tvapp.models.Category;
import fi.jesunmaailma.tvapp.services.ChannelDataService;

public class Categories extends AppCompatActivity {
    public static final String TAG = "TAG";
    RecyclerView categoriesList;
    List<Category> categories;
    CategoryAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    ChannelDataService service;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Kaikki kategoriat");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        service = new ChannelDataService(this);

        categoriesList = findViewById(R.id.categories_list);
        categories = new ArrayList<>();

        GridLayoutManager manager = new GridLayoutManager(this, 2);
        categoriesList.setLayoutManager(manager);

        adapter = new CategoryAdapter(categories);
        categoriesList.setAdapter(adapter);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCategoryData("https://jesunmaailma.ml/cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&categories=all&user_id=1");
            }
        });

        getCategoryData("https://jesunmaailma.ml/cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&categories=all&user_id=1");
    }

    public void getCategoryData(String url) {
        service.getChannelData(url, new ChannelDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONObject response) {
                swipeRefreshLayout.setRefreshing(false);

                categories.clear();

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject categoryData = response.getJSONObject(String.valueOf(i));

                        Category category = new Category(
                                categoryData.getInt("id"),
                                categoryData.getString("name"),
                                categoryData.getString("image_url")
                        );

                        Log.d(TAG, "onResponse: " + category.toString());

                        categories.add(category);
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.d(TAG, "onError: " + error);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}