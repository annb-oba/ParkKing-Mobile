package com.example.afbu.parkking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Objects;

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
                        Intent parkedCarsIntent = new Intent(getContext(), ParkedCars.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Objects.requireNonNull(getContext()).startActivity(parkedCarsIntent);
                        } else {
                            getContext().startActivity(parkedCarsIntent);
                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commit();
        } catch (IllegalStateException e) {
            Log.d(tag, "Failed to show prompt");
        }
    }
}
