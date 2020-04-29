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

import java.util.ArrayList;

public class UserGroupRequestsAdapter extends RecyclerView.Adapter<UserGroupRequestsAdapter.UserviewHolder> {
    private UserGroupRequestsAdapter.OnItemClickListener confirmListener;
    private UserGroupRequestsAdapter.OnItemClickListener denyListener;
    public ArrayList<User> users;

    public UserGroupRequestsAdapter(ArrayList<User> users) {
        this.users = users;
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
                        confirmListener.onItemClick(position);
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
                        denyListener.onItemClick(position);
                    }
                }
            });

        }
    }


    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    @NonNull
    @Override
    public UserviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_user_group_request, parent, false);
        return new UserviewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserviewHolder holder, int position) {
        if(users !=null && users.size() != 0 && users.get(position) != null) {
            User model = users.get(position);
            holder.userDisplayName.setText(model.getUsername());
            holder.userEmailAddress.setText(model.getUserEmailAddress());
            Glide.with(holder.userProfilePhoto.getContext()).load(model.getUserPhotoURL()).into(holder.userProfilePhoto);
        }else{
            holder.userDisplayName.getRootView().setVisibility(View.GONE);
        }
    }


    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setConfrimOnItemClickListener(OnItemClickListener listener){
        this.confirmListener = listener;
    }

    public void setDenyOnItemClickListener(OnItemClickListener listener){
        this.denyListener = listener;
    }
}
