package com.example.find_my_friends.recyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.find_my_friends.R;
import com.example.find_my_friends.userUtil.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserviewHolder> {


    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserviewHolder holder, int position, @NonNull User model) {
        //super.onBindViewHolder(holder,position);
        holder.groupTitle.setText(model.getUsername());

        //holder.groupDesc.setText(model.getGroupDesc());
        //Glide.with(holder.groupPhoto.getContext()).load(Uri.fromFile(new File(model.getGroupPhotoURI()))).into(holder.groupPhoto);
        //Glide.with(holder.groupCreatorPhoto.getContext()).load(Uri.fromFile(new File(model.getGroupPhotoURI()))).into(holder.groupCreatorPhoto);
       // holder.groupDate.setText(model.getGroupMeetDate());
       // holder.groupTime.setText(model.getGroupMeetTime());
       // holder.groupCreator.setText(model.getGroupCreatorUserID());
    }

    @NonNull
    @Override
    public UserviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_group_overview, parent, false);
        return new UserviewHolder(v);
    }

    class UserviewHolder extends RecyclerView.ViewHolder{
        TextView groupTitle;
        TextView groupDesc;
        TextView groupCreator;
        ImageView groupPhoto;
        ImageView groupCreatorPhoto;
        Button moreDetailsBTN;
        TextView groupDistance;
        TextView groupDate;
        TextView groupTime;




        public UserviewHolder(@NonNull View itemView) {
            super(itemView);
            groupTitle = itemView.findViewById(R.id.GroupTitleCV);
            groupDesc = itemView.findViewById(R.id.GroupDescriptionCV);
            groupCreator = itemView.findViewById(R.id.HostedByCV);
            groupPhoto = itemView.findViewById(R.id.GroupPhotoCV);
            groupCreatorPhoto = itemView.findViewById(R.id.ProfilePhotoCV);
            groupDistance = itemView.findViewById(R.id.GroupDistanceCV);
            groupDate = itemView.findViewById(R.id.DateGroupTextviewCV);
            groupTime = itemView.findViewById(R.id.TimeGroupTextViewCV);
            moreDetailsBTN = itemView.findViewById(R.id.MoreDetailBTNCV);
        }
    }
}
