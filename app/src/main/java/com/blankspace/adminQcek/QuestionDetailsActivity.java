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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import static com.blankspace.adminQcek.DbQuery.addNewQuestion;
import static com.blankspace.adminQcek.DbQuery.editQuestion;
import static com.blankspace.adminQcek.DbQuery.g_quesList;

public class QuestionDetailsActivity extends AppCompatActivity {

    private EditText ques, optionA, optionB, optionC, optionD, answer;
    private Button addQB;
    private String qStr, aStr, bStr, cStr, dStr, ansStr;
    private Dialog loadingDialog;
    private String action;
    private int qID;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_details);

        Toolbar toolbar = findViewById(R.id.qdetails_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ques = findViewById(R.id.question);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        answer = findViewById(R.id.answer);
        addQB = findViewById(R.id.addQB);

        loadingDialog = new Dialog(QuestionDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        action = getIntent().getStringExtra("ACTION");

        if(action.compareTo("EDIT") == 0)
        {
            qID = getIntent().getIntExtra("Q_ID",0);
            loadData(qID);
            getSupportActionBar().setTitle("Question " + String.valueOf(qID + 1));
            addQB.setText("UPDATE");
        }
        else
        {
            getSupportActionBar().setTitle("Question " + String.valueOf(g_quesList.size() + 1));
            addQB.setText("ADD");
        }

        addQB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isMultiClick(1000))
                {
                    return;
                }

                qStr = ques.getText().toString();
                aStr = optionA.getText().toString();
                bStr = optionB.getText().toString();
                cStr = optionC.getText().toString();
                dStr = optionD.getText().toString();
                ansStr = answer.getText().toString();

                if(qStr.isEmpty()) {
                    ques.setError("Enter Question");
                    return;
                }

                if(aStr.isEmpty()) {
                    optionA.setError("Enter option A");
                    return;
                }

                if(bStr.isEmpty()) {
                    optionB.setError("Enter option B ");
                    return;
                }
                if(cStr.isEmpty()) {
                    optionC.setError("Enter option C");
                    return;
                }
                if(dStr.isEmpty()) {
                    optionD.setError("Enter option D");
                    return;
                }
                if(ansStr.isEmpty()) {
                    answer.setError("Enter correct answer");
                    return;
                }

                if(!TextUtils.isDigitsOnly(ansStr)){
                    answer.setError("Enter digits only");
                    return;
                }

                int ans = Integer.valueOf(ansStr);
                if(ans < 1 || ans > 4)
                {
                    answer.setError("Enter answer between 1 to 4");
                    return;

                }

                if(action.compareTo("EDIT") == 0)
                {
                    editQ();
                }
                else {
                    addQuestion();
                }

            }
        });
    }




    private void loadData(int id)
    {
        ques.setText(g_quesList.get(id).getQuestion());
        optionA.setText(g_quesList.get(id).getOptionA());
        optionB.setText(g_quesList.get(id).getOptionB());
        optionC.setText(g_quesList.get(id).getOptionC());
        optionD.setText(g_quesList.get(id).getOptionD());
        answer.setText(String.valueOf(g_quesList.get(id).getCorrectAns()));
    }

    private void editQ()
    {
        loadingDialog.show();

        editQuestion(qID, qStr, aStr, bStr, cStr, dStr, Integer.valueOf(ansStr), new OnCompleteListener() {
            @Override
            public void onSuccess() {

                Toast.makeText(QuestionDetailsActivity.this, "Question Updated Successfully", Toast.LENGTH_SHORT).show();

                loadingDialog.dismiss();

                finish();

            }

            @Override
            public void onFailure() {

                Toast.makeText(QuestionDetailsActivity.this,"Something went wrong ! Please Try Again Later ",Toast.LENGTH_SHORT).show();
                //Log.d()
                loadingDialog.dismiss();
            }
        });
    }


    private void addQuestion()
    {
        loadingDialog.show();

        addNewQuestion(qStr, aStr, bStr, cStr, dStr, Integer.valueOf(ansStr), new OnCompleteListener() {
            @Override
            public void onSuccess() {

                Toast.makeText(QuestionDetailsActivity.this, "Question Added Successfully", Toast.LENGTH_SHORT).show();

                loadingDialog.dismiss();

                finish();
            }

            @Override
            public void onFailure() {
                Toast.makeText(QuestionDetailsActivity.this,"Something went wrong ! Please Try Again Later ",Toast.LENGTH_SHORT).show();
                //Log.d()
                loadingDialog.dismiss();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}

