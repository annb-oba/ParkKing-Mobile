package com.example.afbu.parkking;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class EditAccount extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 0, REQUEST_CAMERA = 1;

    private ImageButton btnBackHome, btnEditImage;
    private ImageView imgUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        initResources();
        initEvents();
    }


    private void initEvents() {
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),
                 //       "lol", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadOptions();
            }
        });
    }

    private void uploadOptions(){
        final CharSequence[] items  = {"Take photo", "Choose existing photo"};

        AlertDialog.Builder builder = new AlertDialog.Builder(EditAccount.this);
        builder.setTitle("Change Profile Picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(items[which].equals("Take photo")){

                    Intent gotoCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (gotoCamera.resolveActivity(getPackageManager()) != null){
                        startActivityForResult(gotoCamera, REQUEST_CAMERA);
                    }
                }else if(items[which].equals("Choose existing photo")){
                    Intent gotoGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(gotoGallery, RESULT_LOAD_IMAGE);
                }
            }
        });

        builder.show();
    }

    private void initResources() {
        btnBackHome = (ImageButton) findViewById(R.id.EditAccount_btnBack);
        btnEditImage = (ImageButton) findViewById(R.id.EditAccount_btnChangeImg);
        imgUser = (ImageView) findViewById(R.id.EditAccount_imgUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null){
            if(requestCode == RESULT_LOAD_IMAGE){
                Uri selectedImage = data.getData();
                imgUser.setImageURI(selectedImage);
            }else if(requestCode == REQUEST_CAMERA){
                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                imgUser.setImageBitmap(bmp);
            }
        }
    }
}
