package com.example.afbu.parkking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class SaveSlotPromptDialog extends AppCompatDialogFragment {

    private View view;
    private TextView slotParkedTextView;
    private String slotTitle;
    private AlertDialog.Builder builder;

    public void setSlotTitle(String slotTitle) {
        this.slotTitle = slotTitle;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        view = layoutInflater.inflate(R.layout.dialog_ask_save_slot, null);
        TextView slotParkedTextView = (TextView) view.findViewById(R.id.slotParkedTextView);
        slotParkedTextView.setText("You are currently parked in Slot " + slotTitle);

        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }
}
