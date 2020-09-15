package com.example.find_my_friends.ui.group_requests;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.find_my_friends.GroupRequestsActivity;
import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;
import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.GroupRequestsOverviewAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import static com.example.find_my_friends.util.Constants.FIND_FRIENDS_KEY;

/**
 * the group requests fragment responsible for displaying the current users groups which have had requests for new members
 *
 * @author Harold Carter
 * @version 2.0
 */
public class GroupRequestsFragment extends Fragment {
    private GroupRequestsOverviewAdapter groupRequestsOverviewAdapter;
    private RecyclerView recyclerView;
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LinearLayoutManager linearLayoutManager;
    private CollectionReference groupsRef = db.collection("Groups");

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
        View root = inflater.inflate(R.layout.fragment_group_requests, container, false);
        Toolbar toolbar = root.findViewById(R.id.groups_request_menubar);
        linearLayoutManager = new LinearLayoutManager(this.getContext());
        recyclerView = root.findViewById(R.id.group_requests_recyclerView);
        toolbar.setNavigationIcon(R.drawable.svg_menu_primary);
        toolbar.setTitle("Group Requests");
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

        setupRecyclerView();
        return root;
    }

    /**
     * initializes the recyclerview to user a firestoreRecyclerview, meaning this recyclerview is self updating and doesn't require a snapshot listener; this is because the transaction can be made with a single query.
     */
    private void setupRecyclerView() {
        Query query = groupsRef.whereEqualTo("groupCreatorUserID", mUser.getUid());
        FirestoreRecyclerOptions<Group> options = new FirestoreRecyclerOptions.Builder<Group>().setQuery(query, Group.class).build();
        groupRequestsOverviewAdapter = new GroupRequestsOverviewAdapter(options);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(groupRequestsOverviewAdapter);
        groupRequestsOverviewAdapter.setOnItemClickListener(new GroupRequestsOverviewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Intent intent = new Intent(getContext(), GroupRequestsActivity.class);
                intent.putExtra("documentID", documentSnapshot.getId());
                startActivity(intent);
            }
        });
    }

    /**
     * overrides on start to alert the firestoreRecyclerview adapter to listen for changes
     */
    @Override
    public void onStart() {
        super.onStart();
        groupRequestsOverviewAdapter.startListening();
    }

    /**
     * overrides on stop to alert the firestoreRecyclerview adapter to stop listening for changes
     */
    @Override
    public void onStop() {
        super.onStop();
        groupRequestsOverviewAdapter.stopListening();
    }


}