package fi.jesunmaailma.tvapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.tvapp.R;
import fi.jesunmaailma.tvapp.adapters.ChannelAdapter;
import fi.jesunmaailma.tvapp.models.Channel;
import fi.jesunmaailma.tvapp.services.ChannelDataService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    public static final String TAG = "TAG";
    RecyclerView bigSliderList, newsChannelList, sportsChannelList, enterChannelList;
    ChannelAdapter bigSliderAdapter, newsChannelAdapter, sportsChannelAdapter, enterChannelAdapter;
    List<Channel> channelList, newsChannels, sportsChannel, enterChannel;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout;

    ProgressBar pbLoading;

    ChannelDataService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        channelList = new ArrayList<>();

        bigSliderList = findViewById(R.id.big_slider_list);

        service = new ChannelDataService(this);

        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        pbLoading = findViewById(R.id.pbLoading);

        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );

        toggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        bigSliderList.setLayoutManager(manager);

        bigSliderAdapter = new ChannelAdapter(channelList, "slider");
        bigSliderList.setAdapter(bigSliderAdapter);

        pbLoading.setVisibility(View.VISIBLE);

        getSliderData("https://jesunmaailma.ml/cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&channels=all&user_id=1");
        getNewsChannels("https://jesunmaailma.ml/cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&category=Uutiset&user_id=1");
        getSportsChannel("https://jesunmaailma.ml/cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&category=Urheilu&user_id=1");
        getEntertainmentChannel("https://jesunmaailma.ml/cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&category=Viihde&user_id=1");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pbLoading.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);

                bigSliderList.setVisibility(View.GONE);
                getSliderData("https://jesunmaailma.ml/cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&channels=all&user_id=1");

                newsChannelList.setVisibility(View.GONE);
                getNewsChannels("https://jesunmaailma.ml/cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&category=Uutiset&user_id=1");

                sportsChannelList.setVisibility(View.GONE);
                getSportsChannel("https://jesunmaailma.ml/cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&category=Urheilu&user_id=1");

                enterChannelList.setVisibility(View.GONE);
                getEntertainmentChannel("https://jesunmaailma.ml/cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&category=Viihde&user_id=1");
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mi_home) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        if (item.getItemId() == R.id.mi_categories) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(getApplicationContext(), Categories.class));
        }

        if (item.getItemId() == R.id.mi_exit) {
            drawerLayout.closeDrawer(GravityCompat.START);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(false);
            builder.setMessage("Haluatko varmasti poistua TV App-palvelusta?");
            builder.setNegativeButton("Kyllä", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setPositiveButton("Ei", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return false;
    }

    public void getSliderData(String url) {
        service.getChannelData(url, new ChannelDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONObject response) {

                pbLoading.setVisibility(View.GONE);
                bigSliderList.setVisibility(View.VISIBLE);

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
                        channel.setYoutube(channelData.getString("youtube"));
                        channel.setWebsite(channelData.getString("website"));
                        channel.setCategory(channelData.getString("category"));

                        channelList.add(channel);
                        bigSliderAdapter.notifyDataSetChanged();

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

    public void getNewsChannels(String url) {
        newsChannelList = findViewById(R.id.news_channel_list);
        newsChannels = new ArrayList<>();

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        newsChannelList.setLayoutManager(manager);

        newsChannelAdapter = new ChannelAdapter(newsChannels, "category");
        newsChannelList.setAdapter(newsChannelAdapter);

        service.getChannelData(url, new ChannelDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONObject response) {

                pbLoading.setVisibility(View.GONE);
                newsChannelList.setVisibility(View.VISIBLE);

                newsChannels.clear();

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
                        channel.setYoutube(channelData.getString("youtube"));
                        channel.setWebsite(channelData.getString("website"));
                        channel.setCategory(channelData.getString("category"));

                        newsChannels.add(channel);
                        newsChannelAdapter.notifyDataSetChanged();

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

    public void getSportsChannel(String url) {
        sportsChannelList = findViewById(R.id.sports_channel_list);
        sportsChannel = new ArrayList<>();

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        sportsChannelList.setLayoutManager(manager);

        sportsChannelAdapter = new ChannelAdapter(sportsChannel, "category");
        sportsChannelList.setAdapter(sportsChannelAdapter);

        service.getChannelData(url, new ChannelDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONObject response) {

                pbLoading.setVisibility(View.GONE);
                sportsChannelList.setVisibility(View.VISIBLE);

                sportsChannel.clear();

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
                        channel.setYoutube(channelData.getString("youtube"));
                        channel.setWebsite(channelData.getString("website"));
                        channel.setCategory(channelData.getString("category"));

                        sportsChannel.add(channel);
                        sportsChannelAdapter.notifyDataSetChanged();

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

    public void getEntertainmentChannel(String url) {
        enterChannelList = findViewById(R.id.enter_channel_list);
        enterChannel = new ArrayList<>();

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        enterChannelList.setLayoutManager(manager);

        enterChannelAdapter = new ChannelAdapter(enterChannel, "category");
        enterChannelList.setAdapter(enterChannelAdapter);

        service.getChannelData(url, new ChannelDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONObject response) {

                pbLoading.setVisibility(View.GONE);
                enterChannelList.setVisibility(View.VISIBLE);

                enterChannel.clear();

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
                        channel.setYoutube(channelData.getString("youtube"));
                        channel.setWebsite(channelData.getString("website"));
                        channel.setCategory(channelData.getString("category"));

                        enterChannel.add(channel);
                        enterChannelAdapter.notifyDataSetChanged();

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
}