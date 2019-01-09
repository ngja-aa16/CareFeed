package com.carefeed.android.carefeed;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class FullScreenActivity extends AppCompatActivity {

    private PhotoView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_full_screen);

        fullScreenImageView = (PhotoView) findViewById(R.id.full_screen_imageView);

        Picasso.get().load(getIntent().getExtras().getString("postImage")).into(fullScreenImageView);

        fullScreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
