package com.example.find_my_friends.recyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class GroupRequestsOverviewAdapter extends FirestoreRecyclerAdapter<Group, GroupRequestsOverviewAdapter.GroupOverviewHolder> {

    private FirebaseFirestore db =  FirebaseFirestore.getInstance();
    private OnItemClickListener listener;
    //private GroupOverviewHolder groupOverviewHolder;
    //private Group group;

    public GroupRequestsOverviewAdapter(@NonNull FirestoreRecyclerOptions<Group> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull GroupOverviewHolder holder, int position, @NonNull Group model) {
        holder.groupTitle.setText(model.getGroupTitle());
        holder.groupDesc.setText(model.getGroupDesc());
        Glide.with(holder.groupPhoto.getContext()).load(model.getGroupPhotoURI()).into(holder.groupPhoto);
        Glide.with(holder.groupCreatorPhoto.getContext()).load(model.getGroupCreatorUserPhotoURL()).into(holder.groupCreatorPhoto);
        holder.groupCreator.setText(("Hosted by " + model.getGroupCreatorDisplayName()));

        db.collection("Users").document(model.getGroupCreatorUserID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User temp = documentSnapshot.toObject(User.class);
                if(temp != null) {
                    Glide.with(holder.groupCreatorPhoto.getContext()).load(temp.getUserPhotoURL()).into(holder.groupCreatorPhoto);
                    holder.groupCreator.setText(("Hosted by " + temp.getUsername()));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Glide.with(holder.groupCreatorPhoto.getContext()).load(temp.getUserPhotoURL()).into(holder.groupCreatorPhoto);
                holder.groupCreator.setText(("Hosted by " + "Deleted User"));
            }
        });

        //might cause a crash because its an int, look at this line if there is a crash upon loading the group requests.

        if(model.getRequestedMemberIDS() != null) {
            holder.numberOfGroupRequests.setText(Integer.toString(model.getRequestedMemberIDS().toArray().length));
        }else{
            holder.numberOfGroupRequests.setText("0");
        }
        //ignore all warning related to locale type, it is a single number it can never be a fractional

    }



    @NonNull
    @Override
    public GroupOverviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_group_requests, parent, false);
        return new GroupOverviewHolder(v);
    }

    class GroupOverviewHolder extends RecyclerView.ViewHolder{
        TextView groupTitle;
        TextView groupDesc;
        TextView groupCreator;
        ImageView groupPhoto;
        ImageView groupCreatorPhoto;
        Button viewGroupRequestsBTN;
        TextView numberOfGroupRequests;






        public GroupOverviewHolder(@NonNull View itemView) {
            super(itemView);
            groupTitle = itemView.findViewById(R.id.group_request_cardview_title);
            groupDesc = itemView.findViewById(R.id.group_request_cardview_group_description);
            groupCreator = itemView.findViewById(R.id.group_request_cardview_hosted_by);
            groupPhoto = itemView.findViewById(R.id.group_request_cardview_group_photo);
            groupCreatorPhoto = itemView.findViewById(R.id.group_request_cardview_profile_photo);
            numberOfGroupRequests = itemView.findViewById(R.id.group_request_cardview_number_of_Requests);

            viewGroupRequestsBTN = itemView.findViewById(R.id.group_request_cardview_view_group_requestBTN);
            handleViewGroupRequestsBTN();
        }

        private void handleViewGroupRequestsBTN(){
            viewGroupRequestsBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //startActivity(new Intent(viewGroupRequestsBTN.getContext(), GroupDetailsActivity.class));
                    int position = getAdapterPosition();
                    //String groupID = getItem(position).getGroupID();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
