package com.blankspace.adminQcek;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.blankspace.adminQcek.DbQuery.checkPermissions;
import static com.blankspace.adminQcek.DbQuery.loadData;


public class LoginActivity extends AppCompatActivity {

    private EditText email, pass;
    private Button loginB;
    private FirebaseAuth mAuth;
    private Dialog loadingDialog;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();


    }

    private void initViews()
    {
        email = findViewById(R.id.tv_email);
        pass = findViewById(R.id.tv_password);
        loginB = findViewById(R.id.login_btn);
    }

    private void setClickListeners()
    {

        loginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isMultiClick(2000))
                {
                    return;
                }

                if(validateData())
                {
                    loginUser();
                }
            }
        });


    }

    private void init()
    {
        initViews();

        mAuth = FirebaseAuth.getInstance();

        setClickListeners();

        loadingDialog = new Dialog(LoginActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private boolean validateData()
    {
        if(email.getText().toString().isEmpty())
        {
            email.setError("Enter Email ID");
            return false;
        }

        if(pass.getText().toString().isEmpty())
        {
            pass.setError("Enter Password");
            return false;
        }


        return true;
    }

    private void loginUser()
    {
        loadingDialog.show();
        mAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success


                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            checkPermissions(uid, new com.blankspace.adminQcek.OnCompleteListener() {
                                @Override
                                public void onSuccess() {

                                    if(DbQuery.authorised)
                                    {
                                        loadData(new com.blankspace.adminQcek.OnCompleteListener() {

                                            @Override
                                            public void onSuccess() {
                                                Toast.makeText(LoginActivity.this, "Login Success",
                                                        Toast.LENGTH_SHORT).show();

                                                loadingDialog.dismiss();
                                                goToMainActivity();

                                            }

                                            @Override
                                            public void onFailure()
                                            {
                                                Toast.makeText(LoginActivity.this,"Something went wrong ! Please Try Again Later ",Toast.LENGTH_SHORT).show();
                                                //Log.d()
                                                FirebaseAuth.getInstance().signOut();
                                                loadingDialog.dismiss();
                                            }

                                        });
                                    }
                                    else
                                    {

                                        Toast.makeText(LoginActivity.this,"You are not admin user",Toast.LENGTH_LONG).show();
                                        FirebaseAuth.getInstance().signOut();
                                        loadingDialog.dismiss();
                                    }

                                }

                                @Override
                                public void onFailure() {

                                    Toast.makeText(LoginActivity.this,"Something went wrong ! Please Try Again Later ",Toast.LENGTH_SHORT).show();
                                    //Log.d()
                                    FirebaseAuth.getInstance().signOut();
                                    loadingDialog.dismiss();
                                }
                            });


                        } else {

                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            //Log.d()
                            loadingDialog.dismiss();

                        }

                    }
                });

    }

    private void goToMainActivity()
    {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private boolean isMultiClick(int interval)
    {
        if(SystemClock.elapsedRealtime() - mLastClickTime < interval)
        {
            mLastClickTime = SystemClock.elapsedRealtime();
            return true;
        }

        mLastClickTime = SystemClock.elapsedRealtime();
        return false;
    }

}
