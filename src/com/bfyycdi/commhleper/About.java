package com.bfyycdi.commhleper;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class About extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView aboutText=(TextView)findViewById(R.id.aboutText);
        TextView websiteText=(TextView)findViewById(R.id.websiteText);
        TextView weibo=(TextView)findViewById(R.id.weibo);
        TextView Email=(TextView)findViewById(R.id.Email);
        TextView space=(TextView)findViewById(R.id.space);
        aboutText.setBackgroundResource(R.color.background);
        websiteText.setBackgroundResource(R.color.background);
        weibo.setBackgroundResource(R.color.background);
        Email.setBackgroundResource(R.color.background);
        space.setBackgroundResource(R.color.background);
    }
}
