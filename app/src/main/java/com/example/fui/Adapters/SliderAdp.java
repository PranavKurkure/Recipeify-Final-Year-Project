package com.example.fui.Adapters;


import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.fui.R;
import com.smarteist.autoimageslider.SliderViewAdapter;

public class SliderAdp extends SliderViewAdapter<SliderAdp.Holder> {

    private int[] images;

    public SliderAdp(int[] images) {
        this.images = images;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slider, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder viewHolder, int position) {
        viewHolder.image_view.setImageResource(images[position]);
    }

    @Override
    public int getCount() {
        return images.length;
    }

    public class Holder extends SliderViewAdapter.ViewHolder {

        ImageView image_view;

        public Holder(View itemView) {
            super(itemView);

            image_view = itemView.findViewById(R.id.image_view);

        }
    }
}
