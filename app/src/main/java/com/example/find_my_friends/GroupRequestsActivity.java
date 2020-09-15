package com.example.find_my_friends;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.UserGroupRequestsAdapter;
import com.example.find_my_friends.userUtil.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

import static com.example.find_my_friends.groupUtil.GroupUtil.appendMemberGroupOnly;
import static com.example.find_my_friends.userUtil.UserUtil.appendMembership;
import static com.example.find_my_friends.userUtil.UserUtil.removeMembershipRequest;

/**
 * The activity responsible for allowing a group creator to approve or deny group requests made by third-party members (not the current user).
 *
 * @author Harold Carter
 * @version 3.0
 */
public class GroupRequestsActivity extends AppCompatActivity {

    private UserGroupRequestsAdapter userAdapter;
    private RecyclerView recyclerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("Users");
    private Group group;
    private ArrayList<User> users = new ArrayList<>();
    private TextView messageToUserTextview;
    private DocumentReference docRef;
    private Toolbar toolbar;
    private int count = 0;
    private boolean firstLoad = true;


    /**
     * overrides the default on-create function and within this the inflated view is set as a content view, and from this onscreen variables are stored as local variables of the class, then onclick handlers and loading/initializing relevant resources is handled/implemented.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_requests);

        toolbar = findViewById(R.id.group_requests_activity_menubar);
        recyclerView = findViewById(R.id.group_requests_users_recycler);
        messageToUserTextview = findViewById(R.id.group_request_user_message_textview);


        toolbar.setNavigationIcon(R.drawable.svg_back_arrow_primary);
        toolbar.setTitle("Group Requests");


        this.setActionBar(toolbar);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        handleLoadingData();


    }

    /**
     *
     */
    public void handleLoadingData() {
        String documentID = getIntent().getStringExtra("documentID");
        if (documentID != null) {
            this.docRef = db.collection("Groups").document(documentID);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    group = documentSnapshot.toObject(Group.class);
                    //setupRecyclerView();
                    if (group != null) {
                        toolbar.setTitle(group.getGroupTitle() + " requests");
                    }
                    loadUsers("load");
                }
            });
        }
    }

    /**
     * load the users that have requested to join the group (stored as a class variable), this is done manually by checking the sub-collection of member requests and then fetching each user document, this is then places into an array list of users that is given to the recycler view.
     * if the list of users is empty, then the recyclerview is updates to be empty if the mode was to be updated.
     * if the list of users is empty, if the mode was not update but to load, then the list of users is Loaded (even though this is 0) the recyclerview still needs to be initialised.
     *
     * @param mode mode of loading, a string to represent the desired action of the function, to update users or to initialize the recylerview
     */
    private void loadUsers(final String mode) {
        if (group.getRequestedMemberIDS() != null && group.getRequestedMemberIDS().size() != 0) {
            count = 0;
            for (String s : group.getRequestedMemberIDS()
            ) {
                userRef.document(s).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        users.add(user);
                        count++;
                        if (mode.equals("update")) {
                            updateList();
                        } else {
                            loadList();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        count++;
                        if (mode.equals("update")) {
                            updateList();
                        } else {
                            loadList();
                        }
                    }
                });

            }
        } else {
            if (mode.equals("update")) {
                users.clear();
                updateRecyclerView();
            } else {
                setupRecyclerView();
            }
        }
    }

    /**
     * adds a snapshot listener to the document reference of the group, so that if the group changes then this list of requested members can self update, this callback is triggered any time a change to the group document on the server is detected, meaning that the updates to the recycler view happen in real time to changes on the server
     * upon the snapshot changing the the users are cleared and the updated reference is downloaded/cast to the local variable instance of a group and then the loadUsers function is called with the parameter mode being set to "update".
     * <p>
     * this function is called within the loadUsers function to to clear a cyclic pattern of snapshot listeners, this function is only permitted to run once through means of a local boolean variable. and only if the count (another local variable) is equal to that of the number of members of the group (list finished loading)
     */
    private void loadList() {
        if ((count >= group.getRequestedMemberIDS().size()) && firstLoad) {
            count = 0;
            setupRecyclerView();
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("TAG", "listen:error", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        group = documentSnapshot.toObject(Group.class);
                        users.clear();
                        loadUsers("update");
                    }
                }
            });
            firstLoad = false;
        }
    }

    /**
     * A function that is called for each cycle of loading users however simply uses an if statement to check if the list of loaded users (internal variable count) is equal to that of the total number of group requests, if so then this function calls the updateRecyclerView function.
     */
    private void updateList() {
        if (count >= group.getRequestedMemberIDS().size()) {
            count = 0;
            updateRecyclerView();

        }
    }

    /**
     * notifies the useradapter given to the recycler view that its dataset has changed inturn updating the contents of the recycler view, the reason this data is not require to be overwritten in the user adatper is because arraylists are passed by reference, therefore mutable in this class, meaning any change made within this class is mirror/equal to that in the useradatpers arraylist (beacuse its the same arraylist in memory)
     */
    private void updateRecyclerView() {
        userAdapter.notifyDataSetChanged();
    }


    /**
     * used for intial setup of the recycler view, if the requested members are returned as null or length 0 (no requests made) then the message no new groups requests is displayed else, the recycler view is initialized and handle onclick for confirm and deny listeners are provided through the function handleConfirmOnclick for the primary and handleDenyOnclick for the latter
     */
    private void setupRecyclerView() {
        if (group.getRequestedMemberIDS() == null) {
            messageToUserTextview.setText(R.string.message_no_new_requests);
            messageToUserTextview.setVisibility(View.VISIBLE);
            return;
        } else if (group.getRequestedMemberIDS().toArray().length == 0) {
            messageToUserTextview.setText(R.string.message_no_new_requests);
            messageToUserTextview.setVisibility(View.VISIBLE);
        } else {
            messageToUserTextview.setVisibility(View.INVISIBLE);
        }
        userAdapter = new UserGroupRequestsAdapter(users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);
        handleConfirmOnclick();
        handleDenyOnclick();
    }


    /**
     * as mentioned in the prior function setupRecyclerview, where this function is called, this function simply provides an instance of the interface described in the userAdapter,  upon clicking the confirm button on the useradapter this listener is called, and such within this callback, the position of the user in the arraylist is taken and therefore the user which was
     * clicked on is then known, this means that then the user id can be used to request the document for that user from the database, this then has the request for joining this group removed from its document;  and has the membership for the group concern appended to its memberships
     * <p>
     * the same is achieved for the group concurrently through calling grouputil functions.
     */
    private void handleConfirmOnclick() {
        userAdapter.setConfirmOnItemClickListener(new UserGroupRequestsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (group != null && group.getMembersOfGroupIDS().toArray().length >= 10) {
                    Snackbar.make(recyclerView, "this Group is already full", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                User user = users.get(position);
                db.collection("Users").document(user.getUID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String userUID = (String) documentSnapshot.get("uid");
                        //remove the group request
                        if (group != null && group.getRequestedMemberIDS() != null && (userUID != null)) {
                            group.getRequestedMemberIDS().remove(userUID);
                            User userSnap = documentSnapshot.toObject(User.class);
                            if (userSnap != null) {
                                removeMembershipRequest(group.getGroupID(), documentSnapshot);

                            }

                            docRef.update("requestedMemberIDS", FieldValue.arrayRemove(userUID));
                        }
                        if (userUID != null && group != null) {
                            appendMemberGroupOnly(group, userUID);
                            User userSnap = documentSnapshot.toObject(User.class);
                            if (userSnap != null) {
                                appendMembership(group.getGroupID(), documentSnapshot);
                            }
                            docRef.update("membersOfGroupIDS", FieldValue.arrayUnion(userUID));
                            Snackbar.make(recyclerView, "Member Added to group", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            }
        });
    }

    /**
     * as mentioned in the prior function setupRecyclerview, where this function is called, this function simply provides an instance of the interface described in the userAdapter,  upon clicking the deny button on the useradapter this listener is called, and such within this callback, the position of the user in the arraylist is taken and therefore the user which was
     * clicked on is then known; from this the user document can be requested and upon confirming this user in-fact does still exist, then the request to join the group is removed from both the users document and the group itself.
     */
    private void handleDenyOnclick() {
        userAdapter.setDenyOnItemClickListener(new UserGroupRequestsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                User user = users.get(position);
                db.collection("Users").document(user.getUID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String userUID = (String) documentSnapshot.get("uid");
                        if (group != null && group.getRequestedMemberIDS() != null && (userUID != null)) {
                            group.getRequestedMemberIDS().remove(userUID);
                            Snackbar.make(recyclerView, "Group request Removed", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            docRef.update("requestedMemberIDS", FieldValue.arrayRemove(userUID));
                            User userSnap = documentSnapshot.toObject(User.class);
                            if (userSnap != null) {
                                removeMembershipRequest(group.getGroupID(), documentSnapshot);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(recyclerView, "Failed to deny member", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });

            }
        });
    }

    /**
     * blank override for use in future iterations
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * blank override for use in future iterations
     */
    @Override
    protected void onStop() {
        super.onStop();

    }

}
