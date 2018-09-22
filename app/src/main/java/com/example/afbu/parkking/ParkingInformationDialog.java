package com.example.afbu.parkking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ParkingInformationDialog extends AppCompatDialogFragment {

    private TextView parkingRateTextView, overnightFeeTextView, parkingFeeInformationTextView;
    private View view;
    private String parkingRate, overnightFee, slotTitle;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        view = layoutInflater.inflate(R.layout.dialog_parking_fee, null);
        parkingRateTextView = (TextView) view.findViewById(R.id.parkingRateTextView);
        overnightFeeTextView = (TextView) view.findViewById(R.id.overnightFeeTextView);
        parkingFeeInformationTextView = (TextView) view.findViewById(R.id.parkingFeeInformationTextView);
        parkingRateTextView.setText(parkingRate);
        overnightFeeTextView.setText(overnightFee);
        parkingFeeInformationTextView.setText("Slot " + slotTitle + " Parking Fee Information");

        builder.setView(view)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        return builder.create();
    }

    public void setParkingRate(String parkingRate) {
        this.parkingRate = parkingRate;
    }

    public void  setOvernightFee(String overnightFee) {
        this.overnightFee = overnightFee;

        if(overnightFee.contains("Overnight Fee")) {
            this.overnightFee = overnightFee.substring("Overnight Fee ".length(), overnightFee.length());
        }
    }

    public void setSlotTitle(String slotTitle) {
        this.slotTitle = slotTitle;
    }
}
