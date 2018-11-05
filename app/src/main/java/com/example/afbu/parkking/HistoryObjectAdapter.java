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
import android.widget.Toast;

import java.util.List;

public class HistoryObjectAdapter extends RecyclerView.Adapter<HistoryObjectAdapter.HistoryObjectViewHolder> {
    List<HistoryObject> historyObjectList;
    Context mCtx;
    android.support.v4.app.FragmentManager mFragmentManager;

    public HistoryObjectAdapter(List<HistoryObject> historyObjectList, Context mCtx, android.support.v4.app.FragmentManager mFragmentManager) {
        this.historyObjectList = historyObjectList;
        this.mCtx = mCtx;
        this.mFragmentManager = mFragmentManager;
    }

    @NonNull
    @Override
    public HistoryObjectAdapter.HistoryObjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mCtx);
        View view = layoutInflater.from(parent.getContext()).inflate(R.layout.parking_history_item_layout, parent, false);

        return new HistoryObjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryObjectAdapter.HistoryObjectViewHolder holder, int position) {
        HistoryObject parkingHistory = historyObjectList.get(position);

        holder.amountIncurredTextView.setText(parkingHistory.getAmountIncurred());
        holder.outTimeTextView.setText(parkingHistory.getTimeOut());
        holder.inTimeTextView.setText(parkingHistory.getTimeIn());
        holder.carMakeTextView.setText(parkingHistory.getCarMake());
        holder.plateNumberTextView.setText(parkingHistory.getPlateNumber());
        holder.slotDirectoryTextView.setText(parkingHistory.getSlotDirectory());
        holder.buildingParkedTextView.setText(parkingHistory.getBuildingTitle());

        if(parkingHistory.hasTransaction()) {
            holder.hasTransactionImageView.setVisibility(View.VISIBLE);
        } else {
            holder.hasTransactionImageView.setVisibility(View.INVISIBLE);
        }

        holder.self.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTransactionInfo(parkingHistory);
            }
        });
    }

    private void getTransactionInfo(HistoryObject parkingHistory) {
        ParkingTransactionInformationDialog parkingTransactionInformationDialog = new ParkingTransactionInformationDialog();
        parkingTransactionInformationDialog.setTransactionInformation(parkingHistory.getAmountIncurred(), parkingHistory.getAmountTendered(), parkingHistory.getParkingDuration(), parkingHistory.getBillingType());
        parkingTransactionInformationDialog.show(mFragmentManager, "Parking Transaction Dialog");
    }

    @Override
    public int getItemCount() {
        return historyObjectList.size();
    }

    public class HistoryObjectViewHolder extends RecyclerView.ViewHolder {
        TextView buildingParkedTextView;
        TextView plateNumberTextView, carMakeTextView;
        TextView slotDirectoryTextView;
        TextView inTimeTextView, outTimeTextView;
        TextView amountIncurredTextView;
        ImageView hasTransactionImageView;
        View self;

        public HistoryObjectViewHolder(View itemView) {
            super(itemView);

            self = itemView;

            buildingParkedTextView = itemView.findViewById(R.id.buildingParkedTextView);
            slotDirectoryTextView = itemView.findViewById(R.id.slotDirectoryTextView);
            plateNumberTextView = itemView.findViewById(R.id.plateNumberTextView);
            carMakeTextView = itemView.findViewById(R.id.carMakeTextView);
            inTimeTextView = itemView.findViewById(R.id.timeInTextView);
            outTimeTextView = itemView.findViewById(R.id.timeOutTextView);
            amountIncurredTextView = itemView.findViewById(R.id.amountIncurredTextView);
            hasTransactionImageView = itemView.findViewById(R.id.hasTransactionImageView);
        }
    }
}
