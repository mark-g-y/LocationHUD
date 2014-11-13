package com.locationhud.ui.buttons;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import com.locationhud.R;
import com.locationhud.ui.UiUtility;

/**
 * Created by Mark on 06/11/2014.
 */
public class CustomButton extends TextView {

    public CustomButton(Context context) {
        super(context);
        init(context);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomButton(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        UiUtility.setOnTouchColourChanges(this, android.R.color.transparent, R.color.item_pressed_translucent);
    }
}
