package com.example.quantrasuaserver.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Common.CustomDialog;
import com.example.quantrasuaserver.Model.ServerUserModel;
import com.example.quantrasuaserver.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int APP_REQUEST_CODE = 7171;
    // Authentication with firebase    
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private DatabaseReference serverRef;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        // login methods (phone + email)          
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build(), new AuthUI.IdpConfig.EmailBuilder().build());
        // setup firevase
        serverRef = FirebaseDatabase.getInstance().getReference(Common.SEVER_REF);
        firebaseAuth = FirebaseAuth.getInstance();
        listener = firebaseAuthLocal -> {
            FirebaseUser user = firebaseAuthLocal.getCurrentUser();
            if (user != null) {
                //Check user from firebase
                checkServerUserFromFirebase(user);
            } else {
                phoneLogin();
            }
        };
    }
    // check User isactive
    private void checkServerUserFromFirebase(FirebaseUser user) {
        CustomDialog.show(this);
        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            ServerUserModel userModel = snapshot.getValue(ServerUserModel.class);
                            if (userModel != null) {
                                if (userModel.isActive()) {
                                    goToHomeActivity(userModel);
                                } else {
                                    CustomDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Người quản lý chưa cấp quyền truy cập cho bạn", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            //User not exists in database
                            CustomDialog.dismiss();
                            showRegisterDialog(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        CustomDialog.dismiss();
                        Toast.makeText(MainActivity.this, "[USER LOGIN]" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // register
    private void showRegisterDialog(FirebaseUser user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        //
        TextInputEditText edt_name = itemView.findViewById(R.id.edt_name);
        TextInputEditText edt_phone = itemView.findViewById(R.id.edt_phone);
        AppCompatButton btn_cancel = itemView.findViewById(R.id.btn_cancel);
        AppCompatButton btn_ok = itemView.findViewById(R.id.btn_ok);

        //Set data
        if (user.getPhoneNumber() == null || TextUtils.isEmpty(user.getPhoneNumber())) {
            edt_name.setText(user.getDisplayName());
        } else
            edt_phone.setText(user.getPhoneNumber());

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog registerDialog = builder.create();
        registerDialog.show();

        btn_cancel.setOnClickListener(v -> {
            phoneLogin();
            firebaseAuth.removeAuthStateListener(listener);
        });
        btn_ok.setOnClickListener(v -> {
            String strName = Objects.requireNonNull(edt_name.getText()).toString();
            String strPhone = Objects.requireNonNull(edt_phone.getText()).toString();
            if (strName.isEmpty()) {
                edt_name.setError("Vui lòng nhập tên!");
                return;
            }
            if (strPhone.isEmpty()) {
                edt_phone.setError("Vui lòng nhập số ĐT!");
                return;
            }
            ServerUserModel serverUserModel = new ServerUserModel();
            serverUserModel.setUid(user.getUid());
            serverUserModel.setName(edt_name.getText().toString());
            serverUserModel.setPhone(edt_phone.getText().toString());
            serverUserModel.setActive(false); //Default failed

            CustomDialog.show(this);
            serverRef.child(serverUserModel.getUid())
                    .setValue(serverUserModel)
                    .addOnFailureListener(e -> {
                        CustomDialog.dismiss();
                        Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }).addOnCompleteListener(task -> {
                Toast.makeText(MainActivity.this, "Đăng ký thành công! Admin sẽ cấp quyền đăng nhập cho bạn sớm nhất", Toast.LENGTH_SHORT).show();
                registerDialog.dismiss();
                CustomDialog.dismiss();
            });

        });
    }
    
    private void goToHomeActivity(ServerUserModel serverUserModel) {
        CustomDialog.dismiss();
        Common.currentServerUser = serverUserModel;
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, false));
        startActivity(intent);
        finish();
    }
   
    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .setLogo(R.drawable.app_icon)
                .setTheme(R.style.LoginTheme)
                .build(), APP_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            Log.d("TAG", "onActivityResult: " + response);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d("TAG", "onActivityResult: " + user);
            } else {
                Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
