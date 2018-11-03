package com.example.afbu.parkking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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
import com.bumptech.glide.Glide;
import org.w3c.dom.Text;

import java.util.ArrayList;
public class CarOwnerRecyclerViewAdapter extends RecyclerView.Adapter<CarOwnerRecyclerViewAdapter.ViewHolder>{

    private Context mContext;
    private ArrayList<CarCoOwner> carCoOwnerArrayList;
    private CarCoOwners carCoOwners;

    public CarOwnerRecyclerViewAdapter(Context mContext, ArrayList<CarCoOwner> carCoOwnerArrayList, CarCoOwners carCoOwners) {
        this.mContext = mContext;
        this.carCoOwnerArrayList = carCoOwnerArrayList;
        this.carCoOwners = carCoOwners;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.co_owner_recycler_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarOwnerRecyclerViewAdapter.ViewHolder holder, int position) {
        final int tempPosition = position;
        holder.nameText.setText(carCoOwnerArrayList.get(position).getName());
        holder.emailText.setText(carCoOwnerArrayList.get(position).getEmail());
        Glide.with(mContext).asBitmap().load(mContext.getString(R.string.profilepictureURL)+carCoOwnerArrayList.get(position).getPicture()).into(holder.profilePic);
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carCoOwners.removeCarCoOwner(carCoOwnerArrayList.get(tempPosition).getCo_owner_id());
            }
        });
    }

    @Override
    public int getItemCount() {
        return carCoOwnerArrayList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        TextView emailText;
        ImageView profilePic;
        LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            linearLayout=itemView.findViewById(R.id.CoOwner_linear_layout);
            nameText=itemView.findViewById(R.id.CoOwner_name);
            emailText=itemView.findViewById(R.id.CoOwner_email);
            profilePic=itemView.findViewById(R.id.CoOwner_picture);
        }
    }
}
