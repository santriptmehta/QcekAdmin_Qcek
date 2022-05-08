package com.blankspace.adminQcek;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

    List<QuestionModel> quesList;

    public QuestionAdapter(List<QuestionModel> quesList) {
        this.quesList = quesList;
    }

    @NonNull
    @Override
    public QuestionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ques_item_layout,parent,false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull QuestionAdapter.ViewHolder holder, int position) {

        String ques = quesList.get(position).getQuestion();
        String optA = quesList.get(position).getOptionA();
        String optB = quesList.get(position).getOptionB();
        String optC = quesList.get(position).getOptionC();
        String optD = quesList.get(position).getOptionD();
        int answer = quesList.get(position).getCorrectAns();

        holder.setData(position,ques,optA,optB,optC,optD,answer, this);

    }

    @Override
    public int getItemCount() {
        return quesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView quesTV;
        private TextView ansTV;
        private ImageView deleteB;
        private TextView quesNo;
        private Dialog loadingDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            quesTV = itemView.findViewById(R.id.ques);
            ansTV = itemView.findViewById(R.id.ans);
            deleteB = itemView.findViewById(R.id.deleteB);
            quesNo = itemView.findViewById(R.id.quesNo);

            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        }

        private void setData(final int pos, String ques, String a, String b, String c, String d, int ans, final QuestionAdapter adapter)
        {
            quesNo.setText("Question No. " + String.valueOf(pos + 1));
            quesTV.setText(ques);

            String ansStr;

            if(ans == 1)
                ansStr = "Answer : " + a;
            else if(ans == 2)
                ansStr = "Answer : " + b;
            else if(ans == 3)
                ansStr = "Answer : " + c;
            else
                ansStr = "Answer : " + d;

            ansTV.setText(ansStr);

            deleteB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());

                    builder.setCancelable(true);

                    View dialogview = LayoutInflater.from(itemView.getContext()).inflate(R.layout.delete_warning_dialog,null);

                    Button cancel = dialogview.findViewById(R.id.et_cancelB);
                    Button confirm = dialogview.findViewById(R.id.et_confirmB);
                    TextView title = dialogview.findViewById(R.id.et_title);
                    TextView msg = dialogview.findViewById(R.id.et_message);

                    title.setText("Delete Question");
                    msg.setText("Do You want to Delete this Question ?");

                    builder.setView(dialogview);

                    final AlertDialog alertDialog = builder.create();

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                    confirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                            deleteQ(pos, itemView.getContext(), adapter);
                        }
                    });

                    alertDialog.show();


                }
            });


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(itemView.getContext(), QuestionDetailsActivity.class);
                    intent.putExtra("ACTION","EDIT");
                    intent.putExtra("Q_ID",pos);
                    itemView.getContext().startActivity(intent);
                }
            });

        }

        private void deleteQ(int qNo, final Context context, final QuestionAdapter adapter)
        {

            loadingDialog.show();

            DbQuery.deleteQuestion(qNo, new OnCompleteListener() {
                @Override
                public void onSuccess() {

                    Toast.makeText(context, "Question Deleted Successfully", Toast.LENGTH_SHORT).show();

                    adapter.notifyDataSetChanged();

                    loadingDialog.dismiss();

                }

                @Override
                public void onFailure() {
                    Toast.makeText(context,"Something went wrong ! Please Try Again Later ",Toast.LENGTH_SHORT).show();
                    //Log.d()
                    loadingDialog.dismiss();
                }
            });

        }

    }


}
