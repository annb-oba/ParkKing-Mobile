package com.example.afbu.parkking;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class NotificationRecyclerViewAdapter extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.ViewHolder>{
    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private String ProfileID;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Query notif_ref;
    private Context mContext;
    private ArrayList<Notification> notificationArrayList;
    public NotificationRecyclerViewAdapter(Context context, ArrayList<Notification> notificationArrayList) {
        this.mContext=context;
        this.notificationArrayList=notificationArrayList;
        SharedPreference = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        ProfileID = SharedPreference.getString(PROFID_KEY, "");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_recycler_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if(notificationArrayList.get(position).getIs_read().equals("false")){
            holder.notificationLinearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.colorSelectedGray));
        }
        else{
            holder.notificationLinearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
        }
        switch(notificationArrayList.get(position).getModule()){
            case "co_owner_request":
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.shared_cars));
                break;
            case "incident_report":
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.incident_report));
                break;
            case "closed_parking_slot":
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.closed));
                break;
            case "parking_transaction":
            case "parking_transaction_entrance":
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.notif_icon_parking_transaction));
                break;
            case "parked_car":
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.notif_icon_parked_car));
                break;
            case "tenant_registration":
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.notif_icon_added_tenant));
                break;
            case "tenant_removal":
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.notif_icon_removed_tenant));
                break;
            case "facility_closing":
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.notif_icon_closing_facility));
                break;
            default:
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.open));

        }
        holder.titleText.setText(notificationArrayList.get(position).getTitle());
        holder.messageText.setText(notificationArrayList.get(position).getMessage());
        holder.dateText.setText(notificationArrayList.get(position).getDate());


        holder.notificationLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(notificationArrayList.get(position).getModule()){
                    case "co_owner_request":
                        //send request id for co ownership in intent
                        Intent i = new Intent(mContext, AcceptCoOwner.class);
                        i.putExtra("request_id",notificationArrayList.get(position).getRequest_id());
                        mContext.startActivity(i);
                        break;
                    case "closed_parking_slot":
                    case "left_parking_slot":
                    case "parking_transaction":
                        Intent parkingHistoryIntent = new Intent(mContext, ParkingHistory.class);
                        mContext.startActivity(parkingHistoryIntent);
                        break;
                    case "parked_car":
                    case "facility_closing":
                        Intent parkedCarsIntent = new Intent(mContext, ParkedCars.class);
                        mContext.startActivity(parkedCarsIntent);
                        break;
                    default:
                        //do nothing on click
                }
                holder.notificationLinearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
                //set color to white when clicked
                database.getReference().child("notif_individual").child(ProfileID).child(notificationArrayList.get(position).getId()).child("is_read").setValue("true");
            }
        });



    }

    @Override
    public int getItemCount() {
        return notificationArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView titleText;
        TextView messageText;
        TextView dateText;
        ImageView icon;
        LinearLayout notificationLinearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            titleText=itemView.findViewById(R.id.notification_title);
            messageText=itemView.findViewById(R.id.notification_message);
            dateText=itemView.findViewById(R.id.notification_date);
            icon=itemView.findViewById(R.id.notification_icon);
            notificationLinearLayout=itemView.findViewById(R.id.notification_linearLayout);
        }
    }
}
