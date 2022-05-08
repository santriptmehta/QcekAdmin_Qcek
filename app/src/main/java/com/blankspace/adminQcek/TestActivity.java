package com.blankspace.adminQcek;

import android.app.Dialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.blankspace.adminQcek.DbQuery.addNewTest;
import static com.blankspace.adminQcek.DbQuery.g_catList;
import static com.blankspace.adminQcek.DbQuery.g_testList;
import static com.blankspace.adminQcek.DbQuery.loadTestData;
import static com.blankspace.adminQcek.DbQuery.selected_cat_index;

public class TestActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView testView;
    private TestListAdapter adapter;
    private Dialog loadingDialog;
    private Button addNewB;
    private long mLastClickTime = 0;
    private Dialog addTestDialog;
    private EditText dialogTestTime;
    private TextView dialogTitle;
    private Button dialogAddB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        toolbar = findViewById(R.id.at_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(g_catList.get(selected_cat_index).getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        testView = findViewById(R.id.test_recyler_view);
        addNewB = findViewById(R.id.add_new_test);

        loadingDialog = new Dialog(TestActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        addTestDialog = new Dialog(TestActivity.this);
        addTestDialog.setContentView(R.layout.add_category_dialog);
        addTestDialog.setCancelable(true);
        addTestDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogTestTime = addTestDialog.findViewById(R.id.ac_cat_name);
        dialogAddB = addTestDialog.findViewById(R.id.ac_add_btn);
        dialogTitle = addTestDialog.findViewById(R.id.title);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        testView.setLayoutManager(layoutManager);

        //loadTestData();

        loadingDialog.show();

        loadTestData(new OnCompleteListener() {
            @Override
            public void onSuccess() {
                adapter = new TestListAdapter(g_testList);
                testView.setAdapter(adapter);
                loadingDialog.dismiss();
            }

            @Override
            public void onFailure() {
                Toast.makeText(TestActivity.this, "Something went wrong ! Please Try Again Later ", Toast.LENGTH_SHORT).show();
                //Log.d()
                loadingDialog.dismiss();
            }
        });



        addNewB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(isMultiClick(1000))
                {
                    return;
                }

                dialogTestTime.getText().clear();
                dialogTitle.setText("Add New Test");
                dialogTestTime.setHint("Enter Test Time in Minutes");
                addTestDialog.show();

            }
        });


        dialogAddB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isMultiClick(1000))
                {
                    return;
                }

                if(dialogTestTime.getText().toString().isEmpty())
                {
                    dialogTestTime.setError("Enter Test Time");
                    return;
                }

                if( ! TextUtils.isDigitsOnly(dialogTestTime.getText()))
                {
                    dialogTestTime.setError("Enter only integer");
                    return;
                }

                addNewTestData(Integer.valueOf(dialogTestTime.getText().toString()));
            }
        });

    }


    private void addNewTestData(int time)
    {
        addTestDialog.dismiss();
        loadingDialog.show();

        addNewTest(time,new OnCompleteListener() {
            @Override
            public void onSuccess() {

                Toast.makeText(TestActivity.this,"Test Added Successfully",Toast.LENGTH_SHORT).show();

                adapter.notifyItemInserted(g_testList.size());

                loadingDialog.dismiss();
            }

            @Override
            public void onFailure() {
                Toast.makeText(TestActivity.this,"Something went wrong ! Please Try Again Later ",Toast.LENGTH_SHORT).show();
                //Log.d()
                loadingDialog.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(adapter != null)
            adapter.notifyDataSetChanged();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home)
        {
            TestActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
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
