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

/**
 *  A class containing an adapter user for displaying the information of a users request to join a group on a cardview
 *
 * @author Harold Carter
 * @version 2.0
 */
public class UserGroupRequestsAdapter extends RecyclerView.Adapter<UserGroupRequestsAdapter.UserviewHolder> {
    private UserGroupRequestsAdapter.OnItemClickListener confirmListener;
    private UserGroupRequestsAdapter.OnItemClickListener denyListener;
    public ArrayList<User> users;

    /**
     * default constructor for the user group requests adapter, takes an input of user requests and saves them as the internal variable initializing the list of users being displayed in the recyclerview
     * @param users ArrayList Users to be dispalyed in the recyclerview (typically returned by database query)
     */
    public UserGroupRequestsAdapter(ArrayList<User> users) {
        this.users = users;
    }

    /**
     * the UserviewHolder for the recyclerview, this static subclass holds the information for the specific view model displayed in the recyclerview
     */
    class UserviewHolder extends RecyclerView.ViewHolder {
        TextView userDisplayName;
        TextView userEmailAddress;
        ImageView userProfilePhoto;
        ImageButton confirmBTN;
        ImageButton denyBTN;

        /**
         * default constructor for the UserviewHolder takes the view of the item being displayed in the recyclerview.
         * sets all the internal variables for the onscreenvariables to be equal to their counterparts.
         * @param itemView View, the view that has been inflated and onscreen variable resources are being fetched from.
         */
        public UserviewHolder(@NonNull final View itemView) {
            super(itemView);
            userDisplayName = itemView.findViewById(R.id.user_cardview_group_request_displayTextView);
            userEmailAddress = itemView.findViewById(R.id.user_cardview_group_request_display_email);
            userProfilePhoto = itemView.findViewById(R.id.user_cardview_group_request_profile_photo);
            confirmBTN = itemView.findViewById(R.id.user_cardview_group_request_accept_requestBTN);
            denyBTN = itemView.findViewById(R.id.user_cardview_group_request_deny_requestBTN);
            confirmBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && confirmListener != null) {
                        confirmListener.onItemClick(position);
                    }
                }
            });
            denyBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && denyListener != null) {
                        denyListener.onItemClick(position);
                    }
                }
            });

        }
    }

    /**
     * Gets the current arraylist of users being displayed in the recycler view, note this can return null or 0 which cause instability.
     *
     * @return ArrayList Users, internal variable for users being displayed in the recyclerview.
     */
    public ArrayList<User> getUsers() {
        return users;
    }

    /**
     * Sets the current arraylist of users being displayed in the recycler view.
     *
     * @param users ArrayList Users, internal variable for users being displayed in the recyclerview.
     */
    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    /**
     * Gets the number of request's currently being displayed in the recyclerview.
     *
     * @return int the current size the internal user's arraylist (users displayed in the recyclerview for group requests)
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * overrides the oncreate view holder function, called when each instance of the recyclerview's view card is is made, this links and inflates the layout from the resources and returns the reference ot the view.
     *
     * @param parent  The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return calls the default constructor for the CurrentGroupHolder
     */
    @NonNull
    @Override
    public UserviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_user_group_request, parent, false);
        return new UserviewHolder(v);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UserviewHolder holder, int position) {
        if (users != null && users.size() != 0 && users.get(position) != null) {
            User model = users.get(position);
            holder.userDisplayName.setText(model.getUsername());
            holder.userEmailAddress.setText(model.getUserEmailAddress());
            Glide.with(holder.userProfilePhoto.getContext()).load(model.getUserPhotoURL()).into(holder.userProfilePhoto);
        } else {
            holder.userDisplayName.getRootView().setVisibility(View.GONE);
        }
    }

    /**
     * public interface for the onclick listener, passed a position as a parameter to get reference to which group was interacted with.
     *
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * sets the adapters onclick listener to the inputted onclick listener passed as a parameter by reference for the confirm BTN
     *
     * @param listener GroupAdapter.OnItemClickListener, listener to set as this adapters listener will be triggered when the confirm button is clicked.
     */
    public void setConfirmOnItemClickListener(OnItemClickListener listener) {
        this.confirmListener = listener;
    }

    /**
     * sets the adapters onclick listener to the inputted onclick listener passed as a parameter by reference for the denied BTN
     *
     * @param listener GroupAdapter.OnItemClickListener, listener to set as this adapters listener will be triggered when the deny button is clicked.
     */
    public void setDenyOnItemClickListener(OnItemClickListener listener) {
        this.denyListener = listener;
    }
}
