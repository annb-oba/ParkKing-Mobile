package com.example.afbu.parkking;

import android.app.FragmentManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ParkedCarListAdapter extends RecyclerView.Adapter<ParkedCarListAdapter.ParkedCarViewHolder> {
    private List<ParkedCar> parkedCarList;
    private Context mCtx;

    public ParkedCarListAdapter(List<ParkedCar> parkedCarList, Context mCtx) {
        this.parkedCarList = parkedCarList;
        this.mCtx = mCtx;
    }

    @NonNull
    @Override
    public ParkedCarListAdapter.ParkedCarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.from(parent.getContext()).inflate(R.layout.parked_car_item_layout, parent, false);

        return new ParkedCarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkedCarListAdapter.ParkedCarViewHolder holder, int position) {
        ParkedCar parkedCar = parkedCarList.get(position);

        holder.plateNumberTextView.setText(parkedCar.getPlate_number());
        holder.carMakeTextView.setText(parkedCar.getCar_make());

        holder.buildingParkedTextView.setText(parkedCar.getBuilding());
        holder.floorParkedTextView.setText("Floor " + parkedCar.getFloor() + "; Section " + parkedCar.getSection() + "; Slot " + parkedCar.getSlot());

        holder.dateParkedTextView.setText(parkedCar.getTime_in());
        holder.billTextView.setText(parkedCar.getBill());

        if(!parkedCar.isCurrently_used()) {
            holder.isUsedImageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return parkedCarList.size();
    }

    public class ParkedCarViewHolder extends RecyclerView.ViewHolder {
        TextView plateNumberTextView, carMakeTextView;
        TextView buildingParkedTextView, floorParkedTextView;
        TextView dateParkedTextView, billTextView;
        ImageView isUsedImageView;
        View self;

        public ParkedCarViewHolder(View itemView) {
            super(itemView);

            self = itemView;

            plateNumberTextView = (TextView) itemView.findViewById(R.id.plateNumberTextView);
            carMakeTextView = (TextView) itemView.findViewById(R.id.carMakeTextView);

            buildingParkedTextView = (TextView) itemView.findViewById(R.id.buildingParkedTextView);
            floorParkedTextView = (TextView) itemView.findViewById(R.id.floorParkedTextView);
            dateParkedTextView = (TextView) itemView.findViewById(R.id.dateParkedTextView);
            billTextView = (TextView) itemView.findViewById(R.id.billTextView);

            isUsedImageView = (ImageView) itemView.findViewById(R.id.isUsedImageView);
        }
    }
}
