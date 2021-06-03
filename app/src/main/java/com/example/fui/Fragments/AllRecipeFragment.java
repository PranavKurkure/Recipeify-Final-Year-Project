package com.example.fui.Fragments;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fui.Adapters.RecipeAdapter;
import com.example.fui.Model.Recipe;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.example.fui.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AllRecipeFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference recipeRef;
    private RecipeAdapter adapter;
    private RecyclerView recyclerView;

    private Chip cook_time, rating, name;
    private ChipGroup CG;
    private TextInputEditText recipe_name;
    private TextInputLayout tl1;


    private TextView recipeCategory;

    public AllRecipeFragment() {
    }

    AppCompatActivity activity = (AppCompatActivity)getContext();
    HomeFragment hf = new HomeFragment();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_recipe, container, false);
        init(view);

        Bundle bundle = this.getArguments();
        recipeCategory.setText(bundle.getString("recipe_category"));


        followNormalFlow(recipeCategory.getText().toString());


//        GestureDetector is used to detect Right and Left Swipe
        final GestureDetector gd = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {


                final int SWIPE_MIN_DISTANCE = 120;
                final int SWIPE_MAX_OFF_PATH = 250;
                final int SWIPE_THRESHOLD_VELOCITY = 200;
                try {
                    if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                        return false;
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                            && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                            && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_slide_left_enter,
                                R.anim.fragment_slide_right_exit).replace(R.id.fragment_container, hf).addToBackStack(null).commit();
                    }
                } catch (Exception e) {
                    // nothing
                    Toast.makeText(getContext(), "In catch block\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        view.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gd.onTouchEvent(motionEvent);
            }
        });

        return view;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void init(View view)
    {
        recipeCategory = view.findViewById(R.id.recipeCategory);
        recyclerView = view.findViewById(R.id.recycler_view);
        CG = view.findViewById(R.id.CG);
        cook_time  = view.findViewById(R.id.cook_time);
        rating = view.findViewById(R.id.rating);
        name = view.findViewById(R.id.name);
        recipe_name = view.findViewById(R.id.recipe_name);
        tl1 = view.findViewById(R.id.tl1);
    }

    private void followNormalFlow(String recipe_category)
    {
        recipeRef = db.collection(recipe_category);
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        Query query = recipeRef.orderBy("dish_name", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Recipe> options = new FirestoreRecyclerOptions.Builder<Recipe>()
                .setQuery(query, Recipe.class)
                .build();

        adapter = new RecipeAdapter(options, recipeCategory.getText().toString());

        GridLayoutManager glm =new GridLayoutManager(getContext(), 2);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(glm);
        recyclerView.setAdapter(adapter);

        recipe_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable.toString().toLowerCase().trim();
                Query query;
                if(str.length()==0 || str.isEmpty())
                {
                    query = recipeRef
                            .orderBy("dish_name", Query.Direction.ASCENDING);
                }else{
                    query = recipeRef
                            .whereEqualTo("dish_name_small", str)
                            .orderBy("dish_name_small", Query.Direction.ASCENDING);
                }
                FirestoreRecyclerOptions<Recipe> options = new FirestoreRecyclerOptions.Builder<Recipe>()
                        .setQuery(query, Recipe.class)
                        .build();
                adapter.updateOptions(options);

            }
        });




        CG.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                Query query = recipeRef
                        .orderBy("dish_name", Query.Direction.ASCENDING);
                switch(checkedId)
                {
                    case R.id.cook_time:
                        query = recipeRef
                                .orderBy("cook_time", Query.Direction.ASCENDING);
                        break;
                    case R.id.rating:
                        query = recipeRef
                                .orderBy("rating", Query.Direction.DESCENDING);
                        break;
                    case R.id.name:
                        query = recipeRef
                                .orderBy("dish_name", Query.Direction.ASCENDING);
                        break;
                }
                FirestoreRecyclerOptions<Recipe> options = new FirestoreRecyclerOptions.Builder<Recipe>()
                        .setQuery(query, Recipe.class)
                        .build();
                adapter.updateOptions(options);
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


}
