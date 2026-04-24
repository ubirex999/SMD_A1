package com.example.smd_assigment_1;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private MaterialButton btnSend;
    private DatabaseReference mDatabase;
    private String currentUserId, receiverId;
    private ChatAdapter adapter;
    private List<ChatMessage> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etChatMessage);
        btnSend = findViewById(R.id.btnSendChat);

        currentUserId = FirebaseAuth.getInstance().getUid();
        // For simplicity in this assignment, we use a fixed 'SellerID' as the receiver for buyers
        receiverId = "SellerID"; 

        mDatabase = FirebaseDatabase.getInstance().getReference("chats");
        chatList = new ArrayList<>();
        
        adapter = new ChatAdapter(chatList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Show latest messages at the bottom
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void sendMessage() {
        String msg = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) return;

        long timestamp = System.currentTimeMillis();
        ChatMessage chatMessage = new ChatMessage(currentUserId, receiverId, msg, timestamp);

        mDatabase.push().setValue(chatMessage)
                .addOnSuccessListener(aVoid -> etMessage.setText(""))
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Failed to send", Toast.LENGTH_SHORT).show());
    }

    private void loadMessages() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ChatMessage message = data.getValue(ChatMessage.class);
                    if (message != null) {
                        // Filter messages between these two users
                        if ((message.getSenderId().equals(currentUserId) && message.getReceiverId().equals(receiverId)) ||
                            (message.getSenderId().equals(receiverId) && message.getReceiverId().equals(currentUserId))) {
                            chatList.add(message);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                rvChat.scrollToPosition(chatList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Chat sync failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatVH> {
        private List<ChatMessage> list;
        private String currentId;

        ChatAdapter(List<ChatMessage> list, String currentId) {
            this.list = list;
            this.currentId = currentId;
        }

        @NonNull
        @Override
        public ChatVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Using a simple layout for demo, distinction is made via gravity
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ChatVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatVH holder, int position) {
            ChatMessage msg = list.get(position);
            holder.tv.setText(msg.getMessage());
            
            // Distinguish sender/receiver messages using text alignment
            if (msg.getSenderId().equals(currentId)) {
                holder.tv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                holder.tv.setTextColor(Color.BLUE);
            } else {
                holder.tv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                holder.tv.setTextColor(Color.BLACK);
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ChatVH extends RecyclerView.ViewHolder {
            TextView tv;
            ChatVH(@NonNull View itemView) {
                super(itemView);
                tv = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
