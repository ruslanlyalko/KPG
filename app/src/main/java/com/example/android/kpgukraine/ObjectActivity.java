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

import com.example.android.kpgukraine.models.EventModel;
import com.example.android.kpgukraine.models.ObjectModel;
import com.example.android.kpgukraine.utils.Const;
import com.example.android.kpgukraine.utils.EventAdapter;
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

public class ObjectActivity extends AppCompatActivity {

    //views
    FloatingActionButton fab;
    RecyclerView recyclerViewCategory;

    // Global variables
    private EventAdapter adapter;
    private List<EventModel> eventModelList = new ArrayList<>();
    private boolean isAdmin = false;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    // variables
    String objectKey, objectTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            objectKey = bundle.getString(Const.EXTRA_SUB_CAT_KEY);
            objectTitle = bundle.getString(Const.EXTRA_SUB_CAT_TITLE);

            isAdmin = bundle.getBoolean(Const.EXTRA_IS_ADMIN);
        }

        setTitle(objectTitle);

        initRef();

        fab.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewEvent();
            }
        });

        adapter = new EventAdapter(this, eventModelList, isAdmin);
        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategory.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCategory.setAdapter(adapter);

        loadEventsFromDB();
    }


    private void addNewEvent() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_event_title);


        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_new_event, null, false);
        builder.setView(viewInflated);

        final EditText editTitle = (EditText) viewInflated.findViewById(R.id.edit_title);
        final EditText editDescription = (EditText) viewInflated.findViewById(R.id.edit_description);



        // todo add new inputs for location and time opened

        final Spinner spinnerCategory = (Spinner) viewInflated.findViewById(R.id.spinner_category);
        spinnerCategory.setVisibility(View.GONE);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String title1 = editTitle.getText().toString();
                String description = editDescription.getText().toString();


                EventModel newObject = new EventModel(objectKey, title1, description);

                addNewEventToDB(newObject);

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
    private void addNewEventToDB(EventModel eventModel) {
        DatabaseReference objectsRef = database.getReference(Const.DB_REF_EVENTS);

        String objectKey = objectsRef.push().getKey();

        eventModel.setKey(objectKey);

        objectsRef.child(objectKey).setValue(eventModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        adapter.notifyDataSetChanged();
                    }
                });

    }


    private void loadEventsFromDB() {

        eventModelList.clear();
        adapter.notifyDataSetChanged();

        DatabaseReference categoriesRef = database.getReference(Const.DB_REF_EVENTS);

        categoriesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                EventModel eventModel = dataSnapshot.getValue(EventModel.class);

                if (eventModel.objectKey.equals(objectKey)) {
                    eventModelList.add(eventModel);
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

