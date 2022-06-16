package com.example.quantrasuaserver.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quantrasuaserver.Callback.IRecyclerClickListener;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.EventBus.SelectAddonModel;
import com.example.quantrasuaserver.Model.AddonModel;
import com.example.quantrasuaserver.Model.UpdateAddonModel;
import com.example.quantrasuaserver.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

// RecyclerView load partial data 
public class MyAddonAdapter extends RecyclerView.Adapter<MyAddonAdapter.MyViewHolder> {
    
    // Variables (accept access Fragments, Activity )   
    Context context;
    // List Addon    
    List<AddonModel> addonModelList;
    // Update Addon
    UpdateAddonModel updateAddonModel;
    // Postion edit
    int editPos;

    // Contructor adapter (param context, list)    
    public MyAddonAdapter(Context context, List<AddonModel> addonModelList) {
        this.context = context;
        this.addonModelList = addonModelList;
        // Default postion        
        editPos = -1;
        updateAddonModel = new UpdateAddonModel();
    }

    // Override methods RecyclerView     
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_size_addon_display, parent, false));
    }
    
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // set Data display         
        holder.txt_name.setText(addonModelList.get(position).getName());
        holder.txt_price.setText(Common.formatPrice(addonModelList.get(position).getPrice()));

        //Events
        holder.img_delete.setOnClickListener(view -> {
            addonModelList.remove(position);
            notifyItemRemoved(position);
            updateAddonModel.setAddonModel(addonModelList); //Set for event
            EventBus.getDefault().postSticky(updateAddonModel); //Send event
        });
        // Set event choose addon           
        holder.setListener((view, pos) -> {
            editPos = position;
            // Register eventbus             
            EventBus.getDefault().postSticky(new SelectAddonModel(addonModelList.get(pos)));
        });
    }
       
    @Override
    public int getItemCount() {
        return addonModelList.size();
    }
    
    // Add new Addon     
    public void addNewAddon(AddonModel addonModel) {
        addonModelList.add(addonModel);
        notifyItemInserted(addonModelList.size() - 1);
        updateAddonModel.setAddonModel(addonModelList);
        EventBus.getDefault().postSticky(updateAddonModel);
    }
    // Update Addon
    public void editAddon(AddonModel addonModel) {
        if (editPos != -1) {
            addonModelList.set(editPos, addonModel);
            notifyItemChanged(editPos);
            editPos = -1; // reset variable after success
            //Send update
            updateAddonModel.setAddonModel(addonModelList);
            EventBus.getDefault().postSticky(updateAddonModel);
        }
    }

    // Manipulation with Recyclerview   
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_name)
        TextView txt_name;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_price)
        TextView txt_price;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_delete)
        ImageView img_delete;

        Unbinder unbinder;

        IRecyclerClickListener listener;
               
        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(view -> listener
                    .onItemClickListener(view, getAdapterPosition()));
        }
    }
}
