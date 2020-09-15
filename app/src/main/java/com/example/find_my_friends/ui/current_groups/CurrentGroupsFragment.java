package com.example.find_my_friends.ui.current_groups;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.find_my_friends.GroupDetailsActivity;
import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;
import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.GroupAdapter;
import com.example.find_my_friends.userUtil.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;


import static com.example.find_my_friends.util.Constants.FIND_FRIENDS_KEY;
import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.Constants.currentUserDocument;

/**
 * the current groups fragment responsible for displaying the current groups the signed in user is a member of.
 *
 * @author Harold Carter
 * @version 3.0
 */
public class CurrentGroupsFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference groupsRef = db.collection("Groups");
    private GroupAdapter groupAdapter;
    private RecyclerView recyclerView;
    private ArrayList<Group> groups = new ArrayList<>();
    private int count = 0;
    private ProgressBar progressBar;


    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to. The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here
     * @return the View for the fragment's UI, or null.
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_current_groups, container, false);
        Toolbar toolbar = root.findViewById(R.id.current_groups_menubar);
        progressBar = root.findViewById(R.id.progressBarCurrentGroups);
        toolbar.setNavigationIcon(R.drawable.svg_menu_primary);
        toolbar.setTitle("Current Groups");
        if (getActivity() != null) {
            getActivity().setActionBar(toolbar);
        } else {
            Log.e(FIND_FRIENDS_KEY, "onCreateView: Lost reference to activity, application halted");
            getActivity().finish();
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).openDrawer();
                }
            }
        });
        recyclerView = root.findViewById(R.id.current_groups_recycler);
        loadGroups("load");
        return root;
    }

    /**
     * loads the groups into the local variable for groups then upon completion calls the function loadlist to display the list in the recycler view or update list to notify changes to the dataset
     *
     * @param mode String taking the value of "Load" or "update"
     */
    private void loadGroups(final String mode) {
        if (currentUser.getUsersMemberships() != null && currentUser.getUsersMemberships().toArray().length != 0) {
            progressBar.setVisibility(View.VISIBLE);
            count = 0;
            for (String s : currentUser.getUsersMemberships()
            ) {
                groupsRef.document(s).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Group group = documentSnapshot.toObject(Group.class);
                        groups.add(group);
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
        }
        if (mode.equals("update")) {
            groups.clear();
            updateRecyclerView();
        } else {
            setupRecyclerView();
        }
    }

    /**
     * if the groups have been iterated through the function loads the list into the recyclerview by initializing the recyclerview and setting a snapshot listener on the user's current document, so changes are automatically reloaded, else it will update the state of the progressbar.
     */
    private void loadList() {
        if (count >= currentUser.getUsersMemberships().size()) {
            count = 0;
            progressBar.setVisibility(View.INVISIBLE);
            setupRecyclerView();
            currentUserDocument.getReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        groups.clear();
                        loadGroups("update");
                    }
                }
            });
        } else {
            progressBar.setProgress(count / currentUser.getUsersMemberships().size());
        }
    }

    /**
     * if the list is complete (all groups have been iterated through and added to the groups arrayList) then it will notify the adapter the dataset has changed else update the progressbar
     */
    private void updateList() {
        if (count >= currentUser.getUsersMemberships().size()) {
            count = 0;
            progressBar.setVisibility(View.INVISIBLE);
            updateRecyclerView();

        } else {
            progressBar.setProgress(count / currentUser.getUsersMemberships().size());
        }
    }

    /**
     * notifies the recyclerview adapter that the dataset has changed and therefore update the results being displayed.
     */
    private void updateRecyclerView() {
        groupAdapter.notifyDataSetChanged();
    }

    /**
     * initializes the recylerview, called once the list of groups is loaded, not to be called if the group of lists are null or not initialized
     */
    private void setupRecyclerView() {
        if (currentUser.getUsersMemberships() != null && currentUser.getUsersMemberships().toArray().length != 0) {
            groupAdapter = new GroupAdapter(this.groups);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(groupAdapter);
            groupAdapter.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Intent intent = new Intent(getContext(), GroupDetailsActivity.class);
                    intent.putExtra("documentID", groups.get(position).getGroupID());
                    startActivity(intent);
                }
            });


        }
    }

}