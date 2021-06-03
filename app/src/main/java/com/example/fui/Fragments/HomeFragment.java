package com.example.fui.Fragments;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.ColorSpace;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.fui.Adapters.ItemAdapter;
import com.example.fui.Adapters.SliderAdp;
import com.example.fui.R;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() { }

    private int[] images = {R.drawable.slider1,R.drawable.slider2,R.drawable.slider3,R.drawable.slider4,R.drawable.slider5};
    private SliderAdp adp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        SliderView sliderView = view.findViewById(R.id.slider_view);
        adp = new SliderAdp(images);
        sliderView.setSliderAdapter(adp);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);

//        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
        sliderView.startAutoCycle();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
//        initData();

        recyclerView.setAdapter(new ItemAdapter(initData()));

        return view;
    }

    private List<Model> initData() {
        List<Model> itemList = new ArrayList<>();
        itemList.add(new Model(R.drawable.image1, "Appetizers",5));
        itemList.add(new Model(R.drawable.image3, "Salads",5));
        itemList.add(new Model(R.drawable.image2, "Drinks",5));
        itemList.add(new Model(R.drawable.images, "Dessert",5));
        itemList.add(new Model(R.drawable.image1, "Main Dish",5));
        return itemList;

    }
}
