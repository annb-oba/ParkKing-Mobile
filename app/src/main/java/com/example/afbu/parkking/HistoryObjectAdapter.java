package com.example.afbu.parkking;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class HistoryObjectAdapter extends ArrayAdapter<HistoryObject> {
    private Context mContext;
    int mResource;

    public HistoryObjectAdapter(@NonNull Context context, int resource, @NonNull ArrayList<HistoryObject> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String building_name = getItem(position).getBuilding_name();
        String slot_id = getItem(position).getSlot_id();
        String time_in = getItem(position).getTime_in();
        String time_out = getItem(position).getTime_out();
        String amount_incurred = getItem(position).getAmount_incurred();

        //CarObject carObject = new CarObject(building_name,slot_id,time_in,time_out);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView =  inflater.inflate(mResource,parent,false);

        TextView txtbuilding_name = (TextView) convertView.findViewById(R.id.row_buildingname);
        TextView txttime_in = (TextView) convertView.findViewById(R.id.row_txt_value_timein);
        TextView txttime_out = (TextView) convertView.findViewById(R.id.row_txt_value_timeout);
        TextView txtslot_id = (TextView) convertView.findViewById(R.id.row_slot);
        TextView txt_amount = (TextView) convertView.findViewById(R.id.row_txt_value_amount);
        ImageView img = (ImageView) convertView.findViewById(R.id.row_logo);
        Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.logo);
        img.setImageBitmap(icon);
        txtbuilding_name.setText(building_name);
        txtslot_id.setText(slot_id);
        txttime_in.setText(time_in);
        txttime_out.setText(time_out);
        txt_amount.setText(amount_incurred);

        return convertView;
    }
}
