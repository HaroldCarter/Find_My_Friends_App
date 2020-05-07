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
import androidx.lifecycle.ViewModelProviders;
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

public class CurrentGroupsFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference groupsRef = db.collection("Groups");
    private GroupAdapter groupAdapter;
    private RecyclerView recyclerView;
    private ArrayList<Group> groups = new ArrayList<>();
    private int count = 0;
    private ProgressBar progressBar;

//this fragment does not self update till the fragment is reloaded, it works however the lack of self reloading can be fixed by reloading the fragment , or by doing what is required
    //and implement a custom client side filter to the downloaded content, and upon data change react to said change (requires a change in how requests are displayed).
    private CurrentGroupsViewModel currentGroupsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        currentGroupsViewModel =
                ViewModelProviders.of(this).get(CurrentGroupsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_current_groups, container, false);
        Toolbar toolbar = root.findViewById(R.id.current_groups_menubar);
        progressBar = root.findViewById(R.id.progressBarCurrentGroups);



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
                if(getActivity() != null) {
                    ((MainActivity) getActivity()).openDrawer();
                }
            }
        });



        recyclerView = root.findViewById(R.id.current_groups_recycler);

        loadGroups("load");



        return root;
    }



    private void loadGroups(final String mode){
        if(currentUser.getUsersMemberships() != null && currentUser.getUsersMemberships().toArray().length !=0) {
            //android does not offer a means to submit multiple tasks and collate output, very basic function that is just missing.
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
                        if(mode.equals("update")){
                            updateList();
                        }else {
                            loadList();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        count++;
                        if(mode.equals("update")){
                            updateList();
                        }else {
                            loadList();
                        }
                    }
                });

            }
        }
        if(mode.equals("update")){
            groups.clear();
            updateRecyclerView();
        }else{
            setupRecyclerView();
        }
    }




    private void loadList(){
        if(count >= currentUser.getUsersMemberships().size()){
            count = 0;
            progressBar.setVisibility(View.INVISIBLE);
            //continue to load the groups arraylist in to the recyclerView.
            setupRecyclerView();
            //now listen out for changes to the document once loaded.
            currentUserDocument.getReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(documentSnapshot != null && documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        groups.clear();
                        //now update the view.
                        loadGroups("update");
                    }
                }
            });

        }else{
            progressBar.setProgress(count/currentUser.getUsersMemberships().size());
        }
    }

    private void updateList(){
        if(count >= currentUser.getUsersMemberships().size()){
            count = 0;
            progressBar.setVisibility(View.INVISIBLE);
            //continue to load the groups arraylist in to the recyclerView.
            updateRecyclerView();

        }else{
            progressBar.setProgress(count/currentUser.getUsersMemberships().size());
        }
    }

    private void updateRecyclerView(){
        groupAdapter.notifyDataSetChanged();
    }


    private void setupRecyclerView(){
        if(currentUser.getUsersMemberships() != null && currentUser.getUsersMemberships().toArray().length !=0) {

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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


}