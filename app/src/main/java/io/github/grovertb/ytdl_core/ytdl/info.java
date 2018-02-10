package io.github.grovertb.ytdl_core.ytdl;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by grove on 9/02/2018.
 */

public class info {

    private Context context;

    public info(Activity mActivity) {
        this.context = mActivity;
    }

    public void getDocVideo(final String url, final GetDataCallback getDataCallback) {
        try {
            Document doc = Jsoup.connect(url).get();
            Iterator mElement = doc.select("script").iterator();
            while (mElement.hasNext()) {
                String html = ((Element) mElement.next()).html();
                String jsonStr = utils.between(html, "ytplayer.config = ", ";ytplayer.load");
                if (!jsonStr.equals("")) {
                    JSONObject mDocument = new JSONObject(jsonStr);
                    JSONObject mDataVideo = mDocument.getJSONObject("args");

                    String urlJS = mDocument.getJSONObject("assets").getString("js");
                    final String mTtitle = mDataVideo.getString("title");
                    String encodedStream = mDataVideo.getString("url_encoded_fmt_stream_map");
                    String adaptiveFmts = mDataVideo.getString("adaptive_fmts");

                    ArrayList<String> streams = new ArrayList<String>();
                    streams.addAll(new ArrayList<>(Arrays.asList(encodedStream.split(","))));
                    streams.addAll(new ArrayList<>(Arrays.asList(adaptiveFmts.split(","))));

                    final JSONArray mStreamsData = new JSONArray();
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

                    sig msig = new sig(context);
                    msig.getTokens(urlJS, new sig.GetTokenCallback() {
                        @Override
                        public void onSuccess(ArrayList<String> mtokens) {
                            try {
                                for (int i = 0; i < mStreamsData.length(); i++) {
                                    JSONObject mstream = mStreamsData.getJSONObject(i);
                                    String sig = decipher(mtokens, mStreamsData.getJSONObject(i).getString("s"));
                                    mstream.put("url", mstream.getString("url") + "&signature=" + sig + "&ratebypass=true");
                                }

                                JSONObject mDataResponse = new JSONObject();

                                mDataResponse.put("title", mTtitle);
                                mDataResponse.put("streams", mStreamsData);
                                getDataCallback.onSuccess(mDataResponse);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String decipher(ArrayList<String> tokens, String sig) {
        for (int i = 0, len = tokens.size(); i < len; i++) {
            String token = tokens.get(i);
            Integer pos;
            switch (String.valueOf(token.charAt(0))) {
                case "r":
                    sig = new StringBuilder(sig).reverse().toString();
                    break;
                case "w":
                    pos = Integer.parseInt(token.substring(1));
                    sig = swapHeadAndPosition(sig, pos);
                    break;
                case "s":
                    pos = Integer.parseInt(token.substring(1));
                    sig = sig.substring(pos);
                    break;
                case "p":
                    pos = Integer.parseInt(token.substring(1));
                    sig = sig.substring(0, pos);
                    break;
            }
        }

        return sig;
    }

    private String swapHeadAndPosition(String arr, Integer position) {
        StringBuilder myName = new StringBuilder(arr);
        myName.setCharAt(0, arr.charAt(position % arr.length()));
        myName.setCharAt(position, arr.charAt(0));
        return String.valueOf(myName);
    }

    public interface GetDataCallback{
        void onSuccess(JSONObject mDataStream);
    }
}
