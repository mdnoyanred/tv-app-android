package fi.jesunmaailma.tvapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.tvapp.R;
import fi.jesunmaailma.tvapp.adapters.CategoryAdapter;
import fi.jesunmaailma.tvapp.models.Category;
import fi.jesunmaailma.tvapp.services.ChannelDataService;

public class Categories extends AppCompatActivity {
    public static final String id = "id";
    public static final String name = "name";
    public static final String image = "image";

    CoordinatorLayout clRoot;

    RecyclerView categoriesList;
    List<Category> categories;
    CategoryAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    ProgressBar pbCategories;

    CardView errorContainer;

    FirebaseAnalytics analytics;

    ChannelDataService service;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            setTheme(R.style.Theme_TVApp);
        } else {
            setTheme(R.style.Theme_TVApp);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Kategoriat");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        clRoot = findViewById(R.id.clRoot);

        errorContainer = findViewById(R.id.error_container);

        service = new ChannelDataService(this);

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        categoriesList = findViewById(R.id.categories_list);
        categories = new ArrayList<>();

        LinearLayoutManager manager = new LinearLayoutManager(this);
        categoriesList.setLayoutManager(manager);

        adapter = new CategoryAdapter(categories);
        categoriesList.setAdapter(adapter);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        pbCategories = findViewById(R.id.pb_categories);

        pbCategories.setVisibility(View.VISIBLE);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pbCategories.setVisibility(View.VISIBLE);
                getCategoryData(getResources().getString(R.string.teeveet_prod_api_url) + "?api_key=1A4mgi2rBHCJdqggsYVx&categories=all&user_id=1");

                errorContainer.setVisibility(View.GONE);
                categoriesList.setVisibility(View.GONE);
            }
        });

        getCategoryData(getResources().getString(R.string.teeveet_prod_api_url) + "?api_key=1A4mgi2rBHCJdqggsYVx&categories=all&user_id=1");
    }

    public void getCategoryData(String url) {
        service.getChannelData(url, new ChannelDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONObject response) {
                pbCategories.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                categoriesList.setVisibility(View.VISIBLE);

                categories.clear();

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject categoryData = response.getJSONObject(String.valueOf(i));

                        Category category = new Category();

                        category.setId(categoryData.getInt("id"));
                        category.setName(categoryData.getString("name"));
                        category.setImageUrl(categoryData.getString("image_url"));

                        categories.add(category);
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(String error) {
                pbCategories.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                errorContainer.setVisibility(View.VISIBLE);
                categoriesList.setVisibility(View.GONE);

                Snackbar snackbar = Snackbar.make(clRoot
                        , ""
                        , Snackbar.LENGTH_LONG);

                View snackBarView = getLayoutInflater().inflate(R.layout.layout_snackbar_error, null);

                snackbar.getView().setBackgroundColor(Color.TRANSPARENT);

                Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();

                snackbarLayout.setPadding(0, 0, 0, 0);

                TextView tvError = snackBarView.findViewById(R.id.tv_error);

                tvError.setText(error);

                snackbarLayout.addView(snackBarView, 0);

                snackbar.setDuration(5000);
                snackbar.show();
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