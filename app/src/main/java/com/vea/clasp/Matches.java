package com.vea.clasp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.vea.safecatcher.rt.Match;

public class Matches {
    @Match(
        owner = "java/lang/Integer",
        name = "parseInt",
        desc = "(Ljava/lang/String;)I")
    public static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Log.e("catch", "parseInt : " + s, e);
        }
        return 0;
    }

    @Match(
        owner = "android/widget/Toast",
        name = "makeText",
        desc = "(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast")
    public static Toast makeText(Context context, CharSequence msg, int duration) {
        String s = msg.toString();
        s += "ok";
        return Toast.makeText(context, s, duration);
    }
}