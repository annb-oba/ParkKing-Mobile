package com.example.afbu.parkking;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FloorSpinnerAdapter extends ArrayAdapter<FloorSpinnerObject> {
    private Context mContext;
    public static final String CURRENT_FLOOR_ID = "currentFloorId";
    int mResource;
    public FloorSpinnerAdapter(@NonNull Context context, int resource, @NonNull ArrayList<FloorSpinnerObject> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String id = getItem(position).getFloorID();
        String Title = getItem(position).getFloorTitle();


        FloorSpinnerObject floorSpinnerObject = new FloorSpinnerObject(id,Title);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView =  inflater.from(parent.getContext()).inflate(R.layout.floor_spinner_layout,parent,false);

        TextView tvTitle = (TextView) convertView.findViewById(R.id.title);
        ImageView image = (ImageView) convertView.findViewById(R.id.currentFloorImage);
        tvTitle.setText(Title);

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(CURRENT_FLOOR_ID, mContext.MODE_PRIVATE);
        String currentFloorID = sharedPreferences.getString("currentFloorID", "");
        if(getItem(position).getFloorID().equals(currentFloorID)){
            image.setImageResource(R.drawable.user_position);
        }

        return convertView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String id = getItem(position).getFloorID();
        String Title = getItem(position).getFloorTitle();


        FloorSpinnerObject floorSpinnerObject = new FloorSpinnerObject(id,Title);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView =  inflater.from(parent.getContext()).inflate(R.layout.floor_spinner_layout,parent,false);

        TextView tvTitle = (TextView) convertView.findViewById(R.id.title);
        ImageView image = (ImageView) convertView.findViewById(R.id.currentFloorImage);
        tvTitle.setText(Title);

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(CURRENT_FLOOR_ID, mContext.MODE_PRIVATE);
        String currentFloorID = sharedPreferences.getString("currentFloorID", "");
        if(getItem(position).getFloorID().equals(currentFloorID)){
            image.setImageResource(R.drawable.user_position);
        }

        return convertView;
    }
}
