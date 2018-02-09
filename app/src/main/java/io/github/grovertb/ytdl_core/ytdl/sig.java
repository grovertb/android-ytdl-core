package io.github.grovertb.ytdl_core.ytdl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

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
        String mRegev = "var ([a-zA-Z_\\$][a-zA-Z_0-9]*)=\\{((?:(?:(?:[a-zA-Z_\\$][a-zA-Z_0-9]*|(?:'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'|\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\")):function\\(a\\)\\{(?:return )?a\\.reverse\\(\\)\\}|(?:[a-zA-Z_\\$][a-zA-Z_0-9]*|(?:'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'|\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\")):function\\(a,b\\)\\{return a\\.slice\\(b\\)\\}|(?:[a-zA-Z_\\$][a-zA-Z_0-9]*|(?:'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'|\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\")):function\\(a,b\\)\\{a\\.splice\\(0,b\\)\\}|(?:[a-zA-Z_\\$][a-zA-Z_0-9]*|(?:'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'|\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\")):function\\(a,b\\)\\{var c=a\\[0\\];a\\[0\\]=a\\[b(?:%a\\.length)?\\];a\\[b(?:%a\\.length)?\\]=c(?:;return a)?\\}),?\\r?\\n?)+)\\};";
        String mRegex = "var ([a-zA-Z_\\$][a-zA-Z_0-9]*)=\\{((?:(?:(?:[a-zA-Z_\\$][a-zA-Z_0-9]*|(?:'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'|\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\")):function\\(a\\)\\{(?:return )?a\\.reverse\\(\\)\\}|(?:[a-zA-Z_\\$][a-zA-Z_0-9]*|(?:'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'|\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\")):function\\(a,b\\)\\{return a\\.slice\\(b\\)\\}|(?:[a-zA-Z_\\$][a-zA-Z_0-9]*|(?:'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'|\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\")):function\\(a,b\\)\\{a\\.splice\\(0,b\\)\\}|(?:[a-zA-Z_\\$][a-zA-Z_0-9]*|(?:'[^'\\\\]*(:?\\\\[\\s\\S][^'\\\\]*)*'|\"[^\"\\\\]*(:?\\\\[\\s\\S][^\"\\\\]*)*\")):function\\(a,b\\)\\{var c=a\\[0\\];a\\[0\\]=a\\[b(?:%a\\.length)?\\];a\\[b(?:%a\\.length)?\\]=c(?:;return a)?\\}),?\\r?\\n?)+)\\};";

        try{
            javax.script.ScriptEngineManager se = new javax.script.ScriptEngineManager();
            javax.script.ScriptEngine engine = se.getEngineByName("js");

            System.out.println(body);
            engine.put("str", body);
            engine.put("rgx", mRegev);
//            engine.eval("var rgx="+mRegev);
            Object value = engine.eval("function validate(r, s){ println(new RegExp(r)); return new RegExp(r).exec(s);};validate(rgx, str);");
            System.out.println(value);
        }catch (Exception e) {
            e.printStackTrace();
        }


//        Matcher objResult = Pattern.compile(mRegex).matcher(body);
//        if (objResult.find()) {
//            System.out.println(objResult.groupCount());
//            System.out.println("group(o): " + objResult.group(0));
//            System.out.println("group(1): " + objResult.group(1));
//            System.out.println("group(2): " + objResult.group(2));
//            System.out.println("group(3): " + objResult.group(3));
//            System.out.println("group(4): " + objResult.group(4));
//            System.out.println("group(5): " + objResult.group(5));
//
//        } else {
//            System.out.println("false");
//        }
    }
}
