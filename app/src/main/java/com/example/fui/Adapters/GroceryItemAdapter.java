package com.example.fui.Adapters;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fui.Model.GroceryItemModel;
import com.example.fui.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroceryItemAdapter extends RecyclerView.Adapter<GroceryItemAdapter.ViewHolder> {



    private Map<String, Integer> mp;
    private LayoutInflater mInflater;

    private ArrayList<GroceryItemModel> model;
    GroceryItemModel temp;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docref = db.collection("Users").document(user.getUid());



    public GroceryItemAdapter(Context context, ArrayList<GroceryItemModel> model) {
        this.mInflater = LayoutInflater.from(context);
        this.model = model;

    }

    @NonNull
    @Override
    public GroceryItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.grocery_item, parent, false);
        return new GroceryItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroceryItemAdapter.ViewHolder holder, final int position) {
            final GroceryItemModel exampleItem = model.get(position);

            if(exampleItem.getItemCount() <= 5)
            {
                holder.warning_tv.setVisibility(View.VISIBLE);
            }else{
                holder.warning_tv.setVisibility(View.GONE);
            }
            holder.item_count.setText(String.valueOf(exampleItem.getItemCount()));
            holder.item_name.setText(exampleItem.getItemName());
            holder.btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long x = exampleItem.getItemCount() +1;
                    if(x >= 0)
                    {
                        exampleItem.setItemCount(x);
                        notifyItemChanged(position);
                        updateInFirebase(exampleItem.getItemName(), x);
                    }


                }
            });
            holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    long x = exampleItem.getItemCount()-1;
                    if(x>0)
                    {
                        exampleItem.setItemCount(x);
                        notifyItemChanged(position);
                        updateInFirebase(exampleItem.getItemName(), x);
                    }
                    else{
                        removeItemFromPosition(position, view);
                        removeFromFirebase(exampleItem.getItemName());
                    }
                }
            });
    }

    private void removeItemFromPosition(final int position, View view) {
        temp = model.get(position);
        model.remove(position);
        notifyItemRemoved(position);
        Snackbar.make(view, temp.getItemName(), Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        model.add(position, temp);
                        notifyItemInserted(position);
                        addToFirestore(temp.getItemName(), 1);
                    }
                })
                .show();
    }

    private void addToFirestore(final String name, final int count) {

        docref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot ds = task.getResult();
                            if(ds.exists())
                            {
                                Map<String, Object> mp = (Map<String, Object>) ds.get("Vegetables");
                                mp.put(name, count);
                                docref.update("Vegetables", mp)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });
                            }
                        }
                    }
                });

    }

    private void updateInFirebase(final String item_name, final long value)
    {
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot ds = transaction.get(docref);
                Map<String, Object> map = (Map<String, Object>) ds.get("Vegetables");
                for(Map.Entry<String, Object> e : map.entrySet())
                {
                    if(e.getKey().equalsIgnoreCase(item_name))
                    {
                        map.put(e.getKey(), value);
                    }
                }
                transaction.update(docref, "Vegetables", map);
                return null;
            }
        });
    }

    private void removeFromFirebase(final String item_name)
    {
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot ds = transaction.get(docref);
                Map<String, Object> map = (Map<String, Object>) ds.get("Vegetables");
                Map<String, Object> temp = new HashMap<>();
                for(Map.Entry<String, Object> entry : map.entrySet())
                {
                    if(!entry.getKey().equalsIgnoreCase(item_name))
                    {
                        temp.put(entry.getKey(), entry.getValue());
                    }
                }
                transaction.update(docref, "Vegetables", temp);
                return null;
            }
        });
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView item_name, item_count, warning_tv;
        ImageButton btnAdd, btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.item_name);
            item_count = itemView.findViewById(R.id.item_count);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            warning_tv = itemView.findViewById(R.id.warning_tv);

        }



    }
}
