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
import java.util.List;
import android.content.SharedPreferences;
public class CarObjectAdapter extends ArrayAdapter<CarObject> {
    private Context mContext;
    private SharedPreferences SharedPreference;
    private SharedPreferences.Editor editor;
    private String ProfileID;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    int mResource;
    public CarObjectAdapter(@NonNull Context context, int resource, @NonNull ArrayList<CarObject> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        SharedPreference = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        ProfileID = SharedPreference.getString(PROFID_KEY, "");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
         String id = getItem(position).getId();
         String plateNumber = getItem(position).getPlate_number();
         String model = getItem(position).getModel();
         String brand = getItem(position).getBrand();

         CarObject carObject = new CarObject(id,plateNumber,model,brand);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView =  inflater.inflate(mResource,parent,false);

        TextView tvPlateNumber = (TextView) convertView.findViewById(R.id.textView1);
        TextView tvBrand = (TextView) convertView.findViewById(R.id.textView2);
        TextView tvModel = (TextView) convertView.findViewById(R.id.textView3);
        ImageView image = (ImageView) convertView.findViewById(R.id.imageView1);
        tvPlateNumber.setText(plateNumber);
        tvBrand.setText(brand);
        tvModel.setText(model);

        if(getItem(position).getUsed_by().equals(ProfileID)){
            image.setImageResource(R.drawable.logo);
        }

        return convertView;
    }
}
