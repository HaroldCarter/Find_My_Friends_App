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

/**
 *  A class containing an adapter user for displaying overview of group requests on a cardview
 *
 * @author Harold Carter
 * @version 1.0
 */
public class GroupRequestsOverviewAdapter extends FirestoreRecyclerAdapter<Group, GroupRequestsOverviewAdapter.GroupOverviewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private OnItemClickListener listener;

    /**
     * default overview adatper constructor which sets the super's internal variable to be equal to the passed collection/list of groups (handled by the firestoreRecyclerAdapter)
     *
     * @param options FirestoreRecyclerOptions<Group> the mutable List of groups that are being displayed in the recyclerview (used by FirestoreRecyclerAdapter adapters>
     */
    public GroupRequestsOverviewAdapter(@NonNull FirestoreRecyclerOptions<Group> options) {
        super(options);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position  The position of the item within the adapter's data set.
     * @param model the mode (group) that is being displayed in this view instance.
     */
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
                if (temp != null) {
                    Glide.with(holder.groupCreatorPhoto.getContext()).load(temp.getUserPhotoURL()).into(holder.groupCreatorPhoto);
                    holder.groupCreator.setText(("Hosted by " + temp.getUsername()));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                holder.groupCreator.setText(("Hosted by " + "Deleted User"));
            }
        });
        if (model.getRequestedMemberIDS() != null) {
            holder.numberOfGroupRequests.setText((Integer.toString(model.getRequestedMemberIDS().toArray().length)));
        } else {
            holder.numberOfGroupRequests.setText("0");
        }

    }


    /**
     * overrides the oncreate view holder function, called when each instance of the recyclerview's view card is is made, this links and inflates the layout from the resources and returns the reference ot the view.
     *
     * @param parent  The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return calls the default constructor for the GroupOverviewHolder
     */
    @NonNull
    @Override
    public GroupOverviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_group_requests, parent, false);
        return new GroupOverviewHolder(v);
    }

    /**
     * the GroupOverviewHolder for the recyclerview, this subclass holds the information for the specific view model displayed in the recyclerview
     */
    class GroupOverviewHolder extends RecyclerView.ViewHolder {
        TextView groupTitle;
        TextView groupDesc;
        TextView groupCreator;
        ImageView groupPhoto;
        ImageView groupCreatorPhoto;
        Button viewGroupRequestsBTN;
        TextView numberOfGroupRequests;

        /**
         * default constructor for the GroupOverviewHolder takes the itemview as a parameter and links its internal variables to that of the onscreen variables in the view.
         *
         * @param itemView the view that is representing the data held in this viewholder.
         */
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

        /**
         * handles the click of the view requests button for every group adapter in the recycler view, this alerts the internal listener and passes the position to the listener so reference to the group being clicked can be made
         */
        private void handleViewGroupRequestsBTN() {
            viewGroupRequestsBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    /**
     * public interface for the onclick listener, passed a position as a parameter to get reference to which group was interacted with.
     *
     */
    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    /**
     * sets the adapters onclick listener to the inputted onclick listener passed as a parameter by reference
     *
     * @param listener OnItemClickListener, listener to set as this adapters listener will be triggered when the view group requests button is clicked.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
