package io.github.grovertb.ytdl_core.ytdl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.grovertb.ytdl_core.Constant;

/**
 * Created by grove on 8/02/2018.
 */

public class sig {

    private String jsVarStr = "[a-zA-Z_\\$][a-zA-Z_0-9]*";
    private String jsSingleQuoteStr = "'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'";
    private String jsDoubleQuoteStr = "\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\"";
    private String jsQuoteStr = "(?:" + jsSingleQuoteStr + "|" + jsDoubleQuoteStr + ")";
    private String jsKeyStr = "(?:" + jsVarStr + "|" + jsQuoteStr + ")";

    private String jsPropStr = "(?:\\\\." + jsVarStr + "|\\\\[" + jsQuoteStr + "\\\\])";
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

    public sig() {

    }

    public ArrayList getTokens(String html5playerfile) {
        ArrayList<String> mTokens = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(Constant.YOUTUBE_URL + html5playerfile).get();
            extractActions(doc.text());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mTokens;
    }

    private void extractActions(String body) {
        String mRegex = "var ([a-zA-Z_\\$][a-zA-Z_0-9]*)=\\{((?:(?:(?:[a-zA-Z_\\$][a-zA-Z_0-9]*|(?:'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'|\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\")):function\\(a\\)\\{(?:return )?a\\.reverse\\(\\)\\}|(?:[a-zA-Z_\\$][a-zA-Z_0-9]*|(?:'[^'\\\\]*(:?\\[\\s\\S][^'\\\\]*)*'|\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\")):function\\(a,b\\)\\{return a\\.slice\\(b\\)\\}|(?:[a-zA-Z_\\$][a-zA-Z_0-9]*|(?:'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'|\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\")):function\\(a,b\\)\\{a\\.splice\\(0,b\\)\\}|(?:[a-zA-Z_\\$][a-zA-Z_0-9]*|(?:'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'|\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\")):function\\(a,b\\)\\{var c=a\\[0\\];a\\[0\\]=a\\[b(?:%a\\.length)?\\];a\\[b(?:%a\\.length)?\\]=c(?:;return a)?\\}),?\\r?\\n?)+)\\};";

        System.out.println("mRegex: " + mRegex);

        Matcher objResult = Pattern.compile(mRegex).matcher(body);

        if (objResult.find()) {
            System.out.println(objResult.groupCount());
        } else {
            System.out.println("false");
        }
    }
}
