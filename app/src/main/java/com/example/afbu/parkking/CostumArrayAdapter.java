package com.example.afbu.parkking;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class CostumArrayAdapter extends ArrayAdapter<String>{


    public CostumArrayAdapter(@NonNull Context context, ArrayList<String> clients) {
        super(context, R.layout.row_layout ,clients);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.row_layout, parent, false);

        String singleItem = getItem(position);
        TextView BuildingName = (TextView) customView.findViewById(R.id.row_buildingname);
        ImageView logo = (ImageView) customView.findViewById(R.id.row_logo);

        BuildingName.setText(singleItem);
        logo.setImageResource(R.drawable.logo);
        return customView;
    }
}
