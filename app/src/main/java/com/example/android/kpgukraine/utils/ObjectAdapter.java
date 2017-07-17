package com.example.android.kpgukraine.utils;


import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.kpgukraine.R;
import com.example.android.kpgukraine.models.ObjectModel;
import com.example.android.kpgukraine.models.SubCategory;
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

public class ObjectAdapter extends RecyclerView.Adapter<ObjectAdapter.MyViewHolder> {
    private Context mContext;
    private List<ObjectModel> objectModelList;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private List<SubCategory> subCategoryList = new ArrayList<>();
    private boolean isAdmin;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textTitle;
        public RelativeLayout panelItem;


        public MyViewHolder(View view) {
            super(view);
            textTitle = (TextView) view.findViewById(R.id.text_title);
            panelItem = (RelativeLayout) view.findViewById(R.id.panel_item);
            // TODO InitRef
        }


    }

    public ObjectAdapter(Context mContext, List<ObjectModel> objectModelList, boolean isAdmin) {
        this.mContext = mContext;
        this.objectModelList = objectModelList;
        this.isAdmin = isAdmin;

        loadSubCategoriesFromDB();
    }

    @Override
    public ObjectAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.object_card, parent, false);

        return new ObjectAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ObjectAdapter.MyViewHolder holder, final int position) {
        final ObjectModel objectModel = objectModelList.get(position);

        holder.textTitle.setText(objectModel.title);

        holder.panelItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isAdmin) return false;
                //return false;

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.dialog_object_title);


                View viewInflated = LayoutInflater.from(mContext).inflate(R.layout.dialog_new_object, null, false);
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

                String[] arraySpinner = new String[subCategoryList.size()];

                int pos = 0;
                for (int i = 0; i < subCategoryList.size(); i++) {
                    arraySpinner[i] = subCategoryList.get(i).getTitle();

                    if (subCategoryList.get(i).getKey().equals(objectModel.subCategoryKey))
                        pos = i;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                        android.R.layout.simple_spinner_item, arraySpinner);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinnerCategory.setAdapter(adapter);
                spinnerCategory.setSelection(pos);
                //init
                editTitle.setText(objectModel.getTitle());
                editDescription.setText(objectModel.getDescription());
                editAddress.setText(objectModel.getAddress());
                editDinner.setText(objectModel.getDinner());
                editPhone.setText(objectModel.getPhone());
                editTime.setText(objectModel.getTimeOpened());
                editUri.setText(objectModel.getImageUri());
                editLatitude.setText(objectModel.getLatitude());
                editLongitude.setText(objectModel.getLongitude());

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

                        objectModel.setTitle(title1);
                        objectModel.setDescription(description);
                        objectModel.setAddress(address);
                        objectModel.setDinner(dinner);
                        objectModel.setPhone(phone);
                        objectModel.setTimeOpened(time);
                        objectModel.setImageUri(image);
                        objectModel.setLatitude(latitude);
                        objectModel.setLongitude(longitude);

                        String oldCategoryKey = objectModel.subCategoryKey;
                        String subCategoryTitle = spinnerCategory.getSelectedItem().toString();

                        // check if
                        for (int i = 0; i < subCategoryList.size(); i++) {

                            if (subCategoryList.get(i).getTitle().equals(subCategoryTitle))
                                objectModel.subCategoryKey = subCategoryList.get(i).getKey();
                        }

                        boolean moved = !oldCategoryKey.equals(objectModel.subCategoryKey);

                        updateNewCategoryToDB(objectModel, position, moved);

                    }
                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeObject(objectModel, position);

                    }
                });
                builder.show();
                return true;
            }
        });


        holder.panelItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Load Object
            }
        });

    }

    /**
     * Load all Categories from Firebase DB
     * Listening for any new added categories
     */
    private void loadSubCategoriesFromDB() {

        subCategoryList.clear();

        DatabaseReference categoriesRef = database.getReference(Const.DB_REF_SUBCATEGORIES);

        categoriesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                SubCategory subCategory = dataSnapshot.getValue(SubCategory.class);

                subCategoryList.add(subCategory);
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
     * Remove from DB
     *
     * @param position
     */
    private void removeObject(ObjectModel objectModel, final int position) {

        database.getReference(Const.DB_REF_OBJECTS)
                .child(objectModel.key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                objectModelList.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    /**
     * Update value in DB
     *
     * @param objectModel
     * @param position
     * @param moved
     */
    private void updateNewCategoryToDB(final ObjectModel objectModel, final int position, final boolean moved) {
         if(moved){
            // todo update any other references to this object
            objectModelList.remove(position);
            notifyItemRemoved(position);
        }

        database.getReference(Const.DB_REF_OBJECTS)
                .child(objectModel.key)
                .setValue(objectModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!moved)
                    notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return objectModelList.size();
    }


}