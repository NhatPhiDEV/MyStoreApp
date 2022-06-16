package com.example.quantrasuaserver.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.EventBus.CategoryClick;
import com.example.quantrasuaserver.EventBus.ChangeMenuClick;
import com.example.quantrasuaserver.EventBus.ToastEvent;
import com.example.quantrasuaserver.Model.FCMSendData;
import com.example.quantrasuaserver.R;
import com.example.quantrasuaserver.Remote.IFCMService;
import com.example.quantrasuaserver.Remote.RetrofitFCMClient;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quantrasuaserver.databinding.ActivityHomeBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // using update avatar
    private static final int PICK_IMAGE_REQUEST = 7171;
    //setupNavigaton
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavController navController;
    // default click
    private int menuClick = -1;
    private ImageView img_upload;
    private Uri imgUri = null;
    // container data
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    // Service  Firebase Cloud Messaging
    private IFCMService ifcmService;
    // Storge firebase
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Service Cloud Messaging
        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        //FireStorage push image
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        //Set new custom toolbar
        setSupportActionBar(binding.appBarHome.toolbar);
        //Create Topic
        subscribeToTopic(Common.createTopicOrder());
        //Update token
        updateToken();
        //Navigation Drawer Setup
        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_category, R.id.nav_order, R.id.nav_shipper
                , R.id.nav_best_deals, R.id.nav_most_popular, R.id.nav_discount)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();
        //Setup default profile display in Navigation
        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = headerView.findViewById(R.id.txt_user);
        TextView txt_phone = headerView.findViewById(R.id.txt_phone);
        Common.setSpanString("Xin chào, ", Common.currentServerUser.getName(), txt_user); //Copy function from client app
        txt_phone.setText(Common.currentServerUser.getPhone());
        menuClick = R.id.nav_category; //Default
        //Check new message form client
        checkIsOpenFromActivity();
    }
    //Check if new order accept redirect to order view 
    private void checkIsOpenFromActivity() {
        boolean isOpenFromNewOrder = getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, false);
        if (isOpenFromNewOrder) {
            navController.popBackStack();
            navController.navigate(R.id.nav_order);
            menuClick = R.id.nav_order;
        }
    }
    //Update token
    private void updateToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Common.updateToken(HomeActivity.this, task.getResult(), true, false);
                Log.e("TOKEN", "TOKEN IS: " + task.getResult());
            }
        }).addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show());

    }
    //Subscribe topic
    private void subscribeToTopic(String topicOrder) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(task -> {
            if (!task.isSuccessful())
                Toast.makeText(this, "Failed: " + task.isSuccessful(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }

    //Event bus
    // Event category click
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategoryClick(CategoryClick event) {
        if (event.isSuccess()) {
            if (menuClick != R.id.nav_drinks_list) {
                navController.navigate(R.id.nav_drinks_list);
                menuClick = R.id.nav_drinks_list;
            }
        }
    }
    //Show Notification Success
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onToastEvent(ToastEvent event) {
        if (event.getAction() == Common.ACTION.CREATE) {
            Toast.makeText(this, "Thêm mới cái gì đó thành công!", Toast.LENGTH_SHORT).show();
        } else if (event.getAction() == Common.ACTION.UPDATE) {
            Toast.makeText(this, "Cập nhật cái gì đó thành công!", Toast.LENGTH_SHORT).show();
        } else if (event.getAction() == Common.ACTION.DELETE) {
            Toast.makeText(this, "Xóa cái gì đó thành công!", Toast.LENGTH_SHORT).show();
        }
    }

    //Change navigation menu click
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onChangeMenuClick(ChangeMenuClick event) {
        if (event.isFromDrinksList()) {
            //Clear
            navController.popBackStack(R.id.nav_category, true);
            navController.navigate(R.id.nav_category);
        } else {
            navController.popBackStack(R.id.nav_drinks_list, true);
            navController.navigate(R.id.nav_drinks_list);
        }
        menuClick = -1;
    }

    // Select item in navigation      
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId()) {
            case R.id.nav_category:
                if (item.getItemId() != menuClick) {
                    navController.popBackStack(); //Remove all back stack
                    navController.navigate(R.id.nav_category);
                }
                break;
            case R.id.nav_order:
                if (item.getItemId() != menuClick) {
                    navController.popBackStack(); //Remove all back stack
                    navController.navigate(R.id.nav_order);
                }
                break;
            case R.id.nav_discount:
                if (item.getItemId() != menuClick) {
                    navController.popBackStack(); //Remove all back stack
                    navController.navigate(R.id.nav_discount);
                }
                break;
            case R.id.nav_send_news:
                showNewsDialog();
                break;
            case R.id.nav_best_deals:
                if (item.getItemId() != menuClick) {
                    navController.popBackStack(); //Remove all back stack
                    navController.navigate(R.id.nav_best_deals);
                }
                break;
            case R.id.nav_most_popular:
                if (item.getItemId() != menuClick) {
                    navController.popBackStack(); //Remove all back stack
                    navController.navigate(R.id.nav_most_popular);
                }
                break;
            case R.id.nav_shipper:
                if (item.getItemId() != menuClick) {
                    navController.popBackStack(); //Remove all back stack
                    navController.navigate(R.id.nav_shipper);
                }
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
            default:
                menuClick = -1;
                break;
        }
        menuClick = item.getItemId();
        return true;
    }

    //Sent notification new from customer
    private void showNewsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_news_system, null);

        //Views
        TextInputEditText edt_title = itemView.findViewById(R.id.edt_title);
        TextInputEditText edt_content = itemView.findViewById(R.id.edt_content);
        TextInputEditText edt_link = itemView.findViewById(R.id.edt_link);
        TextInputLayout layout_link = itemView.findViewById(R.id.layout_link);
        img_upload = itemView.findViewById(R.id.img_upload);
        RadioButton rdi_none = itemView.findViewById(R.id.rdi_none);
        RadioButton rdi_link = itemView.findViewById(R.id.rdi_link);
        RadioButton rdi_upload = itemView.findViewById(R.id.rdi_image);

        //Event
        rdi_none.setOnClickListener(view -> {
            layout_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.GONE);
        });
        rdi_link.setOnClickListener(view -> {
            layout_link.setVisibility(View.VISIBLE);
            //edt_link.setVisibility(View.VISIBLE);
            img_upload.setVisibility(View.GONE);
        });
        rdi_upload.setOnClickListener(view -> {
            layout_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.VISIBLE);
        });
        img_upload.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        builder.setView(itemView);
        builder.setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> {
            String strTitle = Objects.requireNonNull(edt_title.getText()).toString();
            String strContent = Objects.requireNonNull(edt_content.getText()).toString();
            String strLink =  Objects.requireNonNull(edt_link.getText()).toString();
            if (rdi_none.isChecked()) {
                sendNews(strTitle, strContent);
            } else if (rdi_link.isChecked()) {
                sendNews(strTitle, strContent, strLink);
            } else if (rdi_upload.isChecked()) {
                if (imgUri != null) {
                    AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Uploading...").create();
                    dialog.show();
                    String file_name = UUID.randomUUID().toString();
                    StorageReference newsImages = storageReference.child("news/" + file_name);
                    newsImages.putFile(imgUri).addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }).addOnSuccessListener(taskSnapshot -> {
                        dialog.dismiss();
                        newsImages.getDownloadUrl().addOnSuccessListener(uri ->
                                sendNews(edt_title.getText().toString(), edt_content.getText().toString(), uri.toString()));
                    }).addOnProgressListener(snapshot -> {
                        double progress = Math.round(100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                    });
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    //Send new notification to client app
    private void sendNews(String title, String content, String url) {
        Map<String, String> notificationData = new HashMap<>();
        notificationData.put(Common.NOTIFY_TITLE, title);
        notificationData.put(Common.NOTIFY_CONTENT, content);
        notificationData.put(Common.IS_SEND_IMAGE, "true");
        notificationData.put(Common.IMAGE_URL, url);

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(), notificationData);

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Vui lòng chờ...").create();
        dialog.show();

        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    dialog.dismiss();
                    if (fcmResponse.getMessage_id() != 0) {
                        Toast.makeText(this, "Thông báo đã được gửi đi!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Thông báo gửi bị failed!", Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }
    //Send message
    private void sendNews(String title, String content) {
        Map<String, String> notificationData = new HashMap<>();
        notificationData.put(Common.NOTIFY_TITLE, title);
        notificationData.put(Common.NOTIFY_CONTENT, content);
        notificationData.put(Common.IS_SEND_IMAGE, "false");

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(), notificationData);

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Vui lòng chờ...").create();
        dialog.show();

        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    dialog.dismiss();
                    if (fcmResponse.getMessage_id() != 0) {
                        Toast.makeText(this, "Thông báo đã gửi đi rồi nè!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Thông báo gửi bị failed òi", Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }
    //Logout
    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> {
                    Common.selectDrinks = null;
                    Common.categorySelected = null;
                    Common.currentServerUser = null;
                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    // upload image
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imgUri = data.getData();
                img_upload.setImageURI(imgUri);
            }
        }
    }
}
