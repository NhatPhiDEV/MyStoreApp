package com.example.quantrasuaserver.Fragment.best_deals;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quantrasuaserver.Callback.IBestDealsCallbackListener;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Model.BestDealsModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BestDealsViewModel extends ViewModel implements IBestDealsCallbackListener {
    private final MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<BestDealsModel>> bestDealsListMutable;
    private final IBestDealsCallbackListener bestDealsCallbackListener;

    public BestDealsViewModel() {
        bestDealsCallbackListener = this;
    }

    public MutableLiveData<List<BestDealsModel>> getBestDealsListMutable() {
        if (bestDealsListMutable == null)
            bestDealsListMutable = new MutableLiveData<>();
        loadBestDeals();
        return bestDealsListMutable;
    }

    public void loadBestDeals() {
        List<BestDealsModel> temp = new ArrayList<>();
        DatabaseReference bestDealsRef = FirebaseDatabase.getInstance().getReference(Common.BEST_DEALS_REF);
        bestDealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot bestDealsSnapshot : snapshot.getChildren()) {
                    BestDealsModel bestDealsModel = bestDealsSnapshot.getValue(BestDealsModel.class);
                    if (bestDealsModel != null) {
                        bestDealsModel.setKey(bestDealsSnapshot.getKey());
                    }
                    temp.add(bestDealsModel);
                }
                bestDealsCallbackListener.onListBestDealsLoadSuccess(temp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bestDealsCallbackListener.onListBestDealsLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onListBestDealsLoadSuccess(List<BestDealsModel> bestDealsModels) {
        bestDealsListMutable.setValue(bestDealsModels);
    }

    @Override
    public void onListBestDealsLoadFailed(String message) {
        messageError.setValue(message);
    }
}