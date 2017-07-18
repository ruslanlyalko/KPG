package com.example.android.kpgukraine.utils;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.kpgukraine.ObjectActivity;
import com.example.android.kpgukraine.R;
import com.example.android.kpgukraine.models.ObjectModel;
import com.example.android.kpgukraine.models.SubCategory;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ObjectAdapter extends RecyclerView.Adapter<ObjectAdapter.MyViewHolder> implements Filterable {
    private Context mContext;
    private List<ObjectModel> objectModelList;
    private List<ObjectModel> objectModelListAll;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private List<SubCategory> subCategoryList = new ArrayList<>();
    private boolean isAdmin;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private ObjectFilter objectFilter;


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textAddress, textPhone;
        RelativeLayout panelItem;
        ImageView imageView;
        Button buttonCall;


        MyViewHolder(View view) {
            super(view);
            panelItem = (RelativeLayout) view.findViewById(R.id.panel_item);
            textTitle = (TextView) view.findViewById(R.id.text_title);
            textAddress = (TextView) view.findViewById(R.id.text_address);
            textPhone = (TextView) view.findViewById(R.id.text_phone);
            imageView = (ImageView) view.findViewById(R.id.image);
            buttonCall = (Button) view.findViewById(R.id.button_call);

        }


    }

    public ObjectAdapter(Context mContext, List<ObjectModel> objectModelList, boolean isAdmin) {
        this.mContext = mContext;
        this.objectModelList = objectModelList;
        this.objectModelListAll = objectModelList;
        this.isAdmin = isAdmin;

        loadSubCategoriesFromDB();
    }

    @Override
    public ObjectAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_object, parent, false);

        return new ObjectAdapter.MyViewHolder(itemView);
    }



    private void setImage(String imageUri, final ImageView targetImageView) {

        if (imageUri != null && !imageUri.isEmpty()) {
            if (imageUri.contains("http")) {
                // set imageView from URL provided by user
                Glide.with(mContext).load(imageUri).into(targetImageView);

            } else {
                final StorageReference ref = storage.getReference(Const.STORE_OBJECT_IMAGES).child(imageUri);

                // check if imageView is exist on Storage, otherwise load default imageView
                ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Glide.with(mContext).using(new FirebaseImageLoader()).load(ref).into(targetImageView);
                        } else {
                            StorageReference ref = storage.getReference(Const.STORE_OBJECT_IMAGES).child(Const.DEF_IMAGE);
                            Glide.with(mContext).using(new FirebaseImageLoader()).load(ref).into(targetImageView);
                        }
                    }
                });
            }

        } else {
            //set default imageView if user dn't enter any link
            StorageReference ref = storage.getReference(Const.STORE_OBJECT_IMAGES).child(Const.DEF_IMAGE);
            Glide.with(mContext).using(new FirebaseImageLoader()).load(ref).into(targetImageView);
        }
    }


    @Override
    public void onBindViewHolder(final ObjectAdapter.MyViewHolder holder, int position) {
        final ObjectModel objectModel = objectModelList.get(position);

        final int positionFinal = position;

        setImage(objectModel.getImageUri(), holder.imageView);
       //todo check this logic


        holder.textTitle.setText(objectModel.title);
        holder.textAddress.setText(objectModel.address);
        holder.textPhone.setText(objectModel.phone);

        holder.buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + getFirstNumber(objectModel.getPhone())));
                mContext.startActivity(callIntent);
            }
        });

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
                ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
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

                        updateNewCategoryToDB(objectModel, positionFinal, moved);

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
                        removeObject(objectModel, positionFinal);

                    }
                });
                builder.show();
                return true;
            }
        });


        holder.panelItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ObjectActivity.class);
                intent.putExtra(Const.EXTRA_SUB_CAT_KEY, objectModel.getKey());
                intent.putExtra(Const.EXTRA_SUB_CAT_TITLE, objectModel.getTitle());
                intent.putExtra(Const.EXTRA_IS_ADMIN, isAdmin);

                mContext.startActivity(intent);
            }
        });

    }

    /**
     * Function to get first phone number in string variable
     *
     * @param phone phone number
     * @return phone number
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
     * @param position of element
     */
    private void removeObject(ObjectModel objectModel, int position) {

        final int pos = position;
        database.getReference(Const.DB_REF_OBJECTS)
                .child(objectModel.key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                objectModelList.remove(pos);
                notifyItemRemoved(pos);
            }
        });
    }

    /**
     * Update value in DB
     *
     * @param objectModel object that we need to upload to db
     * @param position position in adapter
     * @param moved if we have changed location
     */
    private void updateNewCategoryToDB(final ObjectModel objectModel, int position, final boolean moved) {
        final int pos = position;
        if (moved) {
            objectModelList.remove(position);
            notifyItemRemoved(position);
        }

        database.getReference(Const.DB_REF_OBJECTS)
                .child(objectModel.key)
                .setValue(objectModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!moved)
                    notifyItemChanged(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return objectModelList.size();
    }


    @Override
    public Filter getFilter() {

        if (objectFilter == null)
            objectFilter = new ObjectFilter();
        return objectFilter;
    }

    private class ObjectFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            ArrayList<ObjectModel> tempList = new ArrayList<>();

            if (constraint != null && constraint.length() > 0) {

                // search content in friend list
                for (ObjectModel objectModel : objectModelListAll) {
                    if (objectModel.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(objectModel);
                    }
                }

            }
            filterResults.count = tempList.size();
            filterResults.values = tempList;


            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            objectModelList = (ArrayList<ObjectModel>) results.values;
            notifyDataSetChanged();
        }

    }
}