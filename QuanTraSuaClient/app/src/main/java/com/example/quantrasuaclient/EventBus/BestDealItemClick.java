package com.example.quantrasuaclient.EventBus;


import com.example.quantrasuaclient.Model.BestDealModel;

public class BestDealItemClick {
    private BestDealModel bestDealModel;

    public BestDealItemClick(BestDealModel bestDealModel) {
        this.bestDealModel = bestDealModel;
    }

    public BestDealModel getBestDealModel() {
        return bestDealModel;
    }

    public void setBestDealModel(BestDealModel bestDealModel) {
        this.bestDealModel = bestDealModel;
    }
}
