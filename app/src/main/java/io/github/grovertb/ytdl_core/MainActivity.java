package io.github.grovertb.ytdl_core;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.json.JSONObject;

import io.github.grovertb.ytdl_core.ytdl.info;

public class MainActivity extends AppCompatActivity {
    Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        findViewById(R.id.btnGetVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new info(mActivity).getDocVideo("https://www.youtube.com/watch?v=qkkG6g6vT34&hl=en", new info.GetDataCallback() {
                            @Override
                            public void onSuccess(JSONObject mDataStream) {
                                System.out.println(mDataStream);
                            }
                        });
                    }
                }).start();
            }
        });
    }
}
