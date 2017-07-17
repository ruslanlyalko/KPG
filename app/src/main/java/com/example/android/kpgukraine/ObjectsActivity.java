package com.example.android.kpgukraine;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.android.kpgukraine.models.ObjectModel;
import com.example.android.kpgukraine.utils.Const;
import com.example.android.kpgukraine.utils.ObjectAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ObjectsActivity extends AppCompatActivity {

    //views
    FloatingActionButton fab;
    RecyclerView recyclerViewCategory;

    // Global variables
    private ObjectAdapter adapter;
    private List<ObjectModel> objectModelList = new ArrayList<>();
    private boolean isAdmin = false;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    // variables
    String subCategoryKey, subCategoryTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objects);

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
        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategory.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCategory.setAdapter(adapter);


        loadObjectsFromDB();

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


        // todo add new inputs for location and time opened

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

                if (objectModel.subCategoryKey.equals(subCategoryKey)) {
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

