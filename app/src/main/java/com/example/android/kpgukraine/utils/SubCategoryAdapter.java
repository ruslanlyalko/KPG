package com.example.android.kpgukraine.utils;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.example.android.kpgukraine.SubCategoryActivity;
import com.example.android.kpgukraine.models.Category;
import com.example.android.kpgukraine.models.SubCategory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.MyViewHolder> {
    private Context mContext;
    private List<SubCategory> subCategoryList;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private List<Category> categoryList = new ArrayList<>();
    private boolean isAdmin;


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        RelativeLayout panelItem;


        MyViewHolder(View view) {
            super(view);
            textTitle = (TextView) view.findViewById(R.id.text_title);
            panelItem = (RelativeLayout) view.findViewById(R.id.panel_item);
        }
    }

    public SubCategoryAdapter(Context mContext, List<SubCategory> subCategoryList, boolean isAdmin) {
        this.mContext = mContext;
        this.subCategoryList = subCategoryList;
        this.isAdmin = isAdmin;

        loadCategoriesFromDB();
    }

    @Override
    public SubCategoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_category, parent, false);

        return new SubCategoryAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SubCategoryAdapter.MyViewHolder holder, int position) {
        final SubCategory subCategory = subCategoryList.get(position);
        final int positionFinal = position;
        holder.textTitle.setText(subCategory.title);

        holder.panelItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isAdmin) return false;
                //return false;

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.dialog_subcategory_title);


                View viewInflated = LayoutInflater.from(mContext).inflate(R.layout.dialog_new_subcategory, null, false);
                builder.setView(viewInflated);
                final EditText inputTitle1 = (EditText) viewInflated.findViewById(R.id.text_title1);
                final EditText inputUri = (EditText) viewInflated.findViewById(R.id.text_uri);
                final Spinner spinnerCategory = (Spinner) viewInflated.findViewById(R.id.spinner_category);

                String[] arraySpinner = new String[categoryList.size()];

                int pos = 0;
                for (int i = 0; i < categoryList.size(); i++) {
                    arraySpinner[i] = categoryList.get(i).title;

                    if (categoryList.get(i).key.equals(subCategory.categoryKey))
                        pos = i;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                        android.R.layout.simple_spinner_item, arraySpinner);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinnerCategory.setAdapter(adapter);
                spinnerCategory.setSelection(pos);


                //init
                inputTitle1.setText(subCategory.title);
                inputUri.setText(subCategory.imageUri);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        subCategory.title = inputTitle1.getText().toString();
                        subCategory.imageUri = inputUri.getText().toString();
                        String oldCategoryKey = subCategory.categoryKey;
                        String categoryTitle = spinnerCategory.getSelectedItem().toString();

                        for (int i = 0; i < categoryList.size(); i++) {

                            if (categoryList.get(i).title.equals(categoryTitle))
                                subCategory.categoryKey = categoryList.get(i).key;
                        }

                        boolean moved = !oldCategoryKey.equals(subCategory.categoryKey);

                        updateNewCategoryToDB(subCategory, positionFinal, moved);
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
                        removeSubCategory(subCategory, positionFinal);

                    }
                });
                builder.show();
                return true;
            }
        });


        holder.panelItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SubCategoryActivity.class);
                intent.putExtra(Const.EXTRA_SUB_CAT_KEY, subCategory.getKey());
                intent.putExtra(Const.EXTRA_SUB_CAT_TITLE, subCategory.getTitle());
                intent.putExtra(Const.EXTRA_IS_ADMIN, isAdmin);

                mContext.startActivity(intent);
            }
        });

    }

    /**
     * Load all Categories from Firebase DB
     * Listening for any new added categories
     */
    private void loadCategoriesFromDB() {

        categoryList.clear();

        DatabaseReference categoriesRef = database.getReference(Const.DB_REF_CATEGORIES);

        categoriesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Category category = dataSnapshot.getValue(Category.class);
                categoryList.add(category);
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
     * @param position in adapter
     */
    private void removeSubCategory(SubCategory subCategory, final int position) {

        database.getReference(Const.DB_REF_CATEGORIES)
                .child(subCategory.categoryKey)
                .child(subCategory.key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                subCategoryList.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    /**
     * Update value in DB
     *
     * @param subCategory model that we want to store
     * @param moved move
     */
    private void updateNewCategoryToDB(final SubCategory subCategory, final int position, final boolean moved) {
        if (moved) {

            subCategoryList.remove(position);
            notifyItemRemoved(position);
        }

        database.getReference(Const.DB_REF_SUBCATEGORIES).child(subCategory.key)
                .setValue(subCategory).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!moved)
                    notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subCategoryList.size();
    }


}