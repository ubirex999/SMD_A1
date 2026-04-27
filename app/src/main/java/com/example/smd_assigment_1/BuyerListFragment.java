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
 * For SELLERS: shows all buyers who have placed orders containing this seller's products
 * so the seller can chat with those buyers.
 * 
 * Buyer contacts are discovered from seller_orders/{sellerId}, which contains buyerName
 * and userId for each order.
 */
public class BuyerListFragment extends Fragment {

    private static final String DB_URL = "https://smd-assigment-1-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView rvList;
    private LinearLayout emptyState;
    private BuyerChatListAdapter adapter;
    private List<ContactInfo> contactList;
    private DatabaseReference rootRef;
    private String currentUserId;

    static class ContactInfo {
        String oderId;
        String name;
        String lastMessage;
        long lastMessageTime;

        ContactInfo(String oderId, String name) {
            this.oderId = oderId;
            this.name = name;
            this.lastMessage = "";
            this.lastMessageTime = 0;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_seller_list, container, false);

        rvList = root.findViewById(R.id.rvSellerList);
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

        contactList = new ArrayList<>();
        adapter = new BuyerChatListAdapter();
        rvList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvList.setAdapter(adapter);

        loadBuyers();

        return root;
    }

    private void loadBuyers() {
        // For sellers: read seller_orders/{myUid} to find unique buyers
        rootRef.child("seller_orders").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;

                        Map<String, String> buyerMap = new HashMap<>(); // buyerId -> buyerName
                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            String buyerId = orderSnap.child("userId").getValue(String.class);
                            String buyerName = orderSnap.child("buyerName").getValue(String.class);
                            if (buyerId != null && !buyerId.isEmpty()) {
                                buyerMap.put(buyerId, buyerName != null ? buyerName : "Buyer");
                            }
                        }

                        if (buyerMap.isEmpty()) {
                            showEmpty(true);
                            return;
                        }

                        for (Map.Entry<String, String> entry : buyerMap.entrySet()) {
                            contactList.add(new ContactInfo(entry.getKey(), entry.getValue()));
                        }

                        loadLastMessages();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Failed to load buyers", Toast.LENGTH_SHORT).show();
                            showEmpty(true);
                        }
                    }
                });
    }

    private void loadLastMessages() {
        if (!isAdded() || contactList.isEmpty()) {
            showEmpty(contactList.isEmpty());
            return;
        }

        final int[] remaining = {contactList.size()};
        for (ContactInfo info : contactList) {
            String chatId = SellerListFragment.getChatId(currentUserId, info.oderId);
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
                                contactList.sort((a, b) -> Long.compare(b.lastMessageTime, a.lastMessageTime));
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

    private void showEmpty(boolean empty) {
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ──── Adapter ────────────────────────────────────────────────────────────

    private class BuyerChatListAdapter extends RecyclerView.Adapter<BuyerChatListAdapter.VH> {

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
            ContactInfo info = contactList.get(position);

            String initial = info.name.substring(0, 1).toUpperCase();
            holder.tvAvatar.setText(initial);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(avatarColors[Math.abs(info.oderId.hashCode()) % avatarColors.length]);
            holder.tvAvatar.setBackground(bg);

            holder.tvName.setText(info.name);

            if (info.lastMessage != null && !info.lastMessage.isEmpty()) {
                holder.tvLastMsg.setText(info.lastMessage);
            } else {
                holder.tvLastMsg.setText("Tap to start a conversation");
            }

            if (info.lastMessageTime > 0) {
                holder.tvTime.setText(formatTime(info.lastMessageTime));
                holder.tvTime.setVisibility(View.VISIBLE);
            } else {
                holder.tvTime.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ChatActivity.class);
                intent.putExtra("receiverId", info.oderId);
                intent.putExtra("receiverName", info.name);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return contactList.size();
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
