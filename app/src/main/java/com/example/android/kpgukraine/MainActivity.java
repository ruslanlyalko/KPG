package com.example.android.kpgukraine;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.kpgukraine.models.Category;
import com.example.android.kpgukraine.utils.CategoryAdapter;
import com.example.android.kpgukraine.utils.Const;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Views
    DrawerLayout drawer;
    NavigationView navigationView;
    FloatingActionButton fab;
    RecyclerView recyclerViewCategory;


    // Global variables
    private CategoryAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();
    private boolean isAdmin = false;
    private boolean isModerator = false;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private boolean doubleBackToExitPressedOnce;
    private Menu menu;
    private String PERSISTENCE_ENABLED = "persistence_enabled";
    private FirebaseRemoteConfig mRemoteConfig = FirebaseRemoteConfig.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Get saved Instant States
        Boolean persistence = false;
        if (savedInstanceState != null) {
            persistence = savedInstanceState.getBoolean(PERSISTENCE_ENABLED);

        }
        // Enable persistence if it's still not enabled
        if (!persistence)
            database.setPersistenceEnabled(true);


        initRef();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewCategory();
            }
        });

        adapter = new CategoryAdapter(this, categoryList, isAdmin || isModerator);
        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategory.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCategory.setAdapter(adapter);


        // load List of Categories and display it on UI
        loadCategoriesFromDB();

        // need to be called after init adapter
        initRemoteConfig();
    }

    private void initRemoteConfig() {
        mRemoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build());

        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("admin_emails", "");
        defaults.put("moderator_emails", "");

        mRemoteConfig.setDefaults(defaults);
        final Task<Void> fetch = mRemoteConfig.fetch(0);

        fetch.addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mRemoteConfig.activateFetched();
                updateUIWithConfig();
            }
        });
    }

    private void updateUIWithConfig() {

        if (auth.getCurrentUser() != null) {

            String email = auth.getCurrentUser().getEmail();

            isAdmin = mRemoteConfig.getString("admin_emails").contains(email);

            isModerator = mRemoteConfig.getString("moderator_emails").contains(email);

        } else {
            isAdmin = false;
            isModerator = false;
        }

        fab.setVisibility(isAdmin || isModerator ? View.VISIBLE : View.GONE);
        adapter.setIsAdmin(isAdmin || isModerator);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(PERSISTENCE_ENABLED, true);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    /**
     * Add new Category
     */
    private void addNewCategory() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_category_title);


        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_new_category, null, false);
        builder.setView(viewInflated);
        final EditText inputTitle1 = (EditText) viewInflated.findViewById(R.id.text_title1);
        final EditText inputUri = (EditText) viewInflated.findViewById(R.id.text_uri);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String title1 = inputTitle1.getText().toString();
                String uri = inputUri.getText().toString();

                addNewCategoryToDB(title1, uri);

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
    private void addNewCategoryToDB(String title1, String uri) {
        DatabaseReference categoriesRef = database.getReference(Const.DB_REF_CATEGORIES);

        String key = categoriesRef.push().getKey();

        categoriesRef.child(key).setValue(new Category(key, title1, uri))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        adapter.notifyDataSetChanged();
                    }
                });

    }

    /**
     * Load all Categories from Firebase DB
     * Listening for any new added categories
     */
    private void loadCategoriesFromDB() {

        categoryList.clear();
        adapter.notifyDataSetChanged();

        DatabaseReference categoriesRef = database.getReference(Const.DB_REF_CATEGORIES);

        categoriesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Category category = dataSnapshot.getValue(Category.class);
                categoryList.add(category);
                adapter.notifyDataSetChanged();
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
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        recyclerViewCategory = (RecyclerView) findViewById(R.id.recycler_view_category);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                // TODO close application completely
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
                return;
            }
            // Close app after twice click on Back button
            doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.hint_double_press, Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_news) {
            setTitle(R.string.nav_news);

        } else if (id == R.id.nav_business) {
            setTitle(R.string.nav_business);

        } else if (id == R.id.nav_posters) {
            setTitle(R.string.nav_posters);

        } else if (id == R.id.nav_adv) {
            setTitle(R.string.nav_advertisement);

        } else if (id == R.id.nav_wether) {
            setTitle(R.string.nav_wether);

        } else if (id == R.id.nav_auth) {
            if (auth.getCurrentUser() != null) {
                //todo dialog
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.dialog_logout_title)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                auth.signOut();
                                LoginManager.getInstance().logOut();
                                updateUI();
                                updateUIWithConfig();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            } else {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_settings) {
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();

        updateUI();
        updateUIWithConfig();
    }

    private void updateUI() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);

        TextView textUserName = (TextView) hView.findViewById(R.id.textUserName);
        TextView textUserEmail = (TextView) hView.findViewById(R.id.textUserEmail);
        ImageView imageUserPicture = (ImageView) hView.findViewById(R.id.imageUserPicture);

        Menu menuNav = navigationView.getMenu();
        MenuItem navAuth = menuNav.findItem(R.id.nav_auth);

        FirebaseUser user = auth.getCurrentUser();
        // Check if user logged in
        if (user != null) {


            navAuth.setTitle(R.string.nav_authorization_lgout);

            textUserEmail.setText(user.getEmail());
            textUserName.setText(user.getDisplayName());

            Uri userImageUrl = user.getPhotoUrl();
            if (userImageUrl != null)
                Glide.with(this).load(userImageUrl).into(imageUserPicture);
            else
                imageUserPicture.setImageResource(R.mipmap.ic_launcher);

        } else {

            navAuth.setTitle(R.string.nav_authorization);
            textUserName.setText(R.string.app_name);
            textUserEmail.setText(R.string.site_link);
            imageUserPicture.setImageResource(R.mipmap.ic_launcher);
        }

        //
    }
}
