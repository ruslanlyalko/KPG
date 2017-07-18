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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.kpgukraine.CategoryActivity;
import com.example.android.kpgukraine.R;
import com.example.android.kpgukraine.models.Category;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {
    private Context mContext;
    private List<Category> categoryList;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private boolean isAdmin;

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        RelativeLayout panelItem;


        MyViewHolder(View view) {
            super(view);
            textTitle = (TextView) view.findViewById(R.id.text_title);
            panelItem = (RelativeLayout) view.findViewById(R.id.panel_item);
            // TODO InitRef
        }
    }

    public CategoryAdapter(Context mContext, List<Category> reportList, boolean isAdmin) {
        this.mContext = mContext;
        this.categoryList = reportList;
        this.isAdmin = isAdmin;
    }

    @Override
    public CategoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_category, parent, false);

        return new CategoryAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CategoryAdapter.MyViewHolder holder, int position) {
        final Category category = categoryList.get(position);

        final int positionFinal = position;
        holder.textTitle.setText(category.title);

        holder.panelItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isAdmin) return false;
                //return false;

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.dialog_category_title);


                View viewInflated = LayoutInflater.from(mContext).inflate(R.layout.dialog_new_category, null, false);
                builder.setView(viewInflated);
                final EditText inputTitle1 = (EditText) viewInflated.findViewById(R.id.text_title1);
                final EditText inputUri = (EditText) viewInflated.findViewById(R.id.text_uri);

                //init
                inputTitle1.setText(category.title);
                inputUri.setText(category.imageUri);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        category.title = inputTitle1.getText().toString();
                        category.imageUri = inputUri.getText().toString();

                        updateNewCategoryToDB(category, positionFinal);

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
                        removeCategory(category.key, positionFinal);

                    }
                });
                builder.show();
                return true;
            }
        });


        holder.panelItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CategoryActivity.class);
                intent.putExtra(Const.EXTRA_CAT_KEY, category.key);
                intent.putExtra(Const.EXTRA_CAT_TITLE, category.getTitle());
                intent.putExtra(Const.EXTRA_IS_ADMIN, isAdmin);
                mContext.startActivity(intent);
            }
        });

    }

    /**
     * Remove from DB
     *
     * @param key category unique key
     * @param position Where is located
     */
    private void removeCategory(String key, final int position) {

        database.getReference(Const.DB_REF_CATEGORIES).child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                categoryList.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    /**
     * Update value in DB
     *
     * @param category Model that contains all values that we need to upload to db
     * @param position where is in adapter
     */
    private void updateNewCategoryToDB(Category category, final int position) {
        database.getReference(Const.DB_REF_CATEGORIES).child(category.key)
                .setValue(category).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }


}