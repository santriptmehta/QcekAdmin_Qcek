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

import static com.blankspace.adminQcek.DbQuery.deleteSubject;
import static com.blankspace.adminQcek.DbQuery.updateSubjectName;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<CategoryModel> catList;

    public CategoryAdapter(List<CategoryModel> catList) {
        this.catList = catList;
    }


    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {

        String name = catList.get(position).getName();
        int test_count = catList.get(position).getNoOfTest();

        holder.setData(position,name,test_count, this);
    }

    @Override
    public int getItemCount() {
        return catList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView catName, testCount;
        private ImageView deleteB;
        private Dialog loadingDialog, editDialog;
        private TextView tv_editCatName;
        private Button updateCatB;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            catName = itemView.findViewById(R.id.catName);
            testCount = itemView.findViewById(R.id.no_of_tests);
            deleteB = itemView.findViewById(R.id.delete_sub);

            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


            editDialog = new Dialog(itemView.getContext());
            editDialog.setContentView(R.layout.edit_category_dialog);
            editDialog.setCancelable(true);
            editDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

            tv_editCatName = editDialog.findViewById(R.id.ec_cat_name);
            updateCatB = editDialog.findViewById(R.id.ec_add_btn);


        }

        private void setData(final int position, final String name, int test_count, final CategoryAdapter adapter)
        {

            catName.setText(name);
            testCount.setText(String.valueOf(test_count) + " Tests");


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DbQuery.selected_cat_index = position;
                    Intent intent = new Intent(v.getContext(), TestActivity.class);
                    v.getContext().startActivity(intent);
                }
            });


            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    tv_editCatName.setText(name);
                    editDialog.show();

                    return false;
                }
            });


            updateCatB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(tv_editCatName.getText().toString().isEmpty())
                    {
                        tv_editCatName.setError("Enter Subject Name");
                        return;
                    }

                    updateCategory(position,tv_editCatName.getText().toString(), itemView.getContext(), adapter);

                }
            });


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

                    title.setText("Delete Subject");
                    msg.setText("Do You want to Delete this Subject ?");

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
                            deleteCategory(position, itemView.getContext(), adapter);
                        }
                    });

                    alertDialog.show();

                }
            });

        }

        private void deleteCategory(int pos, final Context context, final CategoryAdapter adapter)
        {
            loadingDialog.show();

            deleteSubject(pos, new OnCompleteListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context,"Subject deleted Successfully",Toast.LENGTH_SHORT).show();
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


        private void updateCategory(final int pos, String name, final Context context, final CategoryAdapter adapter)
        {
            editDialog.dismiss();
            loadingDialog.show();

            updateSubjectName(pos, name, new OnCompleteListener() {
                @Override
                public void onSuccess() {

                    Toast.makeText(context,"Subject Name Changed Successfully",Toast.LENGTH_SHORT).show();

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
