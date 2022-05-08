package com.blankspace.adminQcek;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private Button addFromExcel, editCat, logoutB;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        addFromExcel = findViewById(R.id.add_from_excel);
        editCat = findViewById(R.id.edit_cat);
        logoutB = findViewById(R.id.logoutB);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Select Any Option");


        editCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isMultiClick(1000))
                {
                    return;
                }

                Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                startActivity(intent);

            }
        });

        addFromExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isMultiClick(1000))
                {
                    return;
                }

                Intent intent = new Intent(MainActivity.this, AddBulkQActivity.class);
                startActivity(intent);
            }
        });

        logoutB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
            }
        });

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
