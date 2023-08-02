package com.example.youtube;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountActivity extends AppCompatActivity {

    Toolbar toolbar;
    CircleImageView circleImageView;
    TextView username, email;
    TextView txtchannel, txthelp, txtsetting;

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    String strProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // connect to xml with id's
        circleImageView = findViewById(R.id.profileimg);
        username = findViewById(R.id.user_name);
        email = findViewById(R.id.user_email);
        txtchannel = findViewById(R.id.channel);
        txthelp = findViewById(R.id.help);
        txtsetting = findViewById(R.id.setting);
        toolbar = findViewById(R.id.toolBar);

        // Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        // setup toolbar
        toolBarFunc();

        // getdata from Firebase
        getDataFunc();

        txtchannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUserAlreadyHaveChannel();
            }
        });

    }

    private void checkUserAlreadyHaveChannel() {
        reference.child("Channels").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                    intent.putExtra("type", "channel");
                    startActivity(intent);
                }
                else{
                    Dialog dialog = new Dialog(AccountActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.channel_dialogue);
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);

                    // take edittext input
                    EditText channelname = dialog.findViewById(R.id.channel_name);
                    EditText channeldescription = dialog.findViewById(R.id.channel_description);
                    TextView createchannel = dialog.findViewById(R.id.create_channel);

                    createchannel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String chName = channelname.getText().toString();
                            String chDescription = channeldescription.getText().toString();

                            if (chName.isEmpty() || chDescription.isEmpty()){
                                Toast.makeText(AccountActivity.this, "Enter required fields", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                ProgressDialog progressDialog = new ProgressDialog(AccountActivity.this);
                                progressDialog.setTitle("New Channel");
                                progressDialog.setMessage("Creating...");
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.show();

                                // take date when creating channel
                                String date = DateFormat.getDateInstance().format(new Date());

                                HashMap<String, Object> mapData = new HashMap<>();
                                mapData.put("ChannelName", chName);
                                mapData.put("Description", chDescription);
                                mapData.put("Date joined", date);
                                mapData.put("Channel Logo", strProfile);

                                reference.child("Channels").child(user.getUid()).setValue(mapData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            progressDialog.dismiss();
                                            dialog.dismiss();

                                        }
                                        else{
                                            progressDialog.dismiss();
                                            dialog.dismiss();
                                            Toast.makeText(AccountActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        }
                    });

                    dialog.show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AccountActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDataFunc() {
        reference.child("Google Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String strUsername = snapshot.child("username").getValue().toString();
                    String strEmail = snapshot.child("email").getValue().toString();
                    strProfile = snapshot.child("profile").getValue().toString();

                    username.setText(strUsername);
                    email.setText(strEmail);

                    try{
                        Picasso.get().load(strProfile).placeholder(R.drawable.account).into(circleImageView);
                    }
                    catch (Exception exception){
                        exception.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void toolBarFunc(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}