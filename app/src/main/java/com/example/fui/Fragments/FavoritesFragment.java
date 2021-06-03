package com.example.fui.Fragments;

import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fui.Adapters.RecipeAdapter;
import com.example.fui.Model.Recipe;
import com.example.fui.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class FavoritesFragment extends Fragment {

    private FirebaseFirestore db;
    private DocumentReference docref;
    private RecyclerView recycler_view;
    private List<String> userRecipes;

    private TextView tv;
    private AutoCompleteTextView autoCompleteText;
    private ProgressDialog pd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        init(view);
        autoCompleteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString().trim();
                setupData(text);
            }
        });
//        setupData(user);
        return view;
    }

    private void init(View view)
    {
        tv = view.findViewById(R.id.tv);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        autoCompleteText = view.findViewById(R.id.autoCompleteText);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.option_item, getResources().getStringArray(R.array.category));
        autoCompleteText.setAdapter(arrayAdapter);
        recycler_view = view.findViewById(R.id.recycler_view);
        db = FirebaseFirestore.getInstance();
        userRecipes = new ArrayList<>();
        if(user != null)
        {
            docref = db.collection("Users").document(user.getUid());
        }
        pd = new ProgressDialog(getContext());
        pd.setTitle("Please wait!");
        pd.setMessage("Loading");

//        favtv = view.findViewById(R.id.favtv);
    }

    private void setupData(String text)
    {
        pd.show();
        userRecipes.clear();
        docref.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists())
                            {
                                Map<String, Object> map = (Map<String, Object>) documentSnapshot.get("Favorites");
                                for(Map.Entry<String, Object> entry : map.entrySet())
                                {
                                    if(entry.getKey().equalsIgnoreCase(text))
                                    {
                                        userRecipes = (List<String>) entry.getValue();
                                        setupRecyclerView(text, userRecipes);
                                    }
                                }

                            }
                    }
                });
    }

    private void setupRecyclerView(String recipe_category, List<String> userRecipes)
    {
        CollectionReference recipeRef = db.collection(recipe_category);
        Query query;
        if(userRecipes.size() == 0)
        {
            tv.setText("Empty list");
            tv.setVisibility(View.VISIBLE);
            recycler_view.setVisibility(View.GONE);
        }
        else{
            tv.setVisibility(View.GONE);
            recycler_view.setVisibility(View.VISIBLE);
            query = recipeRef.orderBy("dish_name")
                            .whereIn("dish_name", userRecipes);
            FirestoreRecyclerOptions<Recipe> options = new FirestoreRecyclerOptions.Builder<Recipe>()
                    .setQuery(query, Recipe.class)
                    .build();

            RecipeAdapter adapter = new RecipeAdapter(options, recipe_category);
//            RecyclerView recycler_view = getView().findViewById(R.id.recycler_view);
            GridLayoutManager glm = new GridLayoutManager(getActivity().getApplicationContext(),2);
            recycler_view.setHasFixedSize(true);
            recycler_view.setLayoutManager(glm);
            recycler_view.setAdapter(adapter);
            adapter.startListening();
        }
        pd.dismiss();
    }

    private void showToast(String message)
    {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }



}
