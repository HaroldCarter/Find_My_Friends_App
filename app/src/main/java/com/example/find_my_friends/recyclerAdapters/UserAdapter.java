package com.example.find_my_friends.recyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.find_my_friends.R;
import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.userUtil.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import static com.example.find_my_friends.groupUtil.GroupUtil.isUserAlreadyCompleted;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserviewHolder> {
    private Group group;


    //need to figure out a way to deteched when the user has scrolled down, and then request to load the next 10 group requests.


    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options, Group group) {
        super(options);
        this.group = group;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserviewHolder holder, int position, @NonNull User model) {
        //super.onBindViewHolder(holder,position);
        holder.userDisplayName.setText(model.getUsername());
        holder.userEmailAddress.setText(model.getUserEmailAddress());
        Glide.with(holder.userProfilePhoto.getContext()).load(model.getUserPhotoURL()).into(holder.userProfilePhoto);
        if(group != null && isUserAlreadyCompleted(group, model)){
            holder.adminPhoto.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public UserviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_user_overview, parent, false);
        return new UserviewHolder(v);
    }

    class UserviewHolder extends RecyclerView.ViewHolder{
        TextView userDisplayName;
        TextView userEmailAddress;
        ImageView userProfilePhoto;
        ImageView adminPhoto;





        public UserviewHolder(@NonNull View itemView) {
            super(itemView);
            userDisplayName = itemView.findViewById(R.id.user_cardview_displayTextView);
            userEmailAddress = itemView.findViewById(R.id.user_cardview_display_email);
            userProfilePhoto = itemView.findViewById(R.id.user_cardview_profile_photo);
            adminPhoto = itemView.findViewById(R.id.ProfilePhotoCV);

        }
    }
}
