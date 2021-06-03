package com.example.fui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class RatingDialog extends AppCompatDialogFragment {

    private String dish_name;
    private RatingBar ratingBar;
    private TextView rating;
    private TextView status, dishname;

    public RatingDialog(String dish_name) {
        this.dish_name = dish_name;
    }

    private RatingDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.rating_dialog, null);

        builder.setView(view)
                .setTitle("Rate the recipe")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        float temp = ratingBar.getRating();
                        if(temp == 0.0f || temp == 0f)
                        {
                            rating.setVisibility(View.GONE);
                            status.setText("Rating cannot be 0");
                            status.setTextColor(getResources().getColor(R.color.red));
                        }
                        else {
                            rating.setVisibility(View.VISIBLE);
                            listener.applyRating(temp);
                        }

                    }
                });
        ratingBar = view.findViewById(R.id.ratingBar);
        rating = view.findViewById(R.id.rating);
        rating.setVisibility(View.VISIBLE);
        status = view.findViewById(R.id.status);
        dishname = view.findViewById(R.id.dish_name);
        dishname.setText(dish_name);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                String temp="";
                if(v <= 1)
                {
                    status.setTextColor(getResources().getColor(R.color.not_liked));
                    temp = "Not Liked";
                }else if(v <= 2)
                {
                    status.setTextColor(getResources().getColor(R.color.can_be_improved));
                    temp = "Can be improved";

                }else if(v <= 3)
                {
                    status.setTextColor(getResources().getColor(R.color.average));
                    temp = "Average";
                }
                else if(v <= 4)
                {
                    status.setTextColor(getResources().getColor(R.color.great));
                    temp = "Great!";
                }else{
                    status.setTextColor(getResources().getColor(R.color.excellent));
                    temp = "Excellent";
                }
                rating.setText(String.valueOf(v));
                status.setText(temp);
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (RatingDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+ "must implement ExampleDialogListener");
        }
    }

    public interface RatingDialogListener
    {
        void applyRating(float rating);
    }
}
