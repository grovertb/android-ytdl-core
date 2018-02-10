package io.github.grovertb.ytdl_core;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;


import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;

import io.github.grovertb.ytdl_core.ytdl.sig;

public class MainActivity extends AppCompatActivity {
    String VIDEO_URL = "https://www.youtube.com/watch?v=";
    String url = "https://www.youtube.com/watch?v=qkkG6g6vT34&hl=en";

    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;

        findViewById(R.id.btnGetVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDocVideo();
            }
        });
    }

    private void getDocVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(url).get();

                    Iterator mElement = doc.select("script").iterator();

                    while (mElement.hasNext()) {
                        Matcher m;

                        String html = ((Element) mElement.next()).html();

                        String jsonStr = utils.between(html, "ytplayer.config = ", ";ytplayer.load");

                        if (!jsonStr.equals("")) {
                            JSONObject mDocument = new JSONObject(jsonStr);
                            JSONObject mDataVideo = mDocument.getJSONObject("args");
                            String urlJS = mDocument.getJSONObject("assets").getString("js");
                            String mTtitle = mDataVideo.getString("title");
                            String encodedStream = mDataVideo.getString("url_encoded_fmt_stream_map");
                            String adaptiveFmts = mDataVideo.getString("adaptive_fmts");


                            ArrayList<String> streams = new ArrayList<String>();
                            streams.addAll(new ArrayList<>(Arrays.asList(encodedStream.split(","))));
                            streams.addAll(new ArrayList<>(Arrays.asList(adaptiveFmts.split(","))));

                            JSONArray mStreamsData = new JSONArray();
                            for (int i = 0; i < streams.size(); i++) {
                                Uri uri = Uri.parse("http://localhost?" + streams.get(i));
                                JSONObject mStreamData = new JSONObject();
                                for (String name : uri.getQueryParameterNames()) {
                                    for (String value : uri.getQueryParameters(name)) {
                                        try {
                                            mStreamData.put(name, value);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                mStreamsData.put(mStreamData);
                            }

                            // signature
                            new sig(mActivity).getTokens(urlJS);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    private String getAuthor(String body) {
        System.out.println(body);
        String ownerinfo = utils.between(body, "<div id=\"watch7-user-header\" class=\" spf-link \">", "<div id=\"watch8-action-buttons\" class=\"watch-action-buttons clearfix\">");
        if (ownerinfo.equals("")) {
            return "";
        }
        return ownerinfo;
    }
}
