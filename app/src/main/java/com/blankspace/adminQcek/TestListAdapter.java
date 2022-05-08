package com.blankspace.adminQcek;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import static com.blankspace.adminQcek.DbQuery.deleteTestFromDB;
import static com.blankspace.adminQcek.DbQuery.updateTestDb;

public class TestListAdapter extends RecyclerView.Adapter<TestListAdapter.ViewHolder> {

    private List<TestModel> testList;

    public TestListAdapter(List<TestModel> testList) {
        this.testList = testList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.test_item_layout,viewGroup,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        int time = testList.get(i).getTime();
        viewHolder.setData(i,time, this);
    }

    @Override
    public int getItemCount() {
        return testList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView testNo;
        private ImageView deleteTest;
        private Dialog loadingDialog;
        private Dialog editTestDialog;
        //private TextView progressText;
        //private ProgressBar progressBar;
        private EditText dialogTestTime;
        private TextView dialogTitle;
        private Button dialogBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            testNo = itemView.findViewById(R.id.testNo);
            deleteTest = itemView.findViewById(R.id.delete_test);
          //  progressText = itemView.findViewById(R.id.progressText);
         //   progressBar  = itemView.findViewById(R.id.testProgressbar);

            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

            editTestDialog = new Dialog(itemView.getContext());
            editTestDialog.setContentView(R.layout.add_category_dialog);
            editTestDialog.setCancelable(true);
            editTestDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

            dialogTestTime = editTestDialog.findViewById(R.id.ac_cat_name);
            dialogBtn = editTestDialog.findViewById(R.id.ac_add_btn);
            dialogTitle = editTestDialog.findViewById(R.id.title);

        }

        private void setData(final int pos, final int time, final TestListAdapter adapter)
        {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DbQuery.selected_test_index = pos;
                    Intent intent = new Intent(itemView.getContext(), QuestionActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });


            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    dialogTitle.setText("Edit Test");
                    dialogBtn.setText("UPDATE");
                    dialogTestTime.setHint("Enter Test Time in Minutes");
                    dialogTestTime.setText(String.valueOf(time));
                    editTestDialog.show();

                    return false;
                }
            });


            testNo.setText("Test No. : " + String.valueOf(pos + 1));
           // progressText.setText("Your Maximum Score : " + String.valueOf(progress) + " %");

          //  progressBar.setProgress(progress);

            dialogBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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


                    updateTest(pos, Integer.valueOf(dialogTestTime.getText().toString()), itemView.getContext(), adapter);

                }
            });



            deleteTest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());

                    builder.setCancelable(true);

                    View dialogview = LayoutInflater.from(itemView.getContext()).inflate(R.layout.delete_warning_dialog,null);

                    Button cancel = dialogview.findViewById(R.id.et_cancelB);
                    Button confirm = dialogview.findViewById(R.id.et_confirmB);
                    TextView title = dialogview.findViewById(R.id.et_title);
                    TextView msg = dialogview.findViewById(R.id.et_message);

                    title.setText("Delete Test");
                    msg.setText("Do You want to Delete this Test ?");

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
                            deleteTest(pos, itemView.getContext(), adapter);
                        }
                    });

                    alertDialog.show();


                }
            });

        }


        private void deleteTest(int pos, final Context context, final TestListAdapter adapter)
        {
            loadingDialog.show();

            deleteTestFromDB(pos, new OnCompleteListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context,"Test deleted Successfully",Toast.LENGTH_SHORT).show();
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

        private void updateTest(final int pos, int time, final Context context, final TestListAdapter adapter)
        {
            editTestDialog.dismiss();
            loadingDialog.show();

            updateTestDb(pos, time, new OnCompleteListener() {
                @Override
                public void onSuccess() {

                    Toast.makeText(context,"Test Data updated Successfully",Toast.LENGTH_SHORT).show();

                    adapter.notifyItemChanged(pos);

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
