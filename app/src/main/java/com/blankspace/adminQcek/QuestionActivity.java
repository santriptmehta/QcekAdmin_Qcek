package com.blankspace.adminQcek;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.blankspace.adminQcek.DbQuery.g_catList;
import static com.blankspace.adminQcek.DbQuery.g_firestore;
import static com.blankspace.adminQcek.DbQuery.g_quesList;
import static com.blankspace.adminQcek.DbQuery.g_testList;
import static com.blankspace.adminQcek.DbQuery.selected_cat_index;
import static com.blankspace.adminQcek.DbQuery.selected_test_index;

public class QuestionActivity extends AppCompatActivity {

    private RecyclerView quesListView;
    private Dialog loadingDialog;
    private QuestionAdapter adapter;
    private Button addQuesB;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        quesListView = findViewById(R.id.ques_view);
        addQuesB = findViewById(R.id.add_new_ques);

        Toolbar toolbar = findViewById(R.id.ba_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Questions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingDialog = new Dialog(QuestionActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);



        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        quesListView.setLayoutManager(layoutManager);

        loadQuestions();

        addQuesB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isMultiClick(1000))
                {
                    return;
                }

                Intent intent = new Intent(QuestionActivity.this, QuestionDetailsActivity.class);
                intent.putExtra("ACTION","ADD");
                startActivity(intent);
            }
        });

    }


    private void loadQuestions()
    {
        g_quesList.clear();

        loadingDialog.show();

        CollectionReference collectionRef = g_firestore.collection("Questions");
        collectionRef.whereEqualTo("CATEGORY",g_catList.get(selected_cat_index).getDocID())
                .whereEqualTo("TEST", g_testList.get(selected_test_index).getTestID())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            g_quesList.add(new QuestionModel(
                                    doc.getId(),
                                    doc.getString("QUESTION"),
                                    doc.getString("A"),
                                    doc.getString("B"),
                                    doc.getString("C"),
                                    doc.getString("D"),
                                    doc.getLong("ANSWER").intValue()
                            ));

                        }

                            adapter = new QuestionAdapter(g_quesList);
                            quesListView.setAdapter(adapter);

                            getSupportActionBar().setTitle("Test : " + String.valueOf(selected_test_index + 1) + " (" + String.valueOf(g_quesList.size()) + " Questions)");

                            loadingDialog.dismiss();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(QuestionActivity.this,"Something went wrong ! Please Try Again Later ",Toast.LENGTH_SHORT).show();
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

        getSupportActionBar().setTitle("Test : " + String.valueOf(selected_test_index + 1) + " (" + String.valueOf(g_quesList.size()) + " Questions)");


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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
