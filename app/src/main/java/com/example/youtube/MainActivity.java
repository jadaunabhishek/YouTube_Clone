package com.example.youtube;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youtube.Fragment.fragmentAddVideo;
import com.example.youtube.Fragment.fragmentExplore;
import com.example.youtube.Fragment.fragmentHome;
import com.example.youtube.Fragment.fragmentLib;
import com.example.youtube.Fragment.fragmentSubs;
import com.example.youtube.FragmentChannel.dashboardFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    Fragment fragment;
    AppBarLayout appBarLayout;

    BottomNavigationView bottom;
    Toolbar toolbar;
    FrameLayout frameLayout;
    ImageView userprofile;
    // Firebase Log In
    FirebaseAuth auth;
    FirebaseUser user;
    // Google Log In
    GoogleSignInClient googleSignInUser;
    GoogleSignInOptions gsc;
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // connect to xml using id's
        appBarLayout = findViewById(R.id.appBar);
        toolbar = findViewById(R.id.toolBar);
        bottom = findViewById(R.id.bottomnavigationview);
        frameLayout = findViewById(R.id.framelayout);
        userprofile = findViewById(R.id.profileimg);

        // firebase user auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        //google SignIn
        gsc = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        googleSignInUser = GoogleSignIn.getClient(MainActivity.this, gsc);

        showFragment();


        // when clicked on user profile, user can login or if already logged in then can check details
        userprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user != null){
                    startActivity(new Intent(MainActivity.this, AccountActivity.class));
                    fetchProfileImage();
                }
                else{
                    userprofile.setImageResource(R.drawable.account);
//                    showDialog();
                    signIn();
                }
            }
        });

        // setting toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // bottom navigation view
        bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                if (id==R.id.home){
                    loadFragment(new fragmentHome(),true);
                }
                else if (id==R.id.explore) {
                    loadFragment(new fragmentExplore(),false);
                }
                else if (id==R.id.addvideo) {
                    loadFragment(new fragmentAddVideo(),false);

                }
                else if (id==R.id.subscriptions) {
                    loadFragment(new fragmentSubs(),false);

                }
                else if (id==R.id.library) {
                    loadFragment(new fragmentLib(),false);

                }

                return true;
            }
        });
        bottom.setSelectedItemId(R.id.home);

    }

    private void signIn() {
        Intent intent = googleSignInUser.getSignInIntent();
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task= GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential= GoogleAuthProvider.getCredential(account.getIdToken(),null);
                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //Store in firebase
                            FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("username",account.getDisplayName());
                            map.put("email",account.getEmail());
                            map.put("profile",String.valueOf(account.getPhotoUrl()));
                            map.put("uid",firebaseUser.getUid());
                            map.put("Search",account.getDisplayName().toLowerCase());

                            DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Google Users");
                            reference.child(firebaseUser.getUid()).setValue(map);
                        }
                        else {
                            Toast.makeText(MainActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            catch (Exception e){
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void loadFragment(Fragment fragment, boolean flag){
        statusBarColor("#ffffff");
        appBarLayout.setVisibility(View.VISIBLE);
        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction ft= fm.beginTransaction();
        if (flag)
            ft.add(R.id.framelayout,fragment );
        else
            ft.replace(R.id.framelayout,fragment);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.notification){
            Toast.makeText(this, "Notification", Toast.LENGTH_SHORT).show();
        }

        if (id==R.id.search){
            Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    //to fetch profile image
    private void fetchProfileImage(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Google Users");
        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String p = snapshot.child("profile").getValue().toString();

                    //picasso dependency for circular image
                    Picasso.get().load(p).placeholder(R.drawable.account)
                            .into(userprofile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showFragment(){
        String type = getIntent().getStringExtra("type");
        if(type!=null){
            switch (type) {
                case "channel":
                    statusBarColor("#CEFF0000");
                    appBarLayout.setVisibility(View.GONE);
                    fragment = dashboardFragment.newInstance();
                    break;
            }
            if(fragment!=null){
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.framelayout, fragment).commit();
            }
        }
    }
    private void statusBarColor(String color){
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor(color));
    }
}