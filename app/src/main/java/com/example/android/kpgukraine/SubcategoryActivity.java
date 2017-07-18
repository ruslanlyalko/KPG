package com.example.android.kpgukraine;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.android.kpgukraine.models.ObjectModel;
import com.example.android.kpgukraine.utils.Const;
import com.example.android.kpgukraine.utils.ObjectAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SubCategoryActivity extends AppCompatActivity {

    //views
    FloatingActionButton fab;
    RecyclerView recyclerViewCategory;

    // Global variables
    private ObjectAdapter adapter;
    private List<ObjectModel> objectModelList = new ArrayList<>();
    private boolean isAdmin = false;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // variables
    String subCategoryKey, subCategoryTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcategory);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            subCategoryKey = bundle.getString(Const.EXTRA_SUB_CAT_KEY);
            subCategoryTitle = bundle.getString(Const.EXTRA_SUB_CAT_TITLE);

            isAdmin = bundle.getBoolean(Const.EXTRA_IS_ADMIN);
        }

        setTitle(subCategoryTitle);

        initRef();

        fab.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewObject();
            }
        });

        adapter = new ObjectAdapter(this, objectModelList, isAdmin);

        recyclerViewCategory.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));

        recyclerViewCategory.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerViewCategory.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCategory.setAdapter(adapter);


        loadObjectsFromDB();

    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    private class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }

    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    /**
     * Add new Category
     */
    private void addNewObject() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_object_title);


        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_new_object, null, false);
        builder.setView(viewInflated);

        final EditText editTitle = (EditText) viewInflated.findViewById(R.id.edit_title);
        final EditText editDescription = (EditText) viewInflated.findViewById(R.id.edit_description);
        final EditText editAddress = (EditText) viewInflated.findViewById(R.id.edit_address);
        final EditText editDinner = (EditText) viewInflated.findViewById(R.id.edit_dinner);
        final EditText editPhone = (EditText) viewInflated.findViewById(R.id.edit_phone);
        final EditText editTime = (EditText) viewInflated.findViewById(R.id.edit_time);
        final EditText editUri = (EditText) viewInflated.findViewById(R.id.edit_uri);
        final EditText editLatitude = (EditText) viewInflated.findViewById(R.id.edit_latitude);
        final EditText editLongitude = (EditText) viewInflated.findViewById(R.id.edit_longitude);

        final Spinner spinnerCategory = (Spinner) viewInflated.findViewById(R.id.spinner_category);
        spinnerCategory.setVisibility(View.GONE);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String title1 = editTitle.getText().toString();
                String description = editDescription.getText().toString();
                String address = editAddress.getText().toString();
                String dinner = editDinner.getText().toString();
                String phone = editPhone.getText().toString();
                String time = editTime.getText().toString();
                String image = editUri.getText().toString();
                String latitude = editLatitude.getText().toString();
                String longitude = editLongitude.getText().toString();

                ObjectModel newObject = new ObjectModel(subCategoryKey, title1, description, address,
                        dinner, phone, time, image, latitude, longitude);

                addNewObjectToDB(newObject);

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
    private void addNewObjectToDB(ObjectModel objectModel) {
        DatabaseReference objectsRef = database.getReference(Const.DB_REF_OBJECTS);

        String objectKey = objectsRef.push().getKey();

        objectModel.setKey(objectKey);

        objectsRef.child(objectKey).setValue(objectModel)
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
    private void loadObjectsFromDB() {

        objectModelList.clear();
        adapter.notifyDataSetChanged();

        DatabaseReference categoriesRef = database.getReference(Const.DB_REF_OBJECTS);

        categoriesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ObjectModel objectModel = dataSnapshot.getValue(ObjectModel.class);

                if (objectModel != null && objectModel.subCategoryKey.equals(subCategoryKey)) {
                    objectModelList.add(objectModel);
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

    /**
     * Initialize all views references
     */
    private void initRef() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        recyclerViewCategory = (RecyclerView) findViewById(R.id.recycler_view_category);

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

