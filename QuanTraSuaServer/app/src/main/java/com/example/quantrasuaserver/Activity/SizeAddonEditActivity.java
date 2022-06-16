package com.example.quantrasuaserver.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.EventBus.AddonSizeEditEvent;
import com.example.quantrasuaserver.EventBus.SelectAddonModel;
import com.example.quantrasuaserver.EventBus.SelectSizeModel;
import com.example.quantrasuaserver.Model.AddonModel;
import com.example.quantrasuaserver.Model.SizeModel;
import com.example.quantrasuaserver.Model.UpdateAddonModel;
import com.example.quantrasuaserver.Model.UpdateSizeModel;
import com.example.quantrasuaserver.Adapter.MyAddonAdapter;
import com.example.quantrasuaserver.Adapter.MySizeAdapter;
import com.example.quantrasuaserver.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SizeAddonEditActivity extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edt_name)
    TextInputEditText edt_name;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edt_price)
    TextInputEditText edt_price;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_create)
    Button btn_create;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_edit)
    Button btn_edit;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_addon_size)
    RecyclerView recycler_addon_size;

    //Variable
    // Adapter Size + Addon
    MySizeAdapter adapter;
    MyAddonAdapter addonAdapter;
    // Default postion
    private int drinksEditPosition = -1;
    // 
    private boolean needSave = false;
    // check addon or size 
    private boolean isAddon = false;


    //Event
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_create)
    void onCreateNew() {
        if (!isAddon) {
            if (adapter != null) {
                // Create new size (only add to adapter not save data)
                SizeModel sizeModel = new SizeModel();
                sizeModel.setName(Objects.requireNonNull(edt_name.getText()).toString());
                sizeModel.setPrice(Long.parseLong(Objects.requireNonNull(edt_price.getText()).toString()));
                adapter.addNewSize(sizeModel);
            }
        } else { //addon
            if (addonAdapter != null) {
                // Create new addon
                AddonModel addonModel = new AddonModel();
                addonModel.setName(Objects.requireNonNull(edt_name.getText()).toString());
                addonModel.setPrice(Long.parseLong(Objects.requireNonNull(edt_price.getText()).toString()));
                addonAdapter.addNewAddon(addonModel);
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_edit)
    void onEdit() {
        if (!isAddon) {
            if (adapter != null) {
                // Update size
                SizeModel sizeModel = new SizeModel();
                sizeModel.setName(Objects.requireNonNull(edt_name.getText()).toString());
                sizeModel.setPrice(Long.parseLong(Objects.requireNonNull(edt_price.getText()).toString()));
                adapter.editSize(sizeModel);
            }
        } else {
            if (addonAdapter != null) {
                // Update addon (only add to adapter not save data)
                AddonModel addonModel = new AddonModel();
                addonModel.setName(Objects.requireNonNull(edt_name.getText()).toString());
                addonModel.setPrice(Long.parseLong(Objects.requireNonNull(edt_price.getText()).toString()));
                addonAdapter.editAddon(addonModel);
            }
        }
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addon_size_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // if user click button save
                saveData();
                break;
            case android.R.id.home:
                // Check if data change but not save notify with user
                if (needSave) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Thông báo")
                            .setMessage("Cập nhật tất cả những thay đổi trước khi thoát?")
                            .setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> {
                                needSave = false;
                                saveData();
                                closeActivity();
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    closeActivity();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    //Save data
    private void saveData() {
        if (drinksEditPosition != -1) {
            Common.categorySelected.getDrinks().set(drinksEditPosition, Common.selectDrinks); // Save drinks to category
            
            // Get drinks current choose
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("drinks", Common.categorySelected.getDrinks());

            FirebaseDatabase.getInstance()
                    .getReference(Common.CATEGORY_REF) 
                    .child(Common.categorySelected.getMenu_id())
                    .updateChildren(updateData)
                    .addOnFailureListener(e -> Toast.makeText(SizeAddonEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            // Upadte status needSave and setText
                            needSave = false;
                            edt_price.setText("0");
                            edt_name.setText("");
                        }
                    });
        }
    }

    //Set value default
    private void closeActivity() {
        edt_name.setText("");
        edt_price.setText("0");
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_size_addon_edit);
        init();
    }

    private void init() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Setup layout
        recycler_addon_size.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_addon_size.setLayoutManager(layoutManager);
        recycler_addon_size.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        //Fix bug not reset value item
        EventBus.getDefault().removeStickyEvent(UpdateSizeModel.class);
        EventBus.getDefault().removeStickyEvent(UpdateAddonModel.class);
        EventBus.getDefault().removeStickyEvent(SelectAddonModel.class);
        EventBus.getDefault().removeStickyEvent(SelectSizeModel.class);
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    //Receive event
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAddonSizeReceive(AddonSizeEditEvent event) {
        if (!event.isAddon()) { // if event is size
            if (Common.selectDrinks.getSize() == null) // if size is not empty
            {
                Common.selectDrinks.setSize(new ArrayList<>());
            } else {
                adapter = new MySizeAdapter(this, Common.selectDrinks.getSize());
                drinksEditPosition = event.getPos(); //Save drinks edit to update
                recycler_addon_size.setAdapter(adapter);

                isAddon = event.isAddon();
            }
        } else //is addon
        {
            if (Common.selectDrinks.getAddon() == null) // if addon is not empty
                Common.selectDrinks.setAddon(new ArrayList<>());

            addonAdapter = new MyAddonAdapter(this, Common.selectDrinks.getAddon());
            drinksEditPosition = event.getPos(); //Save drinks edit to update
            recycler_addon_size.setAdapter(addonAdapter);

            isAddon = event.isAddon();
        }
    }

    //Update Size
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSizeModelUpdate(UpdateSizeModel event) {
        if (event.getSizeModelList() != null) {
            needSave = true;
            Common.selectDrinks.setSize(event.getSizeModelList());
        }
    }
    //Update Addon
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAddonModelUpdate(UpdateAddonModel event) {
        if (event.getAddonModel() != null) {
            needSave = true;
            Common.selectDrinks.setAddon(event.getAddonModel());
        }
    }

    //Select Size
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSizeSelectModel(SelectSizeModel event) {
        if (event.getSizeModel() != null) {
            edt_name.setText(event.getSizeModel().getName());
            edt_price.setText(String.valueOf(event.getSizeModel().getPrice()));

            btn_edit.setEnabled(true);
        } else {
            btn_edit.setEnabled(false);
        }
    }
    // Select Addon
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAddonSelectModel(SelectAddonModel event) {
        if (event.getAddonModel() != null) {
            edt_name.setText(event.getAddonModel().getName());
            edt_price.setText(String.valueOf(event.getAddonModel().getPrice()));

            btn_edit.setEnabled(true);
        } else {
            btn_edit.setEnabled(false);
        }
    }
}
