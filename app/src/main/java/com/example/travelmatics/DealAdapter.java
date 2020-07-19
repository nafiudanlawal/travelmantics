package com.example.travelmatics;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {
    ArrayList<TravelDeal> travelDeals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    public DealAdapter(){
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        travelDeals = FirebaseUtil.mDeals;
        mChildEventListener = new ChildEventListener(){
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                TravelDeal td = snapshot.getValue(TravelDeal.class);
                td.setId(snapshot.getKey());
                Log.d("Deal", td.getTitle());
                travelDeals.add(td);
                notifyItemInserted(travelDeals.size() - 1);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);

    }
    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.rv_holder, parent, false);
        return new DealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DealViewHolder holder, int position) {
        TravelDeal deal = travelDeals.get(position);
        holder.bind(deal);
    }

    @Override
    public int getItemCount() {
        return travelDeals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvTitle;
        TextView tvDescription;
        TextView tvPrice;
        ImageView imageView;
        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTtile);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            imageView = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal deal){
            tvTitle.setText(deal.getTitle());
            tvDescription.setText(deal.getDescription());
            tvPrice.setText(deal.getPrice());
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            if(!deal.getImageURL().isEmpty() && deal.getImageURL() != null){
                Picasso.get()
                        .load(deal.getImageURL())
                        .resize(160, 160)
                        .centerCrop()
                        .into(imageView);
            }

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            TravelDeal deal = travelDeals.get(position);
            Intent intent = new Intent(view.getContext(), DealActivity.class);
            intent.putExtra("Deal", deal);
            view.getContext().startActivity(intent);
        }
    }

}
