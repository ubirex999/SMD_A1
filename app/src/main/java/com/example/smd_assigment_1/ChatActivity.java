package com.example.smd_assigment_1;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Real-time chat between the current user and a specific receiver.
 * Messages are stored under: chats/{chatId}/{messageId}
 * where chatId is a deterministic combination of both user IDs.
 * 
 * Messages persist across app restarts because they are stored in Firebase RTDB.
 */
public class ChatActivity extends AppCompatActivity {

    private static final String DB_URL = "https://smd-assigment-1-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView rvChat;
    private EditText etMessage;
    private MaterialButton btnSend;
    private DatabaseReference chatRef;
    private String currentUserId, receiverId, receiverName;
    private ChatAdapter adapter;
    private List<ChatMessage> chatList;
    private ValueEventListener chatListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etChatMessage);
        btnSend = findViewById(R.id.btnSendChat);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.chatToolbar);
        
        currentUserId = FirebaseAuth.getInstance().getUid();

        // Get receiver info from intent
        receiverId = getIntent().getStringExtra("receiverId");
        receiverName = getIntent().getStringExtra("receiverName");

        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "Error: No recipient specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (receiverName == null || receiverName.isEmpty()) {
            receiverName = "User";
        }

        // Set toolbar title to receiver name
        toolbar.setTitle(receiverName);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Create deterministic chat ID
        String chatId = getChatId(currentUserId, receiverId);
        chatRef = FirebaseDatabase.getInstance(DB_URL).getReference("chats").child(chatId);

        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    /**
     * Creates a deterministic chat room ID for two users.
     */
    private String getChatId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0
                ? uid1 + "_" + uid2
                : uid2 + "_" + uid1;
    }

    private void sendMessage() {
        String msg = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) return;

        long timestamp = System.currentTimeMillis();
        ChatMessage chatMessage = new ChatMessage(currentUserId, receiverId, msg, timestamp);

        chatRef.push().setValue(chatMessage)
                .addOnSuccessListener(aVoid -> etMessage.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(ChatActivity.this, "Failed to send", Toast.LENGTH_SHORT).show());
    }

    private void loadMessages() {
        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ChatMessage message = data.getValue(ChatMessage.class);
                    if (message != null) {
                        chatList.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
                if (!chatList.isEmpty()) {
                    rvChat.scrollToPosition(chatList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Chat sync failed", Toast.LENGTH_SHORT).show();
            }
        };
        chatRef.addValueEventListener(chatListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatRef != null && chatListener != null) {
            chatRef.removeEventListener(chatListener);
        }
    }

    // ──── Chat Adapter ───────────────────────────────────────────────────────

    private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatVH> {
        private final List<ChatMessage> list;
        private final String currentId;

        ChatAdapter(List<ChatMessage> list, String currentId) {
            this.list = list;
            this.currentId = currentId;
        }

        @NonNull
        @Override
        public ChatVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message, parent, false);
            return new ChatVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatVH holder, int position) {
            ChatMessage msg = list.get(position);
            holder.tvMessage.setText(msg.getMessage());

            // Format time
            String time = new SimpleDateFormat("hh:mm a", Locale.US)
                    .format(new Date(msg.getTimestamp()));
            holder.tvTime.setText(time);

            boolean isSent = msg.getSenderId().equals(currentId);

            // Position bubble left/right
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.bubbleContainer.getLayoutParams();
            if (isSent) {
                params.gravity = Gravity.END;
                holder.bubbleContainer.setBackgroundResource(R.drawable.chat_bubble_sent);
                holder.tvMessage.setTextColor(Color.parseColor("#1A237E"));
            } else {
                params.gravity = Gravity.START;
                holder.bubbleContainer.setBackgroundResource(R.drawable.chat_bubble_received);
                holder.tvMessage.setTextColor(Color.parseColor("#212121"));
            }
            holder.bubbleContainer.setLayoutParams(params);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ChatVH extends RecyclerView.ViewHolder {
            TextView tvMessage, tvTime;
            LinearLayout bubbleContainer;

            ChatVH(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvChatMessage);
                tvTime = itemView.findViewById(R.id.tvChatTime);
                bubbleContainer = itemView.findViewById(R.id.bubbleContainer);
            }
        }
    }
}
