package com.example.afbu.parkking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ParkingTransactionInformationDialog extends AppCompatDialogFragment {

    private TextView amountIncurredTextView, amountTenderedTextView, parkingDurationTextView, billingDescriptionTextView;
    private View view;
    private String amountIncurred, amountTendered, parkingDuration, billingDescription;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        view = layoutInflater.inflate(R.layout.dialog_parking_history_transaction, null);
        amountIncurredTextView = (TextView) view.findViewById(R.id.transactionAmountIncurredTextView);
        amountTenderedTextView = (TextView) view.findViewById(R.id.transactionAmountTenderedTextView);
        parkingDurationTextView = (TextView) view.findViewById(R.id.parkingDurationTextView);
        billingDescriptionTextView = (TextView) view.findViewById(R.id.billingDescriptionTextView);
        amountTenderedTextView.setText(amountTendered);
        amountIncurredTextView.setText(amountIncurred);
        parkingDurationTextView.setText(parkingDuration);
        billingDescriptionTextView.setText(billingDescription);

        builder.setView(view)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        return builder.create();
    }

    public void setTransactionInformation(String amountIncurred, String amountTendered, String parkingDuration, String billingDescription) {
        this.billingDescription = billingDescription;
        this.parkingDuration = parkingDuration;
        this.amountIncurred = amountIncurred;
        this.amountTendered = amountTendered;
    }
}
