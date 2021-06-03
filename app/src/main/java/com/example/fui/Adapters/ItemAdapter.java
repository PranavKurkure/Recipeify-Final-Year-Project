package com.example.fui.Adapters;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fui.Fragments.AllRecipeFragment;
import com.example.fui.Fragments.Model;
import com.example.fui.R;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    List<Model> itemList;
    public ItemAdapter(List<Model> itemList){
        this.itemList = itemList;
    }




    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, final int position) {
        holder.recipeCategory.setText(itemList.get(position).getRecipeCategory());
        holder.relativeLayout.setBackgroundResource(itemList.get(position).getImage());
        holder.recipeNumber.setText(itemList.get(position).getRecipeCount()+" recipes");
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(view.getContext(), itemList.get(position).getRecipeCategory()+" clicked", Toast.LENGTH_SHORT).show();
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                AllRecipeFragment arf = new AllRecipeFragment();
                Bundle bundle = new Bundle();
                bundle.putString("recipe_category", itemList.get(position).getRecipeCategory());
                arf.setArguments(bundle);

                activity.getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_slide_left_enter,
                        R.anim.fragment_slide_left_exit,
                        R.anim.fragment_slide_right_enter,
                        R.anim.fragment_slide_right_exit).replace(R.id.fragment_container, arf).addToBackStack(null).commit();


            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        TextView recipeCategory, recipeNumber;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);
            recipeCategory = itemView.findViewById(R.id.recipeCategory);
            recipeNumber = itemView.findViewById(R.id.recipeNumber);
        }
    }
}
