package fi.jesunmaailma.tvapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import fi.jesunmaailma.tvapp.R;

public class Login extends AppCompatActivity {
    public static final int GOOGLE_AUTH_CODE = 240;
    public static final String id = "id";
    public static final String name = "name";
    public static final String image = "image";

    SignInButton btnSignInWithGoogle;
    FirebaseAuth auth;
    FirebaseUser user;
    GoogleSignInClient client;

    FirebaseAnalytics analytics;

    CoordinatorLayout clRoot;

    ProgressDialog progressDialog;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnSignInWithGoogle = findViewById(R.id.btn_sign_in_with_google);
        clRoot = findViewById(R.id.clRoot);

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Kirjaudu sisään");
        }

        progressDialog = new ProgressDialog(Login.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Kirjaudutaan sisään...");

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        client = GoogleSignIn.getClient(Login.this, options);

        btnSignInWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = client.getSignInIntent();
                startActivityForResult(intent, GOOGLE_AUTH_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_AUTH_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn
                    .getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                try {
                    GoogleSignInAccount account = task
                            .getResult(ApiException.class);
                    if (account != null) {
                        progressDialog.show();
                        AuthCredential credential = GoogleAuthProvider
                                .getCredential(account.getIdToken(), null);
                        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext()
                                            , "Virhe! " + task.getException()
                                                    .getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            } else {
                Snackbar snackbar = Snackbar.make(
                        clRoot,
                        "Käyttäjä keskeytti kirjautumisen.",
                        Snackbar.LENGTH_LONG
                );
                snackbar.setDuration(5000);
                snackbar.show();
            }
        }
    }
}