package io.github.grovertb.ytdl_core.ytdl;

import android.app.Activity;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import io.github.grovertb.ytdl_core.Constant;

/**
 * Created by grove on 8/02/2018.
 */

public class sig {

    private Context context;
    private RequestQueue queue;


    private String jsVarStr = "[a-zA-Z_\\$][a-zA-Z_0-9]*";
    private String jsSingleQuoteStr = "'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'";
    private String jsDoubleQuoteStr = "\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\"";
    private String jsQuoteStr = "(?:" + jsSingleQuoteStr + "|" + jsDoubleQuoteStr + ")";
    private String jsKeyStr = "(?:" + jsVarStr + "|" + jsQuoteStr + ")";

    private String jsPropStr = "(?:\\." + jsVarStr + "|\\[" + jsQuoteStr + "\\])";
    private String jsEmptyStr = "(?:''|\"\")";
    private String reverseStr = ":function\\(a\\)\\{(?:return )?a\\.reverse\\(\\)\\}";
    private String sliceStr = ":function\\(a,b\\)\\{return a\\.slice\\(b\\)\\}";
    private String spliceStr = ":function\\(a,b\\)\\{a\\.splice\\(0,b\\)\\}";
    private String swapStr = ":function\\(a,b\\)\\{var c=a\\[0\\];a\\[0\\]=a\\[b(?:%a\\.length)?\\];a\\[b(?:%a\\.length)?\\]=c(?:;return a)?\\}";

    private String actionsObjRegexp = "var (" + jsVarStr + ")=\\{((?:(?:" +
            jsKeyStr + reverseStr + '|' +
            jsKeyStr + sliceStr + '|' +
            jsKeyStr + spliceStr + '|' +
            jsKeyStr + swapStr +
            "),?\\r?\\n?)+)\\};";

    private String actionsFuncRegexp = "function(?: " + jsVarStr + ")?\\(a\\)\\{" +
            "a=a\\.split\\(" + jsEmptyStr + "\\);\\s*" +
            "((?:(?:a=)?" + jsVarStr +
            jsPropStr +
            "\\(a,\\d+\\);)+)" +
            "return a\\.join\\(" + jsEmptyStr + "\\)\\}";

    private String reverseRegexp = "(?:^|,)(" + jsKeyStr + ")" + reverseStr;
    private String sliceRegexp = "(?:^|,)(" + jsKeyStr + ")" + sliceStr;
    private String spliceRegexp = "(?:^|,)(" + jsKeyStr + ")" + spliceStr;
    private String swapRegexp = "(?:^|,)(" + jsKeyStr + ")" + swapStr;


    private static String  reverseKey = "",
                    sliceKey = "",
                    spliceKey = "",
                    swapKey = "";


    public sig(Activity mActivity) {
        this.context = mActivity.getApplicationContext();
        this.queue = Volley.newRequestQueue(mActivity);
    }

    public void getTokens(String html5playerfile) {
        try {
            StringRequest strReq = new StringRequest(Constant.YOUTUBE_URL + html5playerfile, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        File path = context.getExternalFilesDir(null);
                        File file = new File(path, "config2.txt");
                        FileOutputStream stream = new FileOutputStream(file);
                        stream.write(response.getBytes());
                        stream.close();
                        ArrayList<String> mTokens = extractActions(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("YTDL", "Error: " + error.getMessage());
                }
            });
            queue.add(strReq);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> extractActions(String body) {
        ArrayList<String> tokens = new ArrayList<>();

        Matcher objResult = Pattern.compile(actionsObjRegexp).matcher(body);
        String obj = "", objBody = "", funcBody = "";
        if (objResult.find()) {
            obj = objResult.group(1).replaceAll("\\$", "\\\\$");
            objBody = objResult.group(2).replaceAll("\\$", "\\\\$");
        }

        Matcher funcResult = Pattern.compile(actionsFuncRegexp).matcher(body);

        if (funcResult.find()) {
            funcBody = funcResult.group(1).replaceAll("\\$", "\\\\$");
        }

        if (obj.equals("") & objBody.equals("") && funcBody.equals("")) {
            return tokens;
        } else {

            Matcher mResultReverse = Pattern.compile(reverseRegexp, Pattern.MULTILINE).matcher(objBody);
            if (mResultReverse.find()) {
                reverseKey = mResultReverse.group(1).replaceAll("\\$", "\\\\$").replaceAll("\\$|^'|^\"|'$|\"$", "");
            }

            Matcher mResultSlice = Pattern.compile(sliceRegexp, Pattern.MULTILINE).matcher(objBody);
            if (mResultSlice.find()) {
                sliceKey = mResultSlice.group(1).replaceAll("\\$", "\\\\$").replaceAll("\\$|^'|^\"|'$|\"$", "");
            }

            Matcher mResultSplice = Pattern.compile(spliceRegexp, Pattern.MULTILINE).matcher(objBody);
            if (mResultSplice.find()) {
                spliceKey = mResultSplice.group(1).replaceAll("\\$", "\\\\$").replaceAll("\\$|^'|^\"|'$|\"$", "");
            }

            Matcher mResultSwap = Pattern.compile(swapRegexp, Pattern.MULTILINE).matcher(objBody);
            if (mResultSwap.find()) {
                swapKey = mResultSwap.group(1).replaceAll("\\$", "\\\\$").replaceAll("\\$|^'|^\"|'$|\"$", "");
            }

            String keys = "(" + reverseKey + "|" + sliceKey + "|" + spliceKey + "|" + swapKey + ")";
            String myreg = "(?:a=)?" + obj + "(?:\\." + keys + "|\\['" + keys + "'\\]|\\[\"" + keys + "\"\\])\\(a,(\\d+)\\)";

            ArrayList<String> parts = new ArrayList<String>();
            parts.addAll(new ArrayList<>(Arrays.asList(funcBody.split(";"))));

            for ( int i = 0; i < parts.size(); i++) {
                Matcher mResultTokenize = Pattern.compile(myreg).matcher(parts.get(i));
                if(mResultTokenize.find()) {
                    String key = null;
                    if(mResultTokenize.group(1) != null) {
                        key = mResultTokenize.group(1);
                    }else if(mResultTokenize.group(2) != null) {
                        key = mResultTokenize.group(2);
                    }else if(mResultTokenize.group(3) != null) {
                        key = mResultTokenize.group(3);
                    }

                    if(key != null) {
                        if(key.equals(swapKey)) {
                            tokens.add('w' + mResultTokenize.group(4));
                        }else if(key.equals(reverseKey)) {
                            tokens.add("r");
                        }else if(key.equals(sliceKey)) {
                            tokens.add("s"+ mResultTokenize.group(4));
                        }else if(key.equals(spliceKey)) {
                            tokens.add("p" + mResultTokenize.group(4));
                        }
                    }
                }
            }

            return tokens;
        }
    }

}
