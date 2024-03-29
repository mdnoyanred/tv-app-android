package fi.jesunmaailma.tvapp.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import fi.jesunmaailma.tvapp.BuildConfig;
import fi.jesunmaailma.tvapp.R;

public class Settings extends AppCompatActivity {

    public static final String id = "id";
    public static final String name = "name";
    public static final String image = "image";

    FirebaseAnalytics analytics;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            setTheme(R.style.Theme_TVApp_NoActionBar);
        } else {
            setTheme(R.style.Theme_TVApp_NoActionBar);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        toolbar = findViewById(R.id.tb_settings);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());

            ListPreference lp_theme = findPreference("theme");
            if (lp_theme != null) {
                String theme = sp.getString("theme", "false");
                if ("light_theme".equals(theme)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    lp_theme.setSummary(lp_theme.getEntry());
                } else if ("dark_theme".equals(theme)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    lp_theme.setSummary(lp_theme.getEntry());
                } else if ("system_default".equals(theme)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    lp_theme.setSummary(lp_theme.getEntry());
                }

                lp_theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object obj) {

                        String items = (String) obj;
                        if (preference.getKey().equals("theme")) {
                            switch (items) {
                                case "light_theme":
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                    break;
                                case "dark_theme":
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                    break;
                                case "system_default":
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                                    break;
                            }

                            ListPreference listPreferences = (ListPreference) preference;
                            listPreferences.setSummary(listPreferences.getEntries()[listPreferences.findIndexOfValue(items)]);

                        }

                        return true;
                    }
                });

            }

            Preference pref_open_source = findPreference("open_source");
            if (pref_open_source != null) {
                pref_open_source.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(preference.getContext(), OssLicensesMenuActivity.class);
                        startActivity(intent);
                        OssLicensesMenuActivity.setActivityTitle(getResources().getString(R.string.open_source));
                        return true;
                    }
                });
            }

            Preference pref_show_source = findPreference("show_source");
            if (pref_show_source != null) {
                pref_show_source.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        String repository_url = "https://github.com/JTG69YT/tv-app-android";

                        Intent intent_repository_url = new Intent(Intent.ACTION_VIEW, Uri.parse(repository_url))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent_repository_url);
                        return true;
                    }
                });
            }

            Preference pref_info = findPreference("info");
            if (pref_info != null) {
                pref_info.setSummary(String.format("%s (%s)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
            }

            Preference pref_tos = findPreference("terms_of_service");
            if (pref_tos != null) {
                pref_tos.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        String tos_url = "https://www.privacypolicies.com/live/520c0c3e-b033-4cee-8288-fbb61861f188";

                        int color_toolbar = Color.parseColor("#000000");

                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setShowTitle(true);
                        builder.setToolbarColor(color_toolbar);

                        CustomTabsIntent intent = builder.build();
                        intent.launchUrl(preference.getContext(), Uri.parse(tos_url));

                        return true;
                    }
                });
            }

            Preference pref_pp = findPreference("privacy_policy");
            if (pref_pp != null) {
                pref_pp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        String pp_url = "https://www.privacypolicies.com/live/3d159e75-97bd-48df-9003-2b0178d01836";

                        int color_toolbar = Color.parseColor("#000000");

                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setShowTitle(true);
                        builder.setToolbarColor(color_toolbar);

                        CustomTabsIntent intent = builder.build();
                        intent.launchUrl(preference.getContext(), Uri.parse(pp_url));

                        return true;
                    }
                });
            }

            Preference pref_copyright = findPreference("copyright");
            if (pref_copyright != null) {
                pref_copyright.setSummary(
                        String.format(
                                "%s",
                                "© 2021 Jesun Maailma"
                        )
                );
            }
        }
    }
}