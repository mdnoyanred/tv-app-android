package fi.jesunmaailma.tvapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.tvapp.R;
import fi.jesunmaailma.tvapp.adapters.ChannelAdapter;
import fi.jesunmaailma.tvapp.models.Channel;
import fi.jesunmaailma.tvapp.services.ChannelDataService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
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

    CardView errorContainer;
    TextView tvError;

    ProgressBar pbLoadingSlider, pbLoadingNews, pbLoadingEntertainment;

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

        errorContainer = findViewById(R.id.error_container);

        tvError = findViewById(R.id.tv_error);

        pbLoadingSlider = findViewById(R.id.pbLoadingSlider);
        pbLoadingNews = findViewById(R.id.pbLoadingNews);
        pbLoadingEntertainment = findViewById(R.id.pbLoadingEntertainment);

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

        bigSliderAdapter = new ChannelAdapter(channelList, "slider", auth, user);
        bigSliderList.setAdapter(bigSliderAdapter);

        pbLoadingSlider.setVisibility(View.VISIBLE);
        pbLoadingNews.setVisibility(View.VISIBLE);
        pbLoadingEntertainment.setVisibility(View.VISIBLE);

        getSliderData(getResources().getString(R.string.teeveet_prod_api_url) + "?api_key=1A4mgi2rBHCJdqggsYVx&channels=all&user_id=1");
        getNewsChannels(getResources().getString(R.string.teeveet_prod_api_url) + "?api_key=1A4mgi2rBHCJdqggsYVx&category=Uutiset&user_id=1");
        getEntertainmentChannel(getResources().getString(R.string.teeveet_prod_api_url) + "?api_key=1A4mgi2rBHCJdqggsYVx&category=Viihde&user_id=1");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);

                pbLoadingSlider.setVisibility(View.VISIBLE);
                pbLoadingNews.setVisibility(View.VISIBLE);
                pbLoadingEntertainment.setVisibility(View.VISIBLE);

                errorContainer.setVisibility(View.GONE);

                bigSliderList.setVisibility(View.GONE);
                getSliderData(getResources().getString(R.string.teeveet_prod_api_url) + "?api_key=1A4mgi2rBHCJdqggsYVx&channels=all&user_id=1");

                newsChannelList.setVisibility(View.GONE);
                getNewsChannels(getResources().getString(R.string.teeveet_prod_api_url) + "?api_key=1A4mgi2rBHCJdqggsYVx&category=Uutiset&user_id=1");

                enterChannelList.setVisibility(View.GONE);
                getEntertainmentChannel(getResources().getString(R.string.teeveet_prod_api_url) + "?api_key=1A4mgi2rBHCJdqggsYVx&category=Viihde&user_id=1");
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
        TextView tvSignIn = headerView.findViewById(R.id.tv_login);
        MaterialButton tvSignOut = headerView.findViewById(R.id.tv_sign_out);

        if (user != null) {
            tvName.setVisibility(View.VISIBLE);
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
                                                "Kirjauduit sisään tilillä:\n%s %s",
                                                snapshot.getString("firstName"),
                                                snapshot.getString("lastName")
                                        )
                                );
                            } else {
                                tvName.setText(
                                        String.format(
                                                "Kirjauduit sisään tilillä:\n%s",
                                                user.getDisplayName()
                                        )
                                );
                            }
                        }
                    });
        } else {
            tvName.setVisibility(View.GONE);
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

                documentReference = database.collection("Users").document(user.getUid());
                documentReference
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot snapshot = task.getResult();
                                if (snapshot.exists()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setCancelable(false);
                                    builder.setTitle(
                                            String.format(
                                                    "%s %s",
                                                    snapshot.getString("firstName"),
                                                    snapshot.getString("lastName")
                                            )
                                    );
                                    builder.setMessage("Haluatko varmasti kirjautua ulos Teeveet-palvelusta?");
                                    builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.setPositiveButton("Kirjaudu ulos", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            auth.signOut();

                                            client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    startActivity(
                                                            new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class
                                                            )
                                                    );
                                                    finish();
                                                    overridePendingTransition(0, 0);
                                                }
                                            });
                                        }
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setCancelable(false);
                                    builder.setTitle(user.getDisplayName());
                                    builder.setMessage("Haluatko varmasti kirjautua ulos Teeveet-palvelusta?");
                                    builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.setPositiveButton("Kirjaudu ulos", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            auth.signOut();

                                            client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    startActivity(
                                                            new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class
                                                            )
                                                    );
                                                    finish();
                                                    overridePendingTransition(0, 0);
                                                }
                                            });
                                        }
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mi_open_in_browser) {
            String teeveet_url = "https://teeveet.ml";

            Intent intent_teeveet_url = new Intent(Intent.ACTION_VIEW, Uri.parse(teeveet_url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent_teeveet_url);
        }
        return super.onOptionsItemSelected(item);
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

        if (item.getItemId() == R.id.mi_settings) {
            closeDrawer(drawerLayout);
            startActivity(new Intent(getApplicationContext(), Settings.class));
        }

        if (item.getItemId() == R.id.mi_exit) {
            closeDrawer(drawerLayout);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(false);
            builder.setMessage("Haluatko varmasti poistua Teeveet-palvelusta?");
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
            public void onResponse(JSONArray response) {

                swipeRefreshLayout.setRefreshing(false);
                pbLoadingSlider.setVisibility(View.GONE);
                pbLoadingNews.setVisibility(View.GONE);
                pbLoadingEntertainment.setVisibility(View.GONE);
                bigSliderList.setVisibility(View.VISIBLE);

                channelList.clear();

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject channelData = response.getJSONObject(i);

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

                pbLoadingSlider.setVisibility(View.GONE);
                pbLoadingNews.setVisibility(View.GONE);
                pbLoadingEntertainment.setVisibility(View.GONE);

                bigSliderList.setVisibility(View.GONE);

                newsChannelList.setVisibility(View.GONE);

                newsChannelList.setVisibility(View.GONE);
            }
        });
    }

    public void getNewsChannels(String url) {
        newsChannelList = findViewById(R.id.news_channel_list);
        newsChannels = new ArrayList<>();

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        newsChannelList.setLayoutManager(manager);

        newsChannelAdapter = new ChannelAdapter(newsChannels, "category", auth, user);
        newsChannelList.setAdapter(newsChannelAdapter);

        service.getChannelData(url, new ChannelDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONArray response) {

                swipeRefreshLayout.setRefreshing(false);

                pbLoadingSlider.setVisibility(View.GONE);
                pbLoadingNews.setVisibility(View.GONE);
                pbLoadingEntertainment.setVisibility(View.GONE);

                newsChannelList.setVisibility(View.VISIBLE);

                newsChannels.clear();

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject channelData = response.getJSONObject(i);

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

                pbLoadingSlider.setVisibility(View.GONE);
                pbLoadingNews.setVisibility(View.GONE);
                pbLoadingEntertainment.setVisibility(View.GONE);

                bigSliderList.setVisibility(View.GONE);

                newsChannelList.setVisibility(View.GONE);

                newsChannelList.setVisibility(View.GONE);
            }
        });
    }

    public void getEntertainmentChannel(String url) {
        enterChannelList = findViewById(R.id.enter_channel_list);
        enterChannel = new ArrayList<>();

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        enterChannelList.setLayoutManager(manager);

        enterChannelAdapter = new ChannelAdapter(enterChannel, "category", auth, user);
        enterChannelList.setAdapter(enterChannelAdapter);

        service.getChannelData(url, new ChannelDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONArray response) {

                swipeRefreshLayout.setRefreshing(false);

                pbLoadingSlider.setVisibility(View.GONE);
                pbLoadingNews.setVisibility(View.GONE);
                pbLoadingEntertainment.setVisibility(View.GONE);

                enterChannelList.setVisibility(View.VISIBLE);

                enterChannel.clear();

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject channelData = response.getJSONObject(i);

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

                pbLoadingSlider.setVisibility(View.GONE);
                pbLoadingNews.setVisibility(View.GONE);
                pbLoadingEntertainment.setVisibility(View.GONE);

                bigSliderList.setVisibility(View.GONE);

                newsChannelList.setVisibility(View.GONE);

                newsChannelList.setVisibility(View.GONE);
            }
        });
    }
}