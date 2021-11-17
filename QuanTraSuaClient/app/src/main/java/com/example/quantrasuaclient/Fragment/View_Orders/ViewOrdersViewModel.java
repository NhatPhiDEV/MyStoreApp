package com.example.quantrasuaclient.Fragment.View_Orders;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quantrasuaclient.Model.OrderModel;

import java.util.List;

public class ViewOrdersViewModel extends ViewModel {
    private final MutableLiveData<List<OrderModel>> mutableLiveDataOrderList;

    public ViewOrdersViewModel(){
        mutableLiveDataOrderList = new MutableLiveData<>();
    }

    public MutableLiveData<List<OrderModel>> getMutableLiveDataOrderList() {
        return mutableLiveDataOrderList;
    }

    public void setMutableLiveDataOrderList(List<OrderModel> orderList) {
        mutableLiveDataOrderList.setValue(orderList);
    }
}