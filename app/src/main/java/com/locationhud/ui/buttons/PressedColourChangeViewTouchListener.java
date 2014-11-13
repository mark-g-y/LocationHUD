package com.locationhud.ui.buttons;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Mark on 13/11/2014.
 */
public class PressedColourChangeViewTouchListener implements View.OnTouchListener {

    private View v;
    private int colourOnUp;
    private int colourOnDown;

    public PressedColourChangeViewTouchListener(View v, int colourOnUp, int colourOnDown) {
        this.v = v;
        this.colourOnUp = colourOnUp;
        this.colourOnDown = colourOnDown;
    }

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
}
