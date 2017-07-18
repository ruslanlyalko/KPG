package com.example.android.kpgukraine;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.android.kpgukraine.models.SubCategory;
import com.example.android.kpgukraine.utils.Const;
import com.example.android.kpgukraine.utils.SubCategoryAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    //views
    FloatingActionButton fab;
    RecyclerView recyclerViewCategory;

    // Global variables
    private SubCategoryAdapter adapter;
    private List<SubCategory> subCategoryList = new ArrayList<>();
    private boolean isAdmin = false;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // variables
    String categoryKey, categoryTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            categoryKey = bundle.getString(Const.EXTRA_CAT_KEY);
            categoryTitle = bundle.getString(Const.EXTRA_CAT_TITLE);
            isAdmin = bundle.getBoolean(Const.EXTRA_IS_ADMIN);
        }

        setTitle(categoryTitle);

        initRef();

        fab.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewSubCategory();
            }
        });

        adapter = new SubCategoryAdapter(this, subCategoryList, isAdmin);
        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategory.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCategory.setAdapter(adapter);

        loadSubCategoriesFromDB();

    }

    /**
     * Add new Category
     */
    private void addNewSubCategory() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_subcategory_title);


        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_new_subcategory, null, false);
        builder.setView(viewInflated);
        final EditText inputTitle1 = (EditText) viewInflated.findViewById(R.id.text_title1);
        final EditText inputUri = (EditText) viewInflated.findViewById(R.id.text_uri);
        final Spinner spinnerCategory = (Spinner) viewInflated.findViewById(R.id.spinner_category);

        spinnerCategory.setVisibility(View.GONE);
        /*
        String [] arraySpinner = new String[] {subCategoryTitle};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arraySpinner);
        spinnerCategory.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        */
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String title1 = inputTitle1.getText().toString();
                String uri = inputUri.getText().toString();

                addNewSubCategoryToDB(title1, uri);

            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Save new category to db
     */
    private void addNewSubCategoryToDB(String title1, String uri) {
        DatabaseReference subcategoriesRef = database.getReference(Const.DB_REF_SUBCATEGORIES);

        String key1 = subcategoriesRef.push().getKey();

        subcategoriesRef.child(key1).setValue(new SubCategory(key1, title1, uri, categoryKey))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        adapter.notifyDataSetChanged();
                    }
                });

    }

    /**
     * Load all Categories from Firebase DB
     */
    private void loadSubCategoriesFromDB() {

        subCategoryList.clear();
        adapter.notifyDataSetChanged();

        DatabaseReference categoriesRef = database.getReference(Const.DB_REF_SUBCATEGORIES);

        categoriesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                SubCategory subCategory = dataSnapshot.getValue(SubCategory.class);

                // load subcategories only for current category
                if (subCategory != null && subCategory.getCategoryKey().equals(categoryKey)) {
                    subCategoryList.add(subCategory);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    /**
     * Initialize all views references
     */
    private void initRef() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        recyclerViewCategory = (RecyclerView) findViewById(R.id.recycler_view_category);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(getApplicationContext(), SearchResultActivity.class)));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}

