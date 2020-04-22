package com.example.find_my_friends.recyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.find_my_friends.R;
import com.example.find_my_friends.userUtil.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class UserGroupRequestsAdapter extends FirestoreRecyclerAdapter<User, UserGroupRequestsAdapter.UserviewHolder> {
    private UserGroupRequestsAdapter.OnItemClickListener confirmListener;
    private UserGroupRequestsAdapter.OnItemClickListener denyListener;

    public UserGroupRequestsAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserviewHolder holder, int position, @NonNull User model) {
        //super.onBindViewHolder(holder,position);
        holder.userDisplayName.setText(model.getUsername());
        holder.userEmailAddress.setText(model.getUserEmailAddress());
        Glide.with(holder.userProfilePhoto.getContext()).load(model.getUserPhotoURL()).into(holder.userProfilePhoto);

    }

    @NonNull
    @Override
    public UserviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_user_group_request, parent, false);
        return new UserviewHolder(v);
    }

    class UserviewHolder extends RecyclerView.ViewHolder{
        TextView userDisplayName;
        TextView userEmailAddress;
        ImageView userProfilePhoto;
        ImageButton confirmBTN;
        ImageButton denyBTN;
        //ImageView adminPhoto;





        public UserviewHolder(@NonNull final View itemView) {
            super(itemView);
            userDisplayName = itemView.findViewById(R.id.user_cardview_group_request_displayTextView);
            userEmailAddress = itemView.findViewById(R.id.user_cardview_group_request_display_email);
            userProfilePhoto = itemView.findViewById(R.id.user_cardview_group_request_profile_photo);
            confirmBTN = itemView.findViewById(R.id.user_cardview_group_request_accept_requestBTN);
            denyBTN = itemView.findViewById(R.id.user_cardview_group_request_deny_requestBTN);
            //adminPhoto = itemView.findViewById(R.id.ProfilePhotoCV);
            confirmBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //startActivity(new Intent(viewGroupRequestsBTN.getContext(), GroupDetailsActivity.class));
                    int position = getAdapterPosition();
                    //String groupID = getItem(position).getGroupID();
                    if(position != RecyclerView.NO_POSITION && confirmListener != null){
                        confirmListener.onItemClick(getSnapshots().getSnapshot(position), position, itemView);
                    }
                }
            });
            denyBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //startActivity(new Intent(viewGroupRequestsBTN.getContext(), GroupDetailsActivity.class));
                    int position = getAdapterPosition();
                    //String groupID = getItem(position).getGroupID();
                    if(position != RecyclerView.NO_POSITION && denyListener != null){
                        denyListener.onItemClick(getSnapshots().getSnapshot(position), position, itemView);
                    }
                }
            });

        }
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position, View view);
    }

    public void setConfrimOnItemClickListener(OnItemClickListener listener){
        this.confirmListener = listener;
    }

    public void setDenyOnItemClickListener(OnItemClickListener listener){
        this.denyListener = listener;
    }
}
