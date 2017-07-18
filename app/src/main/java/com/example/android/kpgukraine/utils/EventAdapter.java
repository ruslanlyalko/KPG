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
import com.example.android.kpgukraine.models.EventModel;
import com.example.android.kpgukraine.models.ObjectModel;
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

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.MyViewHolder> {
    private Context mContext;
    private List<EventModel> eventModelList;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private List<ObjectModel> objectModelList = new ArrayList<>();
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

    public EventAdapter(Context mContext, List<EventModel> eventModelList, boolean isAdmin) {
        this.mContext = mContext;
        this.eventModelList = eventModelList;
        this.isAdmin = isAdmin;

        loadObjectsFromDB();
    }

    @Override
    public EventAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent, false);

        return new EventAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final EventAdapter.MyViewHolder holder, final int position) {
        final EventModel eventModel = eventModelList.get(position);

        holder.textTitle.setText(eventModel.title);

        holder.panelItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isAdmin) return false;
                //return false;

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.dialog_object_title);


                View viewInflated = LayoutInflater.from(mContext).inflate(R.layout.dialog_new_event, null, false);
                builder.setView(viewInflated);

                final EditText editTitle = (EditText) viewInflated.findViewById(R.id.edit_title);
                final EditText editDescription = (EditText) viewInflated.findViewById(R.id.edit_description);

                final Spinner spinnerCategory = (Spinner) viewInflated.findViewById(R.id.spinner_category);

                String[] arraySpinner = new String[objectModelList.size()];

                int pos = 0;
                for (int i = 0; i < objectModelList.size(); i++) {
                    arraySpinner[i] = objectModelList.get(i).getTitle();

                    if (objectModelList.get(i).getKey().equals(eventModel.getObjectKey()))
                        pos = i;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                        android.R.layout.simple_spinner_item, arraySpinner);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinnerCategory.setAdapter(adapter);
                spinnerCategory.setSelection(pos);
                //init
                editTitle.setText(eventModel.getTitle());
                editDescription.setText(eventModel.getDescription());


                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String title1 = editTitle.getText().toString();
                        String description = editDescription.getText().toString();


                        eventModel.setTitle(title1);
                        eventModel.setDescription(description);

                        String oldParentKey = eventModel.getObjectKey();
                        String subCategoryTitle = spinnerCategory.getSelectedItem().toString();

                        // check if
                        for (int i = 0; i < objectModelList.size(); i++) {

                            if (objectModelList.get(i).getTitle().equals(subCategoryTitle))
                                eventModel.objectKey = objectModelList.get(i).getKey();
                        }

                        boolean moved = !oldParentKey.equals(eventModel.objectKey);

                        updateEventToDB(eventModel, position, moved);

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
                        removeEvent(eventModel, position);

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


    private void loadObjectsFromDB() {

        objectModelList.clear();

        DatabaseReference categoriesRef = database.getReference(Const.DB_REF_OBJECTS);

        categoriesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                ObjectModel objectModel = dataSnapshot.getValue(ObjectModel.class);

                objectModelList.add(objectModel);
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
    private void removeEvent(EventModel eventModel, final int position) {

        database.getReference(Const.DB_REF_EVENTS)
                .child(eventModel.key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                eventModelList.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    /**
     * Update value in DB
     *
     * @param eventModel
     * @param position
     * @param moved
     */
    private void updateEventToDB(final EventModel eventModel, final int position, final boolean moved) {
         if(moved){
            // todo update any other references to this object
            eventModelList.remove(position);
            notifyItemRemoved(position);
        }

        database.getReference(Const.DB_REF_EVENTS)
                .child(eventModel.key)
                .setValue(eventModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!moved)
                    notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventModelList.size();
    }


}