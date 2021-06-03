package com.example.fui.Adapters;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.fui.Fragments.AllRecipeFragment;
import com.example.fui.Model.Recipe;
import com.example.fui.R;
import com.example.fui.RecipeDisplay;
import com.example.fui.StepsActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class RecipeAdapter extends FirestoreRecyclerAdapter<Recipe, RecipeAdapter.RecipeHolder> {

    private String recipe_category;

    private String DISH_NAME="dish_name", DESCRIPTION="description", RECIPE_TEXT="recipe_text";
    private String INGREDIENTS="ingredients", IMAGE_URL="image_url", FOOD_TYPE="food_type", COOK_TIME="cook_time",AUTHOR="author";
    private String CATEGORY="recipe_category";
    private String SERVINGS="servings";
    private String RATING="rating";




    public RecipeAdapter(@NonNull FirestoreRecyclerOptions<Recipe> options, String recipe_category) {
        super(options);
        this.recipe_category = recipe_category;

    }

    @Override
    protected void onBindViewHolder(@NonNull final RecipeHolder holder, int position, @NonNull final Recipe model) {
        holder.dish_name.setText(model.getDish_name());
        holder.cook_time.setText(model.getCook_time() +"mins");
        String image_url = model.getImage_url();
        Glide.with(holder.itemView.getContext()).load(image_url).into(holder.dish_image);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), RecipeDisplay.class);
            intent.putExtra(DISH_NAME, model.getDish_name());
            intent.putStringArrayListExtra(RECIPE_TEXT, (ArrayList<String>) model.getRecipe_text());
            intent.putExtra(IMAGE_URL, model.getImage_url());
            intent.putExtra("createdOn", model.getCreatedOn());
            intent.putExtra(COOK_TIME, model.getCook_time());
            intent.putExtra(CATEGORY, recipe_category);
            intent.putExtra(FOOD_TYPE, model.isFood_type());
            intent.putExtra(SERVINGS, model.getServings());
            intent.putExtra(DESCRIPTION, model.getDescription());
            intent.putStringArrayListExtra(INGREDIENTS, (ArrayList<String>) model.getIngredients());
            intent.putExtra(RATING, model.getRating());
            intent.putExtra(AUTHOR,model.getAuthor());
            intent.putExtra(SERVINGS, model.getServings());
            view.getContext().startActivity(intent);

        });


    }


    @NonNull
    @Override
    public RecipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item,
                parent, false);
        return new RecipeHolder(v);
    }




    class RecipeHolder extends RecyclerView.ViewHolder{
        TextView cook_time, dish_name;
        ImageView dish_image;

        RecipeHolder(@NonNull final View itemView) {
            super(itemView);
            init();

        }


        private void init() {
            dish_image = itemView.findViewById(R.id.dish_image);
            cook_time = itemView.findViewById(R.id.cook_time);
            dish_name = itemView.findViewById(R.id.dish_name);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }
}
