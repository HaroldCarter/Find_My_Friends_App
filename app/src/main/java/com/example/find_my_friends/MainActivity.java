package com.example.find_my_friends;

import android.os.Bundle;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.find_my_friends.userUtil.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.net.Uri;

import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.Constants.currentUserDocument;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private Button logoutButton;
    private FirebaseAuth firebaseAuth;
    public FirebaseUser firebaseUser;
    private ImageView profilePhoto;
    private TextView usernameTextview;
    private TextView emailTextview;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        db.collection("Users").document(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                currentUserDocument = task.getResult();
                if(currentUserDocument != null) {
                    currentUser = currentUserDocument.toObject(User.class);
                }
            }
        });



                drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        logoutButton = findViewById(R.id.logOutButton);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_current_groups, R.id.nav_map_overview, R.id.nav_my_groups,
                R.id.nav_settings, R.id.nav_group_requests)
                .setDrawerLayout(drawer)
                .build();



        handleLogoutBTN();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //no longer required as no app bar is in use.
        //NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);




        profilePhoto =  navigationView.getHeaderView(0).findViewById(R.id.nav_draw_profile_photo);
        usernameTextview = navigationView.getHeaderView(0).findViewById(R.id.nav_draw_user_name);
        emailTextview = navigationView.getHeaderView(0).findViewById(R.id.nav_draw_user_email);

        if(firebaseUser != null) {
            //usernameTextview.setText(firebaseUser.getDisplayName());
            //can't find the resource in the navigation header? then work on getting the image, setting the image in the reg, amongst their username.
            String temp = firebaseUser.getEmail();
            emailTextview.setText(temp);
            usernameTextview.setText(firebaseUser.getDisplayName());
            //now we need to update the profile photo somehow, and update this every time the database changes. and further research into real time databases
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            //StorageReference  storageReference = firebaseStorage.getReference().child(firebaseUser.getPhotoUrl());
            //Picasso.get().load("http://i.imgur.com/DvpvklR.png").into(imageView);
            Uri uri = firebaseUser.getPhotoUrl();
            if(firebaseUser.getPhotoUrl()!= null) {
                String string = firebaseUser.getPhotoUrl().toString();
                Glide.with(this).load(firebaseUser.getPhotoUrl()).into(profilePhoto);
            }


            //profilePhoto
        }
    }



    private void handleLogoutBTN(){
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //log out here (remove authentication of the user)
                //remove authentication.
                firebaseAuth.signOut();
                finish();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void openDrawer(){
        drawer.openDrawer(GravityCompat.START);
    }
}
