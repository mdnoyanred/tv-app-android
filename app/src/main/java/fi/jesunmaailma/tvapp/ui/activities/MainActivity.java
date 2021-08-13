package fi.jesunmaailma.tvapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    FirebaseAuth auth;
    FirebaseUser user;

    GoogleSignInClient client;

    CoordinatorLayout clRoot;

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

        clRoot = findViewById(R.id.clRoot);

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
        
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        client = GoogleSignIn.getClient(MainActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);

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

        UpdateNavHeader();
    }

    public void UpdateNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.tv_name);
        TextView tvEmail = headerView.findViewById(R.id.tv_email);
        TextView tvSignIn = headerView.findViewById(R.id.tv_login);
        TextView tvSignOut = headerView.findViewById(R.id.tv_sign_out);

        if (user != null) {
            tvName.setVisibility(View.VISIBLE);
            tvEmail.setVisibility(View.VISIBLE);
            tvSignIn.setVisibility(View.GONE);
            tvSignOut.setVisibility(View.VISIBLE);

            tvName.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());
        } else {
            tvName.setVisibility(View.GONE);
            tvEmail.setVisibility(View.GONE);
            tvSignIn.setVisibility(View.VISIBLE);
            tvSignOut.setVisibility(View.GONE);
        }

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer(drawerLayout);
                Intent intent = new Intent(getApplicationContext()
                        , Login.class);
                startActivityForResult(intent, 100);
            }
        });

        tvSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer(drawerLayout);
                SignOutDialog(MainActivity.this);
            }
        });
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onStart() {
        if (user == null) {
            Snackbar snackbar = Snackbar.make(clRoot
                    , ""
                    , Snackbar.LENGTH_LONG);

            View snackBarView = getLayoutInflater().inflate(R.layout.layout_snackbar_logged_out, null);

            snackbar.getView().setBackgroundColor(Color.TRANSPARENT);

            Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();

            snackbarLayout.setPadding(0, 0, 0, 0);

            snackbarLayout.addView(snackBarView, 0);

            snackbar.setDuration(5000);
            snackbar.show();
        } else {
            Snackbar snackbar = Snackbar.make(clRoot
                    , ""
                    , Snackbar.LENGTH_LONG);

            View snackBarView = getLayoutInflater().inflate(R.layout.layout_snackbar_logged_in, null);

            snackbar.getView().setBackgroundColor(Color.TRANSPARENT);

            Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();

            snackbarLayout.setPadding(0, 0, 0, 0);

            TextView tvGreeting = snackBarView.findViewById(R.id.tv_greeting);
            TextView tvEmail = snackBarView.findViewById(R.id.tv_email);

            tvGreeting.setText(String.format("Hei %s!", user.getDisplayName()));
            tvEmail.setText(String.format("(%s)", user.getEmail()));

            snackbarLayout.addView(snackBarView, 0);

            snackbar.setDuration(5000);
            snackbar.show();
        }
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            closeDrawer(drawerLayout);
            startActivity(new Intent(getApplicationContext(), MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            overridePendingTransition(0, 0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void SignOutDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle(user.getDisplayName());
        builder.setMessage("Haluatko varmasti kirjautua ulos sovelluksesta?");
        builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Kirjaudu ulos", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                auth.signOut();

                client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(getApplicationContext()
                                , MainActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                    }
                });
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mi_home) {
            closeDrawer(drawerLayout);
        }

        if (item.getItemId() == R.id.mi_categories) {
            closeDrawer(drawerLayout);
            startActivity(new Intent(getApplicationContext(), Categories.class));
        }

        if (item.getItemId() == R.id.mi_exit) {
            closeDrawer(drawerLayout);
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

        if (item.getItemId() == R.id.mi_settings) {
            closeDrawer(drawerLayout);
            startActivity(new Intent(getApplicationContext(), Settings.class));
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