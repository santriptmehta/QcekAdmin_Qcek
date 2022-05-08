package com.blankspace.adminQcek;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

import static com.blankspace.adminQcek.DbQuery.loadData;
import static java.lang.System.exit;

public class SplashActivity extends AppCompatActivity {

    private TextView appName;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        appName = findViewById(R.id.app_name);

       // Typeface typeface = ResourcesCompat.getFont(this,R.font.blacklist);
       // appName.setTypeface(typeface);

       // Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.myanim);
       // appName.setAnimation(anim);


        DbQuery.g_firestore = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

        //clearData();

        new Thread() {
            public void run() {


                if(mAuth.getCurrentUser() != null)
                {


                    loadData(new OnCompleteListener() {
                        @Override
                        public void onSuccess() {

                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        }

                        public void onFailure()
                        {
                            Toast.makeText(SplashActivity.this,"Something went wrong ! Please Try Later ",Toast.LENGTH_SHORT).show();
                            //Log.d()
                            exit(0);
                        }
                    });


                }
                else
                {

                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }


            }
        }.start();

    }


}
