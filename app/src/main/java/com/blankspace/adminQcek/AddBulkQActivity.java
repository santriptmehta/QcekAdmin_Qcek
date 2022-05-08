package com.blankspace.adminQcek;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.OpenableColumns;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import static com.blankspace.adminQcek.DbQuery.g_catList;
import static com.blankspace.adminQcek.DbQuery.g_firestore;
import static com.blankspace.adminQcek.DbQuery.g_testList;
import static com.blankspace.adminQcek.DbQuery.loadTestData;
import static com.blankspace.adminQcek.DbQuery.selected_cat_index;

public class AddBulkQActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private TextView fileName;
    private Button selectB, addB;
    private Uri fileUri;
    private Spinner spinner;
    private Dialog loadingDialog;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bulk_q);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Add Bulk Questions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        fileName = findViewById(R.id.fliename);
        selectB = findViewById(R.id.select_fileB);
        addB = findViewById(R.id.add_from_excel);
        spinner = findViewById(R.id.spinner);

        loadingDialog = new Dialog(AddBulkQActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        selected_cat_index = -1;

        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();

        categories.add("<----SELECT---->");

        for(int i=0; i < g_catList.size(); i++)
        {
            categories.add(g_catList.get(i).getName());
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        fileUri = null;

        selectB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isMultiClick(2000))
                {
                    return;
                }

                if(selected_cat_index < 0)
                {
                    Toast.makeText(AddBulkQActivity.this, " Select Category First ",Toast.LENGTH_SHORT).show();
                    return;
                }

                checkPermission();
            }
        });

        addB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isMultiClick(2000))
                {
                    return;
                }

                if(fileUri == null)
                {
                    Toast.makeText(AddBulkQActivity.this, " Select File First ",Toast.LENGTH_SHORT).show();
                    return;
                }

                readFile();

            }
        });

    }


    private void checkPermission() {

        if (ActivityCompat.checkSelfPermission(AddBulkQActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            selectFile();

        }
        else {
            ActivityCompat.requestPermissions(AddBulkQActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 222);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 222) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                selectFile();

            } else {
                Toast.makeText(AddBulkQActivity.this, "Please give permission", Toast.LENGTH_SHORT).show();
            }
        }

    }


    private void selectFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 333);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 333) {
            if (resultCode == RESULT_OK) {

                Toast.makeText(AddBulkQActivity.this,"File Selected !",Toast.LENGTH_SHORT).show();
                Uri uri = data.getData();
                //readFile(uri);

                fileUri = uri;

                String file_name = getFileName(uri);

                fileName.setText("FileName : " + file_name);

            }

        }

    }


    private String getFileName(Uri uri) throws IllegalArgumentException {
        // Obtain a cursor with information regarding this uri
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            throw new IllegalArgumentException("Can't obtain file name, cursor is empty");
        }

        cursor.moveToFirst();

        String fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

        cursor.close();

        return fileName;
    }


    private void readFile() {
        //File file = new File(path);

        loadingDialog.show();

        WriteBatch batch  = g_firestore.batch();
      //  int index = questionsList.size();

        //Log.d("LOGGGG",uri.getPath());

        try {
            // InputStream inputStream = new FileInputStream(file);
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);


            Iterator<Row> iterator = sheet.iterator();

            //int rowsCount = sheet.getPhysicalNumberOfRows();
            //if (rowsCount > 0) {
            //    for (int i = 0; i < rowsCount; i++) {

            int i=0;
            while (iterator.hasNext()) {

                Row row = iterator.next();
                 //   Row row = sheet.getRow(i);

                int cellCount = row.getPhysicalNumberOfCells();

                //Log.d("LOGGGGG","cell count = " + String.valueOf(cellCount) + " row = " + String.valueOf(i+1));
                if(cellCount != 7)
                    continue;

                    //check test ID
                    int test_index = 0;
                    String testNo = getCellData(row,0);

                    try {
                        test_index = Integer.valueOf(testNo);
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(AddBulkQActivity.this,"Wrong test number value in Row " + String.valueOf(i+1),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                        return;
                    }

                    if(test_index > g_testList.size() || test_index < 1 )
                    {
                        Toast.makeText(AddBulkQActivity.this,"Test number given in Row " + String.valueOf(i+1) + " is not exist.",Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                        return;
                    }


                    String question = getCellData(row,1);
                    String a = getCellData(row,2);
                    String b = getCellData(row,3);
                    String c = getCellData(row,4);
                    String d = getCellData(row,5);
                    String ansValue = getCellData(row,6);

                    int correct = 1;

                    try {
                        correct = Integer.valueOf(ansValue);
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(AddBulkQActivity.this,"Wrong answer value in Row " + String.valueOf(i+1),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                        return;
                    }

                if(correct > 4 || correct < 1 )
                {
                    Toast.makeText(AddBulkQActivity.this,"Wrong answer value in Row " + String.valueOf(i+1) ,Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                    return;
                }


                    Map<String,Object> quesData = new ArrayMap<>();

                    quesData.put("CATEGORY", g_catList.get(selected_cat_index).getDocID());
                    quesData.put("TEST",g_testList.get(test_index-1).getTestID());
                    quesData.put("QUESTION",question);
                    quesData.put("A",a);
                    quesData.put("B",b);
                    quesData.put("C",c);
                    quesData.put("D",d);
                    quesData.put("ANSWER",correct);


                    DocumentReference quesDoc =  g_firestore.collection("Questions").document();

                    quesData.put("Q_ID",quesDoc.getId());

                    batch.set(quesDoc, quesData);

                    i++;
                }


                final int quesCount = i;

                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(AddBulkQActivity.this, String.valueOf(quesCount) + " Questions Added Successfully.", Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddBulkQActivity.this,"Something went wrong ! Please Try Again Later ",Toast.LENGTH_SHORT).show();
                                //Log.d()
                                loadingDialog.dismiss();
                            }
                        });


          /*  } else {
                Toast.makeText(AddBulkQActivity.this, "File is Empty", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            } */

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(AddBulkQActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            loadingDialog.dismiss();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(AddBulkQActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            loadingDialog.dismiss();
        }
    }



    private String getCellData(Row row, int colNum) {

        String value = "";
        Cell cell = row.getCell(colNum);

        if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
        {
            value = String.valueOf((int)cell.getNumericCellValue());
        }
        else if(cell.getCellType() == Cell.CELL_TYPE_STRING)
        {
            value = cell.getStringCellValue();
        }


        return value;
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        //Log.d("LOGGGG", String.valueOf(i));
        selected_cat_index = i - 1;

        fileUri = null;
        fileName.setText("FileName : ");

        if(i > 0) {
            loadingDialog.show();

            loadTestData(new OnCompleteListener() {
                @Override
                public void onSuccess() {

                    loadingDialog.dismiss();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(AddBulkQActivity.this,"Something went wrong ! Please Try Again Later ",Toast.LENGTH_SHORT).show();
                    //Log.d()
                    loadingDialog.dismiss();
                }
            });
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
