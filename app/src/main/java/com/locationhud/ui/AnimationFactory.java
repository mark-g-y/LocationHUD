package com.locationhud.ui;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * Created by Mark on 30/10/2014.
 */
public class AnimationFactory {

    public static TranslateAnimation buildTranslateAnimation(final View view, final int amountToMoveRight, final int amountToMoveDown, int duration) {

        TranslateAnimation anim = new TranslateAnimation(0, 0, amountToMoveRight, amountToMoveDown);
        anim.setDuration(duration);
        anim.setAnimationListener(buildTranslateAnimationListener(view, amountToMoveRight, amountToMoveDown));

        return anim;
    }

    public static TranslateAnimation.AnimationListener buildTranslateAnimationListener(final View view, final int amountToMoveRight, final int amountToMoveDown) {
        TranslateAnimation.AnimationListener listener = new TranslateAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                params.topMargin += amountToMoveDown;
                params.leftMargin += amountToMoveRight;
                view.setLayoutParams(params);
            }
        };
        return listener;
    }

}
