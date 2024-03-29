package fi.jesunmaailma.tvapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.tvapp.R;
import fi.jesunmaailma.tvapp.adapters.ChannelAdapter;
import fi.jesunmaailma.tvapp.models.Channel;
import fi.jesunmaailma.tvapp.models.Category;
import fi.jesunmaailma.tvapp.services.ChannelDataService;

public class CategoryDetails extends AppCompatActivity {
    public static final String id = "id";
    public static final String name = "name";
    public static final String image = "image";

    CoordinatorLayout clRoot;

    RecyclerView categoryDetailsList;
    ChannelAdapter channelAdapter;
    List<Channel> channelList;
    ChannelDataService channelService;

    SwipeRefreshLayout swipeRefreshLayout;

    ProgressBar pbCategoryDetails;

    CardView errorContainer;

    FirebaseAnalytics analytics;
    FirebaseAuth auth;
    FirebaseUser user;

    Toolbar toolbar;
    ActionBar actionBar;

    Category category;

    TextView tvChannelTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            setTheme(R.style.Theme_TVApp_NoActionBar);
        } else {
            setTheme(R.style.Theme_TVApp_NoActionBar);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_details);

        toolbar = findViewById(R.id.tb_category_details);
        setSupportActionBar(toolbar);

        tvChannelTitle = findViewById(R.id.tv_channel_title);

        actionBar = getSupportActionBar();

        category = (Category) getIntent().getSerializableExtra("category");

        if (actionBar != null) {
            tvChannelTitle.setText(category.getName());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        clRoot = findViewById(R.id.clRoot);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        pbCategoryDetails = findViewById(R.id.pb_category_details);

        errorContainer = findViewById(R.id.error_container);

        pbCategoryDetails.setVisibility(View.VISIBLE);

        analytics = FirebaseAnalytics.getInstance(this);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        categoryDetailsList = findViewById(R.id.category_details_list);
        channelList = new ArrayList<>();
        channelAdapter = new ChannelAdapter(channelList, "category_details", auth, user);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        categoryDetailsList.setLayoutManager(manager);
        categoryDetailsList.setAdapter(channelAdapter);

        channelService = new ChannelDataService(this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pbCategoryDetails.setVisibility(View.VISIBLE);
                getDetails(getResources().getString(R.string.teeveet_prod_api_url) + "?api_key=1A4mgi2rBHCJdqggsYVx&category=" + category.getName() + "&user_id=1");

                categoryDetailsList.setVisibility(View.GONE);
                errorContainer.setVisibility(View.GONE);
            }
        });

        getDetails(getResources().getString(R.string.teeveet_prod_api_url) + "?api_key=1A4mgi2rBHCJdqggsYVx&category=" + category.getName() + "&user_id=1");
    }

    public void getDetails(String url) {

        channelService.getChannelData(url, new ChannelDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONArray response) {
                pbCategoryDetails.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                categoryDetailsList.setVisibility(View.VISIBLE);

                channelList.clear();

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject channelData = response.getJSONObject(i);

                        errorContainer.setVisibility(View.GONE);

                        Channel channel = new Channel();

                        channel.setId(channelData.getInt("id"));
                        channel.setName(channelData.getString("name"));
                        channel.setDescription(channelData.getString("description"));
                        channel.setLiveUrl(channelData.getString("live_url"));
                        channel.setThumbnail(channelData.getString("thumbnail"));
                        channel.setFacebook(channelData.getString("facebook"));
                        channel.setTwitter(channelData.getString("twitter"));
                        channel.setInstagram(channelData.getString("instagram"));
                        channel.setYoutube(channelData.getString("youtube"));
                        channel.setWebsite(channelData.getString("website"));
                        channel.setCategory(channelData.getString("category"));

                        channelList.add(channel);
                        channelAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(String error) {
                pbCategoryDetails.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                errorContainer.setVisibility(View.VISIBLE);

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