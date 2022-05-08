package com.blankspace.adminQcek;

import android.util.ArrayMap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;

public class DbQuery {

    public static FirebaseFirestore g_firestore;
    public static List<CategoryModel> g_catList = new ArrayList<>();
    public static int selected_cat_index = 0;

    public static List<TestModel> g_testList = new ArrayList<>();
    public static int selected_test_index = 0;

    public static List<QuestionModel> g_quesList = new ArrayList<>();
    public static boolean authorised = false;


    public static void addNewQuestion(final String qStr, final String aStr, final String bStr, final String cStr, final String dStr, final int ans, final OnCompleteListener onCompleteListener)
    {

        Map<String,Object> quesData = new ArrayMap<>();

        quesData.put("CATEGORY", g_catList.get(selected_cat_index).getDocID());
        quesData.put("TEST", g_testList.get(selected_test_index).getTestID());
        quesData.put("QUESTION",qStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("D",dStr);
        quesData.put("ANSWER",ans);


        final String doc_id = g_firestore.collection("Questions")
                                .document().getId();

        quesData.put("Q_ID",doc_id);

        g_firestore.collection("Questions")
                .document(doc_id)
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                                        g_quesList.add(new QuestionModel(
                                                doc_id,
                                                qStr, aStr, bStr, cStr, dStr,
                                                ans
                                        ));

                                        onCompleteListener.onSuccess();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        onCompleteListener.onFailure();

                    }
                });

    }


    public static void editQuestion(final int qNo, final String qStr, final String aStr, final String bStr, final String cStr, final String dStr, final int ans, final OnCompleteListener onCompleteListener)
    {

        Map<String,Object> quesData = new ArrayMap<>();
        quesData.put("QUESTION", qStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("D",dStr);
        quesData.put("ANSWER",ans);


        g_firestore.collection("Questions")
                .document(g_quesList.get(qNo).getQuesID())
                .update(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        g_quesList.get(qNo).setQuestion(qStr);
                        g_quesList.get(qNo).setOptionA(aStr);
                        g_quesList.get(qNo).setOptionB(bStr);
                        g_quesList.get(qNo).setOptionC(cStr);
                        g_quesList.get(qNo).setOptionD(dStr);
                        g_quesList.get(qNo).setCorrectAns(ans);

                        onCompleteListener.onSuccess();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        onCompleteListener.onFailure();
                    }
                });

    }

    public static void deleteQuestion(final int qNo, final OnCompleteListener onCompleteListener)
    {

        g_firestore.collection("Questions")
                .document(g_quesList.get(qNo).getQuesID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        g_quesList.remove(qNo);

                        onCompleteListener.onSuccess();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        onCompleteListener.onFailure();
                    }
                });

    }


    public static void addNewSubject(final String title, final OnCompleteListener onCompleteListener)
    {

        WriteBatch batch = g_firestore.batch();

        final DocumentReference  catDoc = g_firestore.collection("QUIZ").document();

        final Map<String,Object> catData = new ArrayMap<>();
        catData.put("NAME",title);
        catData.put("NO_OF_TESTS",0);
        catData.put("CAT_ID",catDoc.getId());

        batch.set(catDoc,catData);

        Map<String,Object> catListData = new ArrayMap<>();
        catListData.put("CAT" + String.valueOf(g_catList.size() + 1) + "_ID",catDoc.getId());
        catListData.put("COUNT", g_catList.size() + 1);

        DocumentReference catListDoc = g_firestore.collection("QUIZ").document("Categories");

        batch.update(catListDoc,catListData);


        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                g_catList.add(new CategoryModel(catDoc.getId(),title,0));

                onCompleteListener.onSuccess();

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        onCompleteListener.onFailure();
                    }
                });

    }


    public static void deleteSubject(final int id, final OnCompleteListener onCompleteListener)
    {


        g_firestore.collection("Questions").whereEqualTo("CATEGORY",g_catList.get(id).getDocID())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        WriteBatch batch = g_firestore.batch();

                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots)
                        {
                            batch.delete(doc.getReference());
                        }


                        DocumentReference testDoc = g_firestore.collection("QUIZ").document(g_catList.get(id).getDocID())
                                .collection("TESTS_LIST").document("TESTS_INFO");

                        batch.delete(testDoc);

                        DocumentReference catDoc = g_firestore.collection("QUIZ").document(g_catList.get(id).getDocID());

                        batch.delete(catDoc);

                        Map<String,Object> catData = new ArrayMap<>();
                        int index=1;
                        for(int i=0; i < g_catList.size(); i++)
                        {
                            if( i != id)
                            {
                                catData.put("CAT" + String.valueOf(index) + "_ID", g_catList.get(i).getDocID());
                                index++;
                            }

                        }

                        catData.put("COUNT", index - 1);

                        DocumentReference catListDoc = g_firestore.collection("QUIZ").document("Categories");

                        batch.set(catListDoc, catData);


                        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                g_catList.remove(id);

                                onCompleteListener.onSuccess();

                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        onCompleteListener.onFailure();
                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onCompleteListener.onFailure();
                    }
                });

    }


    public static void updateSubjectName(final int id, final String name, final OnCompleteListener onCompleteListener)
    {

        g_firestore.collection("QUIZ").document(g_catList.get(id).getDocID())
                .update("NAME",name)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        g_catList.get(id).setName(name);

                        onCompleteListener.onSuccess();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        onCompleteListener.onFailure();
                    }
                });
    }


    public static void updateTestDb(final int id, final int time, final OnCompleteListener onCompleteListener)
    {
        String key = "TEST" + String.valueOf(id + 1) + "_TIME";

        g_firestore.collection("QUIZ").document(g_catList.get(selected_cat_index).getDocID())
                .collection("TESTS_LIST").document("TESTS_INFO")
                .update(key,time)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        g_testList.get(id).setTime(time);

                        onCompleteListener.onSuccess();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        onCompleteListener.onFailure();
                    }
                });
    }


    public static void addNewTest(final int time, final OnCompleteListener onCompleteListener)
    {

        String key = "TEST" + String.valueOf(g_testList.size() + 1) + "_ID";
        String key2 = "TEST" + String.valueOf(g_testList.size() + 1) + "_TIME";
        final String testID = UUID.randomUUID().toString();

        Map<String, Object> data = new ArrayMap<>();

        data.put(key,testID);
        data.put(key2,time);

        WriteBatch batch = g_firestore.batch();

        DocumentReference testDoc = g_firestore.collection("QUIZ").document(g_catList.get(selected_cat_index).getDocID())
                .collection("TESTS_LIST").document("TESTS_INFO");

        batch.set(testDoc,data,SetOptions.merge());

        DocumentReference catDoc = g_firestore.collection("QUIZ").document(g_catList.get(selected_cat_index).getDocID());

        batch.update(catDoc,"NO_OF_TESTS", g_testList.size() + 1 );


        batch.commit()
                 .addOnSuccessListener(new OnSuccessListener<Void>() {
                     @Override
                     public void onSuccess(Void aVoid) {

                         g_testList.add(new TestModel(testID,time));
                         g_catList.get(selected_cat_index).setNoOfTest(g_testList.size());

                         onCompleteListener.onSuccess();

                     }
                 })
                 .addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {

                         onCompleteListener.onFailure();
                     }
                 });
    }


    public static void deleteTestFromDB(final int id, final OnCompleteListener onCompleteListener)
    {

        g_firestore.collection("Questions").whereEqualTo("TEST",g_testList.get(id).getTestID())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        WriteBatch batch = g_firestore.batch();

                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots)
                        {
                            batch.delete(doc.getReference());
                        }


                        DocumentReference testDoc = g_firestore.collection("QUIZ").document(g_catList.get(selected_cat_index).getDocID())
                                .collection("TESTS_LIST").document("TESTS_INFO");

                        Map<String, Object> testListData = new ArrayMap<>();
                        int index=1;
                        for(int i=0; i< g_testList.size();  i++)
                        {
                            if(i != id)
                            {
                                testListData.put("TEST" + String.valueOf(index) + "_ID", g_testList.get(i).getTestID());
                                testListData.put("TEST" + String.valueOf(index) + "_TIME", g_testList.get(i).getTime());
                                index++;
                            }
                        }

                        batch.set(testDoc,testListData);

                        DocumentReference catDoc = g_firestore.collection("QUIZ").document(g_catList.get(selected_cat_index).getDocID());

                        batch.update(catDoc,"NO_OF_TESTS", g_testList.size() - 1 );


                        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                g_testList.remove(id);

                                g_catList.get(selected_cat_index).setNoOfTest(g_testList.size());

                                onCompleteListener.onSuccess();

                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        onCompleteListener.onFailure();

                                    }
                                });




                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        onCompleteListener.onFailure();
                    }
                });

    }


    public static void loadTestData(final OnCompleteListener onCompleteListener)
    {

        g_testList.clear();

        CollectionReference collectionRef = g_firestore.collection("QUIZ");

        collectionRef.document(g_catList.get(selected_cat_index).getDocID())
                .collection("TESTS_LIST").document("TESTS_INFO")
                .get()
                .addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.isSuccessful())
                        {
                            DocumentSnapshot doc = task.getResult();

                            int noOfTests = g_catList.get(selected_cat_index).getNoOfTest();

                            for(int t=1; t <= noOfTests; t++) {
                                g_testList.add(new TestModel(
                                        doc.getString("TEST" + String.valueOf(t) + "_ID"),
                                        doc.getLong("TEST" + String.valueOf(t) + "_TIME").intValue()
                                ));
                            }


                           onCompleteListener.onSuccess();

                        }
                        else
                        {
                            //Log.d("LOGGGG" , "failed  : " + task.getException().getMessage());
                            if(onCompleteListener != null)
                                onCompleteListener.onFailure();
                        }

                    }
                });


    }


    public static void loadCategories(final OnCompleteListener onCompleteListener)
    {

        g_catList.clear();
        // Log.d("LOGGG","in loadData");

        CollectionReference collectionRef = g_firestore.collection("QUIZ");

        collectionRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Map<String,QueryDocumentSnapshot> docList = new ArrayMap<>();

                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots)
                        {
                            docList.put(doc.getId(),doc);
                        }

                        QueryDocumentSnapshot catListDoc = docList.get("Categories");

                        long catCount = catListDoc.getLong("COUNT");

                        for( int i=1; i <= catCount; i++)
                        {

                            String catID = catListDoc.getString("CAT" + String.valueOf(i) + "_ID");

                            QueryDocumentSnapshot catDoc = docList.get(catID);

                            int noOfTests = catDoc.getLong("NO_OF_TESTS").intValue();

                            g_catList.add(new CategoryModel(
                                    catID,
                                    catDoc.getString("NAME"),
                                    noOfTests
                            ));


                        }


                        //Call callback
                        if(onCompleteListener != null)
                            onCompleteListener.onSuccess();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                        if(onCompleteListener != null)
                            onCompleteListener.onFailure();
                    }
                });
    }


    public static void checkPermissions(final  String uid, final OnCompleteListener onCompleteListener)
    {
        g_firestore.collection("USERS").document(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        Boolean isAdmin = documentSnapshot.getBoolean("IS_ADMIN");

                       // Log.d("LOGGG",String.valueOf(isAdmin));

                        if(isAdmin != null && isAdmin == true)
                        {
                            authorised = true;
                        }
                        else
                        {
                            authorised = false;
                        }

                        onCompleteListener.onSuccess();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        onCompleteListener.onFailure();
                    }
                });

    }

    public static void loadData(final OnCompleteListener onCompleteListener)
    {


        loadCategories(new OnCompleteListener() {
            @Override
            public void onSuccess() {

                onCompleteListener.onSuccess();
            }

            @Override
            public void onFailure()
            {
                onCompleteListener.onFailure();
            }

        });

    }


}
