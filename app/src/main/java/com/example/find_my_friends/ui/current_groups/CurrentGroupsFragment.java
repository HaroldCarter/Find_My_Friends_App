package com.example.find_my_friends.ui.current_groups;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.find_my_friends.GroupDetailsActivity;
import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;
import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.GroupOverviewAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import static com.example.find_my_friends.util.Constants.FIND_FRIENDS_KEY;
import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.Constants.currentUserDocument;

public class CurrentGroupsFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference groupsRef = db.collection("Groups");
    private GroupOverviewAdapter groupOverviewAdapter;
    private RecyclerView recyclerView;

//this fragment does not self update till the fragment is reloaded, it works however the lack of self reloading can be fixed by reloading the fragment , or by doing what is required
    //and implement a custom client side filter to the downloaded content, and upon data change react to said change (requires a change in how requests are displayed).
    private CurrentGroupsViewModel currentGroupsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        currentGroupsViewModel =
                ViewModelProviders.of(this).get(CurrentGroupsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_current_groups, container, false);
        Toolbar toolbar = root.findViewById(R.id.current_groups_menubar);


       // getActivity().getActionBar().setIcon(R.drawable.svg_menu_primary);
       // getActivity().getActionBar().setTitle("Current Groups");

        //setting the generic toolbars settings.
        toolbar.setNavigationIcon(R.drawable.svg_menu_primary);
        toolbar.setTitle("Current Groups");


        if(getActivity() != null) {
            getActivity().setActionBar(toolbar);
        }else{
            //display an error saying the program has lost reference to itself.
            Log.e(FIND_FRIENDS_KEY, "onCreateView: Lost reference to activity, application halted");
            getActivity().finish();
        }


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });
        //

        //


        recyclerView = root.findViewById(R.id.current_groups_recycler);
        setupRecyclerView();

        //((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("banana");

        return root;
    }



    private void setupRecyclerView(){
        if(currentUser.getUsersMemberships() != null && currentUser.getUsersMemberships().toArray().length !=0) {
            Query query = groupsRef.whereIn("groupID", currentUser.getUsersMemberships());
            FirestoreRecyclerOptions<Group> options = new FirestoreRecyclerOptions.Builder<Group>().setQuery(query, Group.class).build();
            groupOverviewAdapter = new GroupOverviewAdapter(options);

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(groupOverviewAdapter);

            groupOverviewAdapter.setOnItemClickListener(new GroupOverviewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                    Intent intent = new Intent(getContext(), GroupDetailsActivity.class);
                    intent.putExtra("documentID", documentSnapshot.getId());
                    startActivity(intent);
                }
            });
            groupOverviewAdapter.startListening();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(groupOverviewAdapter != null) {
            groupOverviewAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(groupOverviewAdapter != null) {
            groupOverviewAdapter.stopListening();
        }
    }

}