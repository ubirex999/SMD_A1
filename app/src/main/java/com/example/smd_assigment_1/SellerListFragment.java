package com.example.smd_assigment_1;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Displays a list of all sellers who have listed products.
 * Buyers can tap a seller to open a chat conversation.
 */
public class SellerListFragment extends Fragment {

    private static final String DB_URL = "https://smd-assigment-1-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView rvSellerList;
    private LinearLayout emptyState;
    private SellerChatListAdapter adapter;
    private List<SellerInfo> sellerList;
    private DatabaseReference rootRef;
    private String currentUserId;

    // Holds info about each seller for the list
    static class SellerInfo {
        String sellerId;
        String sellerName;
        String lastMessage;
        long lastMessageTime;

        SellerInfo(String sellerId, String sellerName) {
            this.sellerId = sellerId;
            this.sellerName = sellerName;
            this.lastMessage = "";
            this.lastMessageTime = 0;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_seller_list, container, false);

        rvSellerList = root.findViewById(R.id.rvSellerList);
        emptyState = root.findViewById(R.id.emptyState);

        View btnBack = root.findViewById(R.id.btnBackSellerList);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        currentUserId = FirebaseAuth.getInstance().getUid();
        rootRef = FirebaseDatabase.getInstance(DB_URL).getReference();

        sellerList = new ArrayList<>();
        adapter = new SellerChatListAdapter();
        rvSellerList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSellerList.setAdapter(adapter);

        loadSellers();

        return root;
    }

    private void loadSellers() {
        // Step 1: Read all products to find distinct sellers
        rootRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                // Collect unique seller IDs from products
                Map<String, String> sellerIds = new HashMap<>(); // sellerId -> placeholder name
                for (DataSnapshot productSnap : snapshot.getChildren()) {
                    String sellerId = productSnap.child("sellerId").getValue(String.class);
                    if (sellerId != null && !sellerId.isEmpty() && !sellerId.equals(currentUserId)) {
                        sellerIds.put(sellerId, "");
                    }
                }

                if (sellerIds.isEmpty()) {
                    showEmpty(true);
                    return;
                }

                // Step 2: For each unique seller, fetch their name from /users
                final int[] remaining = {sellerIds.size()};
                for (String sid : sellerIds.keySet()) {
                    rootRef.child("users").child(sid).child("name")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot nameSnap) {
                                    if (!isAdded()) return;
                                    String name = nameSnap.getValue(String.class);
                                    if (name == null || name.isEmpty()) name = "Seller";
                                    SellerInfo info = new SellerInfo(sid, name);
                                    sellerList.add(info);
                                    remaining[0]--;
                                    if (remaining[0] <= 0) {
                                        // All sellers loaded, now fetch last messages
                                        loadLastMessages();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    remaining[0]--;
                                    if (remaining[0] <= 0 && isAdded()) {
                                        loadLastMessages();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to load sellers", Toast.LENGTH_SHORT).show();
                    showEmpty(true);
                }
            }
        });
    }

    private void loadLastMessages() {
        if (!isAdded() || sellerList.isEmpty()) {
            showEmpty(sellerList.isEmpty());
            return;
        }

        final int[] remaining = {sellerList.size()};
        for (SellerInfo info : sellerList) {
            String chatId = getChatId(currentUserId, info.sellerId);
            rootRef.child("chats").child(chatId).limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot msgSnap : snapshot.getChildren()) {
                                ChatMessage msg = msgSnap.getValue(ChatMessage.class);
                                if (msg != null) {
                                    info.lastMessage = msg.getMessage();
                                    info.lastMessageTime = msg.getTimestamp();
                                }
                            }
                            remaining[0]--;
                            if (remaining[0] <= 0 && isAdded()) {
                                // Sort by last message time (most recent first)
                                sellerList.sort((a, b) -> Long.compare(b.lastMessageTime, a.lastMessageTime));
                                adapter.notifyDataSetChanged();
                                showEmpty(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            remaining[0]--;
                            if (remaining[0] <= 0 && isAdded()) {
                                adapter.notifyDataSetChanged();
                                showEmpty(false);
                            }
                        }
                    });
        }
    }

    /**
     * Creates a deterministic chat room ID for two users
     * by sorting their UIDs alphabetically.
     */
    static String getChatId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0
                ? uid1 + "_" + uid2
                : uid2 + "_" + uid1;
    }

    private void showEmpty(boolean empty) {
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvSellerList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ──── Adapter ────────────────────────────────────────────────────────────

    private class SellerChatListAdapter extends RecyclerView.Adapter<SellerChatListAdapter.VH> {

        // A set of colors for avatar backgrounds
        private final int[] avatarColors = {
                0xFF1976D2, 0xFF388E3C, 0xFFD32F2F,
                0xFF7B1FA2, 0xFFE64A19, 0xFF00796B
        };

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_seller_chat, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            SellerInfo info = sellerList.get(position);

            // Avatar: first letter with colored circle
            String initial = info.sellerName.substring(0, 1).toUpperCase();
            holder.tvAvatar.setText(initial);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(avatarColors[Math.abs(info.sellerId.hashCode()) % avatarColors.length]);
            holder.tvAvatar.setBackground(bg);

            holder.tvName.setText(info.sellerName);

            if (info.lastMessage != null && !info.lastMessage.isEmpty()) {
                holder.tvLastMsg.setText(info.lastMessage);
                holder.tvLastMsg.setVisibility(View.VISIBLE);
            } else {
                holder.tvLastMsg.setText("Tap to start a conversation");
                holder.tvLastMsg.setVisibility(View.VISIBLE);
            }

            if (info.lastMessageTime > 0) {
                holder.tvTime.setText(formatTime(info.lastMessageTime));
                holder.tvTime.setVisibility(View.VISIBLE);
            } else {
                holder.tvTime.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ChatActivity.class);
                intent.putExtra("receiverId", info.sellerId);
                intent.putExtra("receiverName", info.sellerName);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return sellerList.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvAvatar, tvName, tvLastMsg, tvTime;

            VH(@NonNull View itemView) {
                super(itemView);
                tvAvatar = itemView.findViewById(R.id.tvSellerAvatar);
                tvName = itemView.findViewById(R.id.tvSellerName);
                tvLastMsg = itemView.findViewById(R.id.tvLastMessage);
                tvTime = itemView.findViewById(R.id.tvChatTime);
            }
        }

        private String formatTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            if (diff < 60_000) return "Just now";
            if (diff < 3_600_000) return (diff / 60_000) + "m ago";
            if (diff < 86_400_000) return (diff / 3_600_000) + "h ago";
            return new SimpleDateFormat("MMM dd", Locale.US).format(new Date(timestamp));
        }
    }
}
