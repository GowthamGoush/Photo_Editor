package com.example.photoeditor;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    private GridView imageGallery;
    private ArrayList<String> images;
    private int REQUEST_PERMISSIONS = 2131;
    private String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private int PERMISSIONS_COUNTS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageGallery = findViewById(R.id.imageGallery);

        imageGallery.setAdapter(new ImageAdapter(this));

        imageGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {

                if (null != images && !images.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                    intent.putExtra("path", images.get(position));
                    startActivity(intent);
                    Toast.makeText(
                            getApplicationContext(),
                            "position " + position + " " + images.get(position),
                            Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private class ImageAdapter extends BaseAdapter {

        private Activity context;

        public ImageAdapter(Activity localContext) {
            context = localContext;
            images = getAllShownImagesPath(context);
        }

        public int getCount() {
            return images.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ImageView picturesView;
            if (convertView == null) {
                picturesView = new ImageView(context);
                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picturesView
                        .setLayoutParams(new GridView.LayoutParams(270, 270));

            } else {
                picturesView = (ImageView) convertView;
            }

            Glide.with(context).load(images.get(position))
                    .placeholder(R.drawable.ic_launcher_background).centerCrop()
                    .into(picturesView);

            return picturesView;
        }

        private ArrayList<String> getAllShownImagesPath(Activity activity) {
            Uri uri;
            Cursor cursor;
            int column_index_data, column_index_folder_name;
            ArrayList<String> listOfAllImages = new ArrayList<String>();
            String absolutePathOfImage = null;
            uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

            cursor = activity.getContentResolver().query(uri, projection, null,
                    null, null);

            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            column_index_folder_name = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(column_index_data);

                listOfAllImages.add(absolutePathOfImage);
            }
            return listOfAllImages;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private  boolean notPermissions(){
        for (int i=0;i<PERMISSIONS_COUNTS;i++){
            if(checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        if (notPermissions()){
            requestPermissions(PERMISSIONS,REQUEST_PERMISSIONS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSIONS && grantResults.length >0){
            if(notPermissions()){
                finishAffinity();
            }
        }
    }
}