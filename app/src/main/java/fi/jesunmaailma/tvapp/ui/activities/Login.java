package fi.jesunmaailma.tvapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabsIntent;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
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
    MaterialButton btnSignIn;
    TextView btnReadMore, btnRegister, btnForgotPassword;

    EditText emailEdit, passwordEdit;

    FirebaseAuth auth;
    FirebaseUser user;
    GoogleSignInClient client;

    FirebaseAnalytics analytics;

    ProgressDialog progressDialog;
    ActionBar actionBar;

    AlertDialog.Builder builder;
    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            setTheme(R.style.Theme_TVApp);
        } else {
            setTheme(R.style.Theme_TVApp);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnReadMore = findViewById(R.id.btn_read_more);

        btnSignInWithGoogle = findViewById(R.id.btn_sign_in_with_google);
        btnSignIn = findViewById(R.id.loginBtn);
        btnRegister = findViewById(R.id.register_activity_txt);

        btnForgotPassword = findViewById(R.id.forgotPasswordBtn);

        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);

        builder = new AlertDialog.Builder(this);
        inflater = getLayoutInflater();

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

        emailEdit.addTextChangedListener(watcher);
        passwordEdit.addTextChangedListener(watcher);

        client = GoogleSignIn.getClient(Login.this, options);

        btnSignInWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = client.getSignInIntent();
                startActivityForResult(intent, GOOGLE_AUTH_CODE);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                if (email.isEmpty()) {
                    emailEdit.setError("Sähköposti vaaditaan.");
                    return;
                }

                if (password.isEmpty()) {
                    passwordEdit.setError("Salasana vaaditaan.");
                    return;
                }

                if (password.length() < 8) {
                    passwordEdit.setError("Salasanan täytyy olla 8 merkin pituinen.");
                    return;
                }

                progressDialog.show();
                btnSignIn.setEnabled(false);

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            startActivity(
                                    new Intent(
                                            getApplicationContext()
                                    , MainActivity.class
                                    ).addFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    )
                            );
                            finish();
                            overridePendingTransition(0, 0);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Virhe! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        btnReadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jesun_maailma_tili_url = "https://finnplace.ml/jesun-maailma-tili";

                int color_toolbar = Color.parseColor("#37439F");

                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setShowTitle(true);
                builder.setToolbarColor(color_toolbar);

                CustomTabsIntent intent = builder.build();
                intent.launchUrl(Login.this, Uri.parse(jesun_maailma_tili_url));
            }
        });

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = inflater.inflate(R.layout.forgot_password_dialog, null);
                builder.setTitle("Unohditko salasanasi?")
                        .setMessage("Syötä sähköpostisi saadaksesi salasanan palautuslinkin.")
                        .setPositiveButton("Palauta", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText email = view.findViewById(R.id.emailEdit);
                                String emailVal = email.getText().toString();

                                if (emailVal.isEmpty()) {
                                    email.setError("Pakollinen.");
                                    return;
                                }

                                auth.sendPasswordResetEmail(emailVal).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "Salasanan palautuslinkki lähetetty.", Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).setNegativeButton("Peruuta", null)
                        .setView(view)
                        .create().show();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
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

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String email = emailEdit.getText().toString();
            String password = passwordEdit.getText().toString();

            btnSignIn.setEnabled(!email.isEmpty() && !password.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

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
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    finish();
                                    overridePendingTransition(0, 0);
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
                progressDialog.dismiss();
            }
        }
    }
}