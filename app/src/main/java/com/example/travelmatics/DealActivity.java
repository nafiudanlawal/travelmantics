package com.example.travelmatics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private static final int PICTURE_RESULT = 69;
    private static final int DEVICE_WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
    EditText etTitle;
    EditText etPrice;
    EditText etDescription;
    ImageView imageView;
    Button btnImage;
    TravelDeal deal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        etTitle = (EditText) findViewById(R.id.etTitle);
        etPrice = (EditText) findViewById(R.id.etPrice);
        etDescription = (EditText) findViewById(R.id.etDescription);
        imageView = (ImageView) findViewById(R.id.iv_tour);
        btnImage = (Button) findViewById(R.id.btnImage);
        final Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getParcelableExtra("Deal");
        if(deal == null) {
            this.deal = new TravelDeal();
        }
        this.deal = deal;
        etTitle.setText(deal.getTitle());
        etPrice.setText(deal.getPrice());
        etDescription.setText(deal.getDescription());
        if(!deal.getImageURL().isEmpty() && deal.getImageURL() != null){
            Picasso.get()
                    .load(deal.getImageURL())
                    .resize(DEVICE_WIDTH, DEVICE_WIDTH * 2/3)
                    .centerCrop()
                    .into(imageView);
        }
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/jpeg");
                intent1.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent1, "Insert Picture"), PICTURE_RESULT);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_RESULT && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            final StorageReference ref =  FirebaseUtil.mStorageReference.child(imageUri.getLastPathSegment());
            UploadTask uploadTask = ref.putFile(imageUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String url = downloadUri.toString();
                        String imageName = ref.getName();
                        Log.d("Nafiu", "imageName = " + imageName);
                        Log.d("Nafiu", "downloadUrl = "+ url);
                        deal.setImageURL(url);
                        deal.setImageName(imageName);
                        Picasso.get()
                                .load(deal.getImageURL())
                                .resize(DEVICE_WIDTH, DEVICE_WIDTH * 2/3)
                                .centerCrop()
                                .into(imageView);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Travel Deal Saved", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            case R.id.delete_deal:
                deleteDeal();
                Toast.makeText(this, "Travel Deal Deleted", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            case R.id.logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>(){
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Logout", "User Logged Out");
                                FirebaseUtil.attachListener();
                            }
                        });
                FirebaseUtil.detachListener();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    private void saveDeal() {
        deal.setTitle(etTitle.getText().toString());
        deal.setPrice(etPrice.getText().toString());
        deal.setDescription(etDescription.getText().toString());

        if(deal.getId() == null){
            mDatabaseReference.push().setValue(deal);
        }else{
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }
    }

    private void deleteDeal(){
        if(deal != null){
            mDatabaseReference.child(deal.getId()).removeValue();
            if(deal.getImageName() != null && !deal.getImageName().isEmpty()){
                StorageReference storageReference = FirebaseUtil.mStorageReference.child(deal.getImageName());
                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Delete Deal", "File Deleted Successfully  - file: " + deal.getImageName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Delete Deal", "File Deletion Failed  - file: " + deal.getImageName());
                    }
                });
            }
        }
        else {
            Toast.makeText(this, "Please save deal before deleting", Toast.LENGTH_LONG).show();
        }
        return;
    }
    private void backToList(){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_menu, menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.save_menu).setVisible(true);
            menu.findItem(R.id.delete_deal).setVisible(true);
            enableText(true);
        }
        else {
            menu.findItem(R.id.save_menu).setVisible(false);
            menu.findItem(R.id.delete_deal).setVisible(false);
            enableText(false);
        }
        return true;
    }
    private void enableText(boolean isEnabled){
        etTitle.setEnabled(isEnabled);
        etDescription.setEnabled(isEnabled);
        etPrice.setEnabled(isEnabled);
        btnImage.setVisibility( isEnabled ? View.VISIBLE: View.INVISIBLE );
    }
}