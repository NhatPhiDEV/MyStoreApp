package com.example.quantrasuaclient.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quantrasuaclient.Adapter.ChatAdapter;
import com.example.quantrasuaclient.Model.ChatModel;
import com.example.quantrasuaclient.Model.MsgModel;
import com.example.quantrasuaclient.R;
import com.example.quantrasuaclient.Services.RetrofitAPI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Follow https://youtu.be/7_Cc36c7EW0
public class ChatActivity extends AppCompatActivity {
    RecyclerView chatsRV;
    EditText editMsg;
    FloatingActionButton sendMsg;
    String BOT_KEY = "bot";
    String USER_KEY = "user";
    ArrayList<ChatModel> chatModalArrayList;
    ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatsRV = findViewById(R.id.recycler_chat);
        editMsg = findViewById(R.id.edt_chat);
        sendMsg = findViewById(R.id.btn_send);
        chatModalArrayList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatModalArrayList, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        chatsRV.setLayoutManager(manager);
        chatsRV.setAdapter(chatAdapter);
        sendMsg.setOnClickListener(view -> {
            if (editMsg.getText().toString().isEmpty()) {
                Toast.makeText(ChatActivity.this, "Bạn chưa nhập tin nhắn...", Toast.LENGTH_SHORT).show();
                return;
            }
            getResponse(editMsg.getText().toString());
            editMsg.setText("");
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getResponse(String message) {
        chatModalArrayList.add(new ChatModel(message, USER_KEY));
        chatAdapter.notifyDataSetChanged();
        String url = "http://api.brainshop.ai/get?bid=161647&key=cgh2zkVjWp3eJNsD&uid=[uid]&msg=" + message;
        String BASE_URL = "http://api.brainshop.ai/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<MsgModel> call = retrofitAPI.getMessage(url);
        call.enqueue(new Callback<MsgModel>() {
            @Override
            public void onResponse(@NonNull Call<MsgModel> call, @NonNull Response<MsgModel> response) {
                if (response.isSuccessful()) {
                    MsgModel modal = response.body();
                    chatModalArrayList.add(new ChatModel(modal.getCnt(), BOT_KEY));
                    chatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MsgModel> call, @NonNull Throwable t) {
                chatModalArrayList.add(new ChatModel("Bạn chưa nhập tin nhắn...", BOT_KEY));
                chatAdapter.notifyDataSetChanged();

            }
        });
    }

}
