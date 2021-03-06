package com.example.quantrasuaserver.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quantrasuaserver.Model.DiscountModel;
import com.example.quantrasuaserver.R;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyDiscountAdapter extends RecyclerView.Adapter<MyDiscountAdapter.MyViewHolder> {

    Context context;
    List<DiscountModel> discountModelList;
    SimpleDateFormat simpleDateFormat;

    @SuppressLint("SimpleDateFormat")
    public MyDiscountAdapter(Context context, List<DiscountModel> discountModelList) {
        this.context = context;
        this.discountModelList = discountModelList;
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_discount_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_code.setText(new StringBuilder("Mã Code: ").append(discountModelList.get(position).getKey()));
        holder.txt_percent.setText(new StringBuilder("Phần trăm: ").append(discountModelList.get(position).getPercent()).append("%"));
        holder.txt_valid.setText(new StringBuilder("Hạn sử dụng: ").append(simpleDateFormat.format(discountModelList.get(position).getUntilDate())));
    }

    @Override
    public int getItemCount() {
        return discountModelList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_code)
        TextView txt_code;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_percent)
        TextView txt_percent;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_valid)
        TextView txt_valid;

        Unbinder unbinder;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
