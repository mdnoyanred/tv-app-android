package fi.jesunmaailma.tvapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.tvapp.R;
import fi.jesunmaailma.tvapp.adapters.ChannelAdapter;
import fi.jesunmaailma.tvapp.models.Channel;
import fi.jesunmaailma.tvapp.services.ChannelDataService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "TAG";
    public static final String id = "id";
    public static final String name = "name";
    public static final String image = "image";

    RecyclerView bigSliderList, newsChannelList, enterChannelList;
    ChannelAdapter bigSliderAdapter, newsChannelAdapter, enterChannelAdapter;
    List<Channel> channelList, newsChannels, enterChannel;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout;

    CardView newsChannelContainer, enterChannelContainer, errorContainer;
    TextView tvError;

    ProgressBar pbLoading;

    ChannelDataService service;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore database;
    FirebaseAnalytics analytics;
    DocumentReference documentReference;

    GoogleSignInClient client;

    CoordinatorLayout clRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            setTheme(R.style.Theme_TVApp_NoActionBar);
        } else {
            setTheme(R.style.Theme_TVApp_NoActionBar);
        }

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

        newsChannelContainer = findViewById(R.id.news_channel_container);
        enterChannelContainer = findViewById(R.id.enter_channel_container);
        errorContainer = findViewById(R.id.error_container);

        tvError = findViewById(R.id.tv_error);

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
        database = FirebaseFirestore.getInstance();

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        client = GoogleSignIn.getClient(MainActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        bigSliderList.setLayoutManager(manager);

        bigSliderAdapter = new ChannelAdapter(channelList, "slider");
        bigSliderList.setAdapter(bigSliderAdapter);

        pbLoading.setVisibility(View.VISIBLE);

        getSliderData("https://jesunmaailma.ml/livetv-cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&channels=all&user_id=1");
        getNewsChannels("https://jesunmaailma.ml/livetv-cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&category=Uutiset&user_id=1");
        getEntertainmentChannel("https://jesunmaailma.ml/livetv-cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&category=Viihde&user_id=1");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pbLoading.setVisibility(View.VISIBLE);
                errorContainer.setVisibility(View.GONE);

                bigSliderList.setVisibility(View.GONE);
                getSliderData("https://jesunmaailma.ml/livetv-cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&channels=all&user_id=1");

                newsChannelContainer.setVisibility(View.GONE);
                newsChannelList.setVisibility(View.GONE);
                getNewsChannels("https://jesunmaailma.ml/livetv-cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&category=Uutiset&user_id=1");

                enterChannelContainer.setVisibility(View.GONE);
                enterChannelList.setVisibility(View.GONE);
                getEntertainmentChannel("https://jesunmaailma.ml/livetv-cms/api.php?api_key=1A4mgi2rBHCJdqggsYVx&category=Viihde&user_id=1");
            }
        });

        UpdateNavHeader();

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
            documentReference = database.collection("Users").document(user.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        Snackbar snackbar = Snackbar.make(clRoot
                                , ""
                                , Snackbar.LENGTH_LONG);

                        View snackBarView = getLayoutInflater().inflate(R.layout.layout_snackbar_logged_in, null);

                        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);

                        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();

                        snackbarLayout.setPadding(0, 0, 0, 0);

                        TextView tvGreeting = snackBarView.findViewById(R.id.tv_greeting);
                        TextView tvEmail = snackBarView.findViewById(R.id.tv_email);

                        tvGreeting.setText(String.format("Hei %s!", snapshot.getString("firstName")));
                        tvEmail.setText(String.format("(%s)", snapshot.getString("email")));

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
                }
            });
        }
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

            documentReference = database
                    .collection("Users")
                    .document(user.getUid());

            documentReference
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot.exists()) {
                                tvName.setText(
                                        String.format(
                                                "%s %s",
                                                snapshot.getString("firstName"),
                                                snapshot.getString("lastName")
                                        )
                                );
                                tvEmail.setText(
                                        String.format(
                                                "%s",
                                                snapshot.getString("email")
                                        )
                                );
                            } else {
                                tvName.setText(user.getDisplayName());
                                tvEmail.setText(user.getEmail());
                            }
                        }
                    });
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
                startActivity(intent);
            }
        });

        tvSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer(drawerLayout);
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
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
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

                swipeRefreshLayout.setRefreshing(false);
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
                        channel.setInstagram(channelData.getString("instagram"));
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
                swipeRefreshLayout.setRefreshing(false);
                errorContainer.setVisibility(View.VISIBLE);
                pbLoading.setVisibility(View.GONE);

                bigSliderList.setVisibility(View.GONE);

                newsChannelContainer.setVisibility(View.GONE);
                newsChannelList.setVisibility(View.GONE);

                newsChannelContainer.setVisibility(View.GONE);
                newsChannelList.setVisibility(View.GONE);
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

                swipeRefreshLayout.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                newsChannelContainer.setVisibility(View.VISIBLE);
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
                        channel.setInstagram(channelData.getString("instagram"));
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
                swipeRefreshLayout.setRefreshing(false);
                errorContainer.setVisibility(View.VISIBLE);
                pbLoading.setVisibility(View.GONE);

                bigSliderList.setVisibility(View.GONE);

                newsChannelContainer.setVisibility(View.GONE);
                newsChannelList.setVisibility(View.GONE);

                newsChannelContainer.setVisibility(View.GONE);
                newsChannelList.setVisibility(View.GONE);
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

                swipeRefreshLayout.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                enterChannelContainer.setVisibility(View.VISIBLE);
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
                        channel.setInstagram(channelData.getString("instagram"));
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
                swipeRefreshLayout.setRefreshing(false);
                errorContainer.setVisibility(View.VISIBLE);
                pbLoading.setVisibility(View.GONE);

                bigSliderList.setVisibility(View.GONE);

                newsChannelContainer.setVisibility(View.GONE);
                newsChannelList.setVisibility(View.GONE);

                newsChannelContainer.setVisibility(View.GONE);
                newsChannelList.setVisibility(View.GONE);
            }
        });
    }
}