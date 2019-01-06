package com.carefeed.android.carefeed;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class FullScreenActivity extends AppCompatActivity {

    private Toolbar transparentToolbar;
    private ImageView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_full_screen);

        transparentToolbar = (Toolbar) findViewById(R.id.full_screen_toolbar);
        fullScreenImageView = (ImageView) findViewById(R.id.full_screen_imageView);

        Picasso.get().load(getIntent().getExtras().getString("postImage")).into(fullScreenImageView);

        setSupportActionBar(transparentToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
    }
}
