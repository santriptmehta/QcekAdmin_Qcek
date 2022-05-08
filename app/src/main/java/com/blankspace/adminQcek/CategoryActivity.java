package com.blankspace.adminQcek;

import android.app.Dialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.blankspace.adminQcek.DbQuery.addNewSubject;
import static com.blankspace.adminQcek.DbQuery.g_catList;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView catGridView;
    private Button addNewSub;
    private Dialog loadingDialog, addCatDialog;
    private EditText dialogCatName;
    private Button dialogAddB;
    private CategoryAdapter adapter;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);


        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Categories");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        catGridView = findViewById(R.id.cat_view);
        addNewSub = findViewById(R.id.add_new_sub);


        loadingDialog = new Dialog(CategoryActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        addCatDialog = new Dialog(CategoryActivity.this);
        addCatDialog.setContentView(R.layout.add_category_dialog);
        addCatDialog.setCancelable(true);
        addCatDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogCatName = addCatDialog.findViewById(R.id.ac_cat_name);
        dialogAddB = addCatDialog.findViewById(R.id.ac_add_btn);

        // loadCategoryData();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        catGridView.setLayoutManager(layoutManager);

        adapter = new CategoryAdapter(g_catList);
        catGridView.setAdapter(adapter);

        addNewSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isMultiClick(1000))
                {
                    return;
                }

                dialogCatName.getText().clear();
                addCatDialog.show();
            }
        });



        dialogAddB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isMultiClick(1000))
                {
                    return;
                }

                if(dialogCatName.getText().toString().isEmpty())
                {
                    dialogCatName.setError("Enter Category Name");
                    return;
                }

                addSubject(dialogCatName.getText().toString());
            }
        });

    }

    private void addSubject(String title)
    {
        addCatDialog.dismiss();
        loadingDialog.show();

        addNewSubject(title, new OnCompleteListener() {
            @Override
            public void onSuccess() {

                Toast.makeText(CategoryActivity.this, "Category Added Successfully", Toast.LENGTH_SHORT).show();

                adapter.notifyItemInserted(g_catList.size());

                loadingDialog.dismiss();

            }

            @Override
            public void onFailure() {
                Toast.makeText(CategoryActivity.this,"Something went wrong ! Please Try Again Later ",Toast.LENGTH_SHORT).show();
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
            CategoryActivity.this.finish();
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
