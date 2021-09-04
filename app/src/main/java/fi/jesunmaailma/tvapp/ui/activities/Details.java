package fi.jesunmaailma.tvapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    PlayerView playerView;
    ImageView channelLogo, fbLink, twtLink, igLink, ytLink, webLink;
    TextView channelTitle, channelDesc, playerTitle, playerDesc;
    ImageView fullScreenEnter, fullScreenExit;
    ProgressBar progressBar;
    boolean isFullScreen = false;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseAnalytics analytics;

    SimpleExoPlayer player;

    Channel channel;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            setTheme(R.style.Theme_TVApp);
        } else {
            setTheme(R.style.Theme_TVApp);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        actionBar = getSupportActionBar();

        channel = (Channel) getIntent().getSerializableExtra("channel");

        actionBar.setTitle(channel.getName());
        actionBar.setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        playerView = findViewById(R.id.playerView);
        fullScreenEnter = playerView.findViewById(R.id.exo_fullscreen_icon);
        fullScreenExit = playerView.findViewById(R.id.exo_fullscreen_exit);

        progressBar = findViewById(R.id.progressBar);

        fullScreenEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFullScreen) {

                    fullScreenEnter.setVisibility(View.GONE);
                    fullScreenExit.setVisibility(View.VISIBLE);

                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

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

        channelLogo = findViewById(R.id.channelLogo);
        channelTitle = findViewById(R.id.channelTitle);
        channelDesc = findViewById(R.id.channelDesc);

        Picasso.get()
                .load(channel.getThumbnail())
                .placeholder(R.drawable.tvapp_logo_placeholder)
                .into(channelLogo);

        playerTitle = playerView.findViewById(R.id.playerName);
        playerDesc = playerView.findViewById(R.id.playerDesc);

        channelTitle.setText(channel.getName());
        channelDesc.setText(channel.getDescription());

        playerTitle.setText(channel.getName());
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

        if (user != null) {
            playChannel(channel.getLiveUrl());
        } else {
            showAlertDialog(
                    Details.this,
                    "Kirjautuminen vaaditaan!",
                    String.format(
                            "%s\n\n%s",
                            "Kirjautuminen vaaditaan, jotta voit katsoa netti-tv:tä.",
                            "Kirjaudu sisään painamalla alla olevaa \"Kirjaudu\"-painiketta."
                    )
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            overridePendingTransition(0, 0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showAlertDialog(Activity activity, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        builder.setPositiveButton("Kirjaudu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(getApplicationContext(), Login.class), 100);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void openLink(String url) {
        int color_toolbar = Color.BLACK;

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
        if (user != null) {
            player.setPlayWhenReady(false);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (user != null) {
            player.seekToDefaultPosition();
            player.setPlayWhenReady(true);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (user != null) {
            player.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        if (user == null) {
            showAlertDialog(
                    Details.this,
                    "Kirjautuminen vaaditaan!",
                    String.format(
                            "%s\n\n%s",
                            "Kirjautuminen vaaditaan, jotta voit katsoa netti-tv:tä.",
                            "Kirjaudu sisään painamalla alla olevaa \"Kirjaudu\"-painiketta."
                    )
            );
        }
        super.onStart();
    }
}