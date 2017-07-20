package com.example.android.kpgukraine;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.kpgukraine.models.EventModel;
import com.example.android.kpgukraine.models.ObjectModel;
import com.example.android.kpgukraine.utils.Const;
import com.example.android.kpgukraine.utils.EventAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ObjectActivity extends AppCompatActivity {

    //views
    FloatingActionButton fab;
    RecyclerView recyclerViewCategory;
    TextView textTitle, textAddress, textTime, textPhone, textEvents, textDescription, textDescriptionTitle;
    LinearLayout panelAddress, panelTime, panelPhone;
    ImageView imageView;

    // Global variables
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private EventAdapter adapter;
    private List<EventModel> eventModelList = new ArrayList<>();
    private boolean isAdmin = false;

    private String objectKey, objectTitle;
    private ObjectModel objectModel = new ObjectModel();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object);
        //initCollapsingToolbar();

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

        loadObjectDetailsFromDB(objectKey);
        loadEventsFromDB();
    }
/*
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar1);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar1);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.title_activity_oject));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }*/

    private void loadObjectDetailsFromDB(String objectKey) {

        DatabaseReference categoriesRef = database.getReference(Const.DB_REF_OBJECTS).child(objectKey);

        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                objectModel = dataSnapshot.getValue(ObjectModel.class);

                updateUI(objectModel);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updateUI(final ObjectModel objectModel) {

        textTitle.setText(objectModel.getTitle());

        setImage(objectModel.getImageUri(), imageView);
        textPhone.setText(objectModel.getPhone());
        textAddress.setText(objectModel.getAddress());
        textTime.setText(objectModel.getTimeOpened());

        if (objectModel.getDescription() != null && !objectModel.getDescription().isEmpty())
            textDescription.setText(objectModel.getDescription());
        else {
            textDescription.setVisibility(View.GONE);
            textDescriptionTitle.setVisibility(View.GONE);
        }

        panelPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + getFirstNumber(objectModel.getPhone())));
                startActivity(callIntent);
            }
        });

        panelAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //todo open map
                // longitude, latitude
            }
        });
    }

    /**
     * Function to get first phone number in string variable
     *
     * @param phone input phone number
     * @return first phone number
     */
    private String getFirstNumber(String phone) {

        int firstComma = phone.indexOf(",");
        int firstDot = phone.indexOf(".");

        if (firstComma > 0 || firstDot > 0) {

            if (firstComma > 0 && firstComma < firstDot)
                return phone.substring(0, firstComma);
            else
                return phone.substring(0, firstDot);
        }

        return phone;
    }

    private void setImage(String imageUri, final ImageView targetImageView) {

        if (imageUri != null && !imageUri.isEmpty()) {
            if (imageUri.contains("http")) {
                // set image from URL provided by user
                Glide.with(getApplicationContext()).load(imageUri).into(targetImageView);

            } else {
                final StorageReference ref = storage.getReference(Const.STORE_OBJECT_IMAGES).child(imageUri);

                // check if image is exist on Storage, otherwise load default image
                ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Glide.with(getApplicationContext()).using(new FirebaseImageLoader()).load(ref).into(targetImageView);
                        } else {
                            StorageReference ref = storage.getReference(Const.STORE_OBJECT_IMAGES).child(Const.DEF_IMAGE);
                            Glide.with(getApplicationContext()).using(new FirebaseImageLoader()).load(ref).into(targetImageView);
                        }
                    }
                });
            }

        } else {
            //set default image if user dn't enter any link
            StorageReference ref = storage.getReference(Const.STORE_OBJECT_IMAGES).child(Const.DEF_IMAGE);
            Glide.with(getApplicationContext()).using(new FirebaseImageLoader()).load(ref).into(targetImageView);
        }
    }


    private void addNewEvent() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_event_title);


        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_new_event, null, false);
        builder.setView(viewInflated);

        final EditText editTitle = (EditText) viewInflated.findViewById(R.id.edit_title);
        final EditText editDescription = (EditText) viewInflated.findViewById(R.id.edit_description);

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

                if (eventModel != null && eventModel.objectKey.equals(objectKey)) {
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
        imageView = (ImageView) findViewById(R.id.image);
        panelAddress = (LinearLayout) findViewById(R.id.panel_address);
        panelTime = (LinearLayout) findViewById(R.id.panel_time);
        panelPhone = (LinearLayout) findViewById(R.id.panel_phone);

        textTitle = (TextView) findViewById(R.id.text_title);
        textAddress = (TextView) findViewById(R.id.text_address);
        textTime = (TextView) findViewById(R.id.text_time);
        textPhone = (TextView) findViewById(R.id.text_phone);
        textDescription = (TextView) findViewById(R.id.text_description);
        textDescriptionTitle = (TextView) findViewById(R.id.text_description_title);
        textEvents = (TextView) findViewById(R.id.text_events);
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

