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

import com.google.firebase.analytics.FirebaseAnalytics;

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
    public static final String TAG = "TAG";
    public static final String id = "id";
    public static final String name = "name";
    public static final String image = "image";

    RecyclerView categoryDetailsList;
    ChannelAdapter channelAdapter;
    List<Channel> channelList;
    ChannelDataService channelService;

    SwipeRefreshLayout swipeRefreshLayout;

    FirebaseAnalytics analytics;

    ActionBar actionBar;

    Category category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_details);

        actionBar = getSupportActionBar();

        category = (Category) getIntent().getSerializableExtra("category");

        if (actionBar != null) {
            actionBar.setTitle(category.getName());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        categoryDetailsList = findViewById(R.id.category_details_list);
        channelList = new ArrayList<>();
        channelAdapter = new ChannelAdapter(channelList, "category_details");

        GridLayoutManager manager = new GridLayoutManager(this, 2);
        categoryDetailsList.setLayoutManager(manager);
        categoryDetailsList.setAdapter(channelAdapter);

        channelService = new ChannelDataService(this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDetails();
            }
        });

        getDetails();
    }

    public void getDetails() {
        String url = "https://jesunmaailma.ml/cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&category=" + category.getName() + "&user_id=1";

        channelService.getChannelData(url, new ChannelDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONObject response) {
                swipeRefreshLayout.setRefreshing(false);

                channelList.clear();

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject channelData = response.getJSONObject(String.valueOf(i));

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
                Log.d(TAG, "onErrorResponse: " + error);
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