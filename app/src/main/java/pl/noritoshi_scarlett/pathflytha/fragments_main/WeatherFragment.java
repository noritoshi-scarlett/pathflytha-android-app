package pl.noritoshi_scarlett.pathflytha.fragments_main;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import pl.noritoshi_scarlett.pathflytha.R;


public class WeatherFragment extends Fragment {


    public WeatherFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        ImageView imgView = view.findViewById(R.id.bitmap);

        return view;
    }

}
