package com.example.photoeditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity2 extends AppCompatActivity {

    private ImageView imageView;
    private Button button,buttonSave;
    private Bitmap bitmap;
    private int width = 0;
    private int height = 0;
    private static final int MAX_PIXEL_COUNT = 2048;
    private int[] pixels;
    private int pixelCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Intent intent = getIntent();
        final String path = intent.getStringExtra("path");

        final Uri uri = Uri.fromFile(new File(path));

        imageView = findViewById(R.id.imageCrop);
        button = findViewById(R.id.colorBtn);
        buttonSave = findViewById(R.id.SaveBtn);
/*
        Glide.with(MainActivity2.this)
                .load(path)
                .placeholder(R.drawable.ic_launcher_background).centerCrop()
                .into(imageView);

 */
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               final File outFile = createImageFile();
               try (FileOutputStream out = new FileOutputStream(outFile)){
                   bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
                   Uri imageUri = Uri.parse("file://"+outFile.getAbsolutePath());
                   sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,imageUri));
                   Toast.makeText(MainActivity2.this,"Your image was saved !",Toast.LENGTH_SHORT).show();
               } catch (FileNotFoundException e) {
                   e.printStackTrace();
               } catch (IOException e) {
                   e.printStackTrace();
               }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        for (int i =0;i<pixelCount;i++){
                           pixels[i] /=2;
                        }
                        bitmap.setPixels(pixels,0,width,0,0,width,height);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
            }
        });

        new Thread(){
            public void run(){
                bitmap = null;
                final BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
                bmpOptions.inBitmap = bitmap;
                bmpOptions.inJustDecodeBounds = true;

                try(InputStream input = getContentResolver().openInputStream(uri)) {
                    bitmap = BitmapFactory.decodeStream(input,null,bmpOptions);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                bmpOptions.inJustDecodeBounds = false;
                width = bmpOptions.outWidth;
                height = bmpOptions.outHeight;

                int resizeScale = 1;

                if(width > MAX_PIXEL_COUNT){
                    resizeScale = width/MAX_PIXEL_COUNT;
                }
                else if(height > MAX_PIXEL_COUNT){
                    resizeScale = width/MAX_PIXEL_COUNT;
                }

                if(width/resizeScale > MAX_PIXEL_COUNT || height/resizeScale > MAX_PIXEL_COUNT){
                    resizeScale++;
                }

                bmpOptions.inSampleSize = resizeScale;
                InputStream input = null;

                try {
                    input = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    recreate();
                    return;
                }

                bitmap = BitmapFactory.decodeStream(input,null,bmpOptions);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });

                width = bitmap.getWidth();
                height = bitmap.getHeight();

                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);

                pixelCount = width*height;
                pixels = new int[pixelCount];

                bitmap.getPixels(pixels,0,width,0,0,width,height);
            }
        }.start();

    }

    private File createImageFile(){
        final String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String name = "/JPEG "+time+".jpg";
        final File storage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        return  new File(storage + name);
    }

}