package com.locationhud.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.locationhud.ui.buttons.PressedColourChangeViewTouchListener;

/**
 * Created by Mark on 30/10/2014.
 */
public class UiUtility {
    public static void setOnTouchColourChanges(final View v, final int colourOnUp, final int colourOnDown) {
        v.setOnTouchListener(new PressedColourChangeViewTouchListener(v, colourOnUp, colourOnDown));
    }
    public static View.OnTouchListener getOnTouchListenerColourChanges(final View v, final int colourOnUp, final int colourOnDown) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        v.setBackgroundResource(colourOnUp);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundResource(colourOnDown);
                        break;
                }
                return false;
            }
        };
    }

    public static void setOnTouchImageChanges(final ImageButton button, final int imageOnUp, final int imageOnDown) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        button.setImageResource(imageOnUp);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        button.setImageResource(imageOnDown);
                        break;
                }
                return false;
            }
        });
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    public static Point getScreenSize(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        return new Point(width, height);
    }
}
