package fi.jesunmaailma.tvapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.squareup.picasso.Picasso;

import fi.jesunmaailma.tvapp.R;
import fi.jesunmaailma.tvapp.models.Channel;

public class Details extends AppCompatActivity {
    public static final String TAG = "TAG";
    PlayerView playerView;
    ImageView fbLink, twtLink, ytLink, webLink, playerLogo;
    TextView channelTitle, channelDesc, playerTitle, playerDesc;
    ImageView fullScreen;
    ProgressBar progressBar;
    boolean isFullScreen = false;

    SimpleExoPlayer player;

    Channel channel;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        actionBar = getSupportActionBar();

        channel = (Channel) getIntent().getSerializableExtra("channel");

        actionBar.setTitle(channel.getName());
        actionBar.setDisplayHomeAsUpEnabled(true);

        playerView = findViewById(R.id.playerView);
        fullScreen = playerView.findViewById(R.id.exo_fullscreen_icon);

        progressBar = findViewById(R.id.progressBar);

        fullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullScreen) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                    if (actionBar != null) {
                        actionBar.show();
                    }

                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) playerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = (int) (200 * getResources().getDisplayMetrics().density);
                    playerView.setLayoutParams(params);

                    isFullScreen = false;
                } else {
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

        fbLink = findViewById(R.id.facebookLink);
        twtLink = findViewById(R.id.twitterLink);
        ytLink = findViewById(R.id.youtubeLink);
        webLink = findViewById(R.id.websiteLink);

        channelTitle = findViewById(R.id.channelTitle);
        channelDesc = findViewById(R.id.channelDesc);

        playerLogo = playerView.findViewById(R.id.playerLogo);

        Picasso.get().load(channel.getThumbnail()).into(playerLogo);

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
        Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(open);
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