package fi.jesunmaailma.tvapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import fi.jesunmaailma.tvapp.R;
import fi.jesunmaailma.tvapp.models.Channel;

public class Details extends AppCompatActivity {
    public static final String TAG = "TAG";
    public static final String id = "id";
    public static final String name = "name";
    public static final String image = "image";
    
    Toolbar toolbar;

    PlayerView playerView;
    ImageView fbLink, twtLink, igLink, ytLink, webLink;
    TextView channelTitle, channelDesc, playerDesc;
    ImageView ivLogo, fullScreenEnter, fullScreenExit;

    ProgressBar progressBar;

    boolean isFullScreen = false;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseAnalytics analytics;

    SimpleExoPlayer player;

    Channel channel;

    ActionBar actionBar;
    AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            setTheme(R.style.Theme_TVApp_NoActionBar);
        } else {
            setTheme(R.style.Theme_TVApp_NoActionBar);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        channel = (Channel) getIntent().getSerializableExtra("channel");

        toolbar = findViewById(R.id.tb_details);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        appBarLayout = findViewById(R.id.appbar);

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        ivLogo = findViewById(R.id.iv_logo);

        playerView = findViewById(R.id.playerView);
        fullScreenEnter = playerView.findViewById(R.id.exo_fullscreen_icon);
        fullScreenExit = playerView.findViewById(R.id.exo_fullscreen_exit);

        Picasso.get()
                .load(channel.getThumbnail())
                .into(ivLogo);

        progressBar = findViewById(R.id.progressBar);

        fullScreenEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFullScreen) {

                    fullScreenEnter.setVisibility(View.GONE);
                    fullScreenExit.setVisibility(View.VISIBLE);

                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LOW_PROFILE);

                    appBarLayout.setVisibility(View.GONE);

                    if (actionBar != null) {
                        actionBar.hide();
                    }

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) playerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = params.MATCH_PARENT;
                    playerView.setLayoutParams(params);

                    isFullScreen = true;
                }
            }
        });

        fullScreenExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullScreen) {

                    fullScreenExit.setVisibility(View.GONE);
                    fullScreenEnter.setVisibility(View.VISIBLE);

                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                    appBarLayout.setVisibility(View.VISIBLE);

                    if (actionBar != null) {
                        actionBar.show();
                    }

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) playerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = (int) (200 * getResources().getDisplayMetrics().density);
                    playerView.setLayoutParams(params);

                    isFullScreen = false;
                }
            }
        });

        fbLink = findViewById(R.id.facebookLink);
        twtLink = findViewById(R.id.twitterLink);
        igLink = findViewById(R.id.instagramLink);
        ytLink = findViewById(R.id.youtubeLink);
        webLink = findViewById(R.id.websiteLink);

        channelTitle = findViewById(R.id.channelTitle);
        channelDesc = findViewById(R.id.channelDesc);

        playerDesc = playerView.findViewById(R.id.playerDesc);

        channelTitle.setText(channel.getName());
        channelDesc.setText(channel.getDescription());

        playerDesc.setText(channel.getDescription());

        fbLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink(channel.getFacebook());
            }
        });

        twtLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink(channel.getTwitter());
            }
        });

        igLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink(channel.getInstagram());
            }
        });

        ytLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink(channel.getYoutube());
            }
        });

        webLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink(channel.getWebsite());
            }
        });

        playChannel(channel.getLiveUrl());
    }

    public void openLink(String url) {
        int color_toolbar = Color.parseColor("#00142F");

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setShowTitle(true);
        builder.setToolbarColor(color_toolbar);

        CustomTabsIntent intent = builder.build();
        intent.launchUrl(Details.this, Uri.parse(url));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void playChannel(String liveUrl) {
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
        HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(liveUrl));

        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                    player.setPlayWhenReady(true);
                } else if (state == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                    playerView.setKeepScreenOn(true);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        player.setPlayWhenReady(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        player.seekToDefaultPosition();
        player.setPlayWhenReady(true);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        player.release();
        super.onDestroy();
    }
}