package com.example.fui.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fui.Adapters.GroceryItemAdapter;
import com.example.fui.Model.GroceryItemModel;
import com.example.fui.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Map;

public class GroceryFragment extends Fragment implements View.OnClickListener {

    private RelativeLayout expandableView;
    private ImageButton arrowBtn;
    private CardView cardView;
    private GroceryItemAdapter adapter;
    private Map<String, Object> mp;
    private Button btnAddItem;
    private RecyclerView recyclerView;

    private ArrayList<GroceryItemModel> modelList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference groceryRef;
    ProgressDialog pd;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_grocery, container, false);
        init(v);
        view = v;
        pd.setMessage("Updating");
        pd.setTitle("Please Wait");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            String UID = user.getUid();
            groceryRef = db.collection("Users").document(UID);
        }





        createExampleList(v);
//        buildRecyclerView(v);
        arrowBtn.setOnClickListener(this);
        cardView.setOnClickListener(this);
        btnAddItem.setOnClickListener(this);

        return v;
    }

    private void buildRecyclerView(View v) {

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroceryItemAdapter(getContext(), modelList);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(adapter);

    }

    private void showToast(String msg)
    {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void createExampleList(final View v) {
        modelList = new ArrayList<>();

        groceryRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        if(document.exists())
                        {
                            Map<String, Object> map = (Map<String, Object>) document.get("Vegetables");

                            if(map.isEmpty())
                            {
                                recyclerView.setVisibility(View.GONE);
                            }
                            else{
                                for (Map.Entry<String, Object> entry : map.entrySet())
                                {
                                    String name = entry.getKey();
                                    long count = (Long)entry.getValue();
                                    modelList.add(new GroceryItemModel(name, count));
                                }
                                buildRecyclerView(v);
                            }
                        }

                    }
                });



    }



    private void init(View v) {
        expandableView = v.findViewById(R.id.expandableView);
        arrowBtn = v.findViewById(R.id.arrowButton);
        cardView = v.findViewById(R.id.cardView);
        btnAddItem = v.findViewById(R.id.btnAddItem);
        recyclerView = v.findViewById(R.id.recyclerView);
        pd = new ProgressDialog(v.getContext());
    }


    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.arrowButton:

            case R.id.cardView:
                expandCardView();
                break;

            case R.id.btnAddItem:
                showDialog();
                break;
        }
    }



    private void expandCardView() {
        if (expandableView.getVisibility()==View.GONE){
            TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
            expandableView.setVisibility(View.VISIBLE);
            arrowBtn.setBackgroundResource(R.drawable.ic_arrow_down);

        } else {
            TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
            expandableView.setVisibility(View.GONE);
            arrowBtn.setBackgroundResource(R.drawable.ic_arrow_down);

        }
        arrowBtn.setRotation(arrowBtn.getRotation()+180F);
    }

    private void showDialog()
    {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.grocery_item_dialog, null);

        Button btnSave, btnCancel;
        final TextInputEditText itemCount, itemName;
        itemCount = view.findViewById(R.id.itemCount);
        itemName = view.findViewById(R.id.itemName);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);





        final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();

        alertDialog.show();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = itemName.getText().toString();
                boolean flag = checkDigit(itemCount.getText().toString());
                if(name.length() == 0 || name.trim().equalsIgnoreCase(""))
                {
                    itemName.setError("Cannot be empty");
                }
                else if(itemCount.getText().length() == 0 || itemCount.getText().toString().trim().equalsIgnoreCase("") || flag)
                {
                    itemCount.setError("Quantity cannot be empty");
                }
                else {

                    int count = Integer.parseInt(itemCount.getText().toString());
                    int position = modelList.size();
                    if (position == 0) {
                        modelList.add(new GroceryItemModel(name, count));
                    } else {
                        modelList.add(position, new GroceryItemModel(name, count));
                    }
                    showProgress();
                    addToFirestore(name, count);
//                    adapter.notifyItemInserted(position);
//                    adapter.notifyDataSetChanged();
                    alertDialog.dismiss();
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });
    }

    private boolean checkDigit(String toString) {
        boolean flag = false;
        char[] chars = toString.toCharArray();
        for(char c : chars)
        {
            if(!Character.isDigit(c))
            {
                flag = true;
            }
        }
        return flag;

    }

    private void addToFirestore(final String name, final int count) {


        groceryRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot ds = task.getResult();
                            if(ds.exists())
                            {
                                mp = (Map<String, Object>) ds.get("Vegetables");
                                mp.put(name, count);
                                groceryRef.update("Vegetables", mp)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getContext(), "Added item Successfully", Toast.LENGTH_LONG).show();
                                                closeProgress();
                                                createExampleList(view);
                                            }
                                        });
                            }
                        }
                    }
                });

    }

    private void showProgress()
    {
        pd.show();
    }
    private void closeProgress()
    {
        pd.dismiss();
    }
}
