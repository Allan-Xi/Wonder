package org.wondertech.wonder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.wondertech.wonder.Utils.ArcTranslateAnimation;
import org.wondertech.wonder.Utils.Utilities;

import java.util.Timer;
import java.util.TimerTask;


public class SellingAnimation extends Activity {
    private static float VISIBLE = 1;
    private static float INVISIBLE = 0;
    private static float TRANSDEVIATE = 200;
    private static int TEXTFADEDURATION = 1000;
    private static int AFTERTEXTWAITTIME = 2000;
    private static int LEFTVIEWFADEDURATION = 500;
    private static int BUBBLEMOVEDURATION = 1100;
    private static int AFTERBUBBLEMOVEWAITTIME = 1900;
    private static int TEXTANDDRIVEFADEDURATION = 500;
    private static int RIGHTVIEWFADEDURATION = 1500;
    private static int FINISHWAITTIME = 2500;

    private ImageView bubble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_selling_animation);
        final TextView text1 = (TextView)findViewById(R.id.selling_text1);
        final TextView text2 = (TextView)findViewById(R.id.selling_text2);
        final TextView text3 = (TextView)findViewById(R.id.selling_text3);
        final ImageView leftView = (ImageView)findViewById(R.id.selling_left);
        final ImageView rightView = (ImageView)findViewById(R.id.selling_right);
        bubble = new ImageView(this);
        bubble.setImageResource(R.drawable.message_bubble);
        final RelativeLayout layout = (RelativeLayout)findViewById(R.id.selling_layout);
        layout.addView(bubble);
        bubble.setVisibility(View.INVISIBLE);

        //fade in text
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(text1, "alpha", INVISIBLE, VISIBLE);
        anim1.setDuration(TEXTFADEDURATION);
        ObjectAnimator anim11 = ObjectAnimator.ofFloat(text2, "alpha", INVISIBLE, VISIBLE);
        anim11.setDuration(TEXTFADEDURATION);
        ObjectAnimator anim12 = ObjectAnimator.ofFloat(text3, "alpha", INVISIBLE, VISIBLE);
        anim12.setDuration(TEXTFADEDURATION);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(text1, "alpha", VISIBLE, VISIBLE);
        anim2.setDuration(AFTERTEXTWAITTIME);

        anim2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ObjectAnimator anim00 = ObjectAnimator.ofFloat(leftView, "alpha", INVISIBLE, VISIBLE);
                anim00.setDuration(LEFTVIEWFADEDURATION);
                anim00.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        bubble.setVisibility(View.VISIBLE);
                        bubble.setAlpha(INVISIBLE);
                        ArcTranslateAnimation anim1 = new ArcTranslateAnimation(
                                (leftView.getLeft() + leftView.getRight()) / 2 - bubble.getWidth() / 2,
                                (rightView.getLeft() + rightView.getRight()) / 2 - bubble.getWidth() / 2,
                                (leftView.getTop() + leftView.getBottom()) / 2 - bubble.getHeight() / 2,
                                (leftView.getTop() + leftView.getBottom()) / 2 - bubble.getHeight() / 2,
                                -TRANSDEVIATE);
                        anim1.setDuration(BUBBLEMOVEDURATION);
                        anim1.setFillAfter(true);
                        anim1.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                bubbleAlphaAnimation();
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                ArcTranslateAnimation anim2 = new ArcTranslateAnimation(
                                        (rightView.getLeft() + rightView.getRight()) / 2 - bubble.getWidth() / 2,
                                        (leftView.getLeft() + leftView.getRight()) / 2 - bubble.getWidth() / 2,
                                        (leftView.getTop() + leftView.getBottom()) / 2 - bubble.getHeight() / 2,
                                        (leftView.getTop() + leftView.getBottom()) / 2 - bubble.getHeight() / 2,
                                        TRANSDEVIATE);
                                anim2.setDuration(BUBBLEMOVEDURATION);
                                anim2.setFillAfter(true);
                                anim2.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        bubbleAlphaAnimation();
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {

                                        ObjectAnimator anim3 = ObjectAnimator.ofFloat(bubble, "alpha", INVISIBLE, INVISIBLE);
                                        anim3.setDuration(AFTERBUBBLEMOVEWAITTIME);
                                        anim3.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                rightView.setAlpha(INVISIBLE);
                                                rightView.setImageResource(R.drawable.car);
                                            }
                                        });

                                        ObjectAnimator anim40 = ObjectAnimator.ofFloat(text3, "alpha", INVISIBLE, INVISIBLE);
                                        anim40.setDuration(TEXTANDDRIVEFADEDURATION);
                                        anim40.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                text3.setText(getString(R.string.and_drive));

                                            }
                                        });
                                        ObjectAnimator anim4 = ObjectAnimator.ofFloat(rightView, "alpha", INVISIBLE, VISIBLE);
                                        anim4.setDuration(RIGHTVIEWFADEDURATION);
                                        ObjectAnimator anim44 = ObjectAnimator.ofFloat(text3, "alpha", INVISIBLE, VISIBLE);
                                        anim44.setDuration(RIGHTVIEWFADEDURATION);
                                        anim4.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                //super.onAnimationEnd(animation);
                                                bubble.setAlpha(INVISIBLE);
                                                ArcTranslateAnimation anim1 = new ArcTranslateAnimation(
                                                        (leftView.getLeft() + leftView.getRight()) / 2 - bubble.getWidth() / 2,
                                                        (rightView.getLeft() + rightView.getRight()) / 2 - bubble.getWidth() / 2,
                                                        (leftView.getTop() + leftView.getBottom()) / 2 - bubble.getHeight() / 2,
                                                        (leftView.getTop() + leftView.getBottom()) / 2 - bubble.getHeight() / 2,
                                                        -TRANSDEVIATE);
                                                anim1.setDuration(BUBBLEMOVEDURATION);
                                                anim1.setFillAfter(true);
                                                anim1.setAnimationListener(new Animation.AnimationListener() {
                                                    @Override
                                                    public void onAnimationStart(Animation animation) {
                                                        bubbleAlphaAnimation();
                                                    }

                                                    @Override
                                                    public void onAnimationEnd(Animation animation) {
                                                        ArcTranslateAnimation anim2 = new ArcTranslateAnimation(
                                                                (rightView.getLeft() + rightView.getRight()) / 2 - bubble.getWidth() / 2,
                                                                (leftView.getLeft() + leftView.getRight()) / 2 - bubble.getWidth() / 2,
                                                                (leftView.getTop() + leftView.getBottom()) / 2 - bubble.getHeight() / 2,
                                                                (leftView.getTop() + leftView.getBottom()) / 2 - bubble.getHeight() / 2,
                                                                TRANSDEVIATE);
                                                        anim2.setDuration(BUBBLEMOVEDURATION);
                                                        anim2.setFillAfter(true);
                                                        anim2.setAnimationListener(new Animation.AnimationListener() {
                                                            @Override
                                                            public void onAnimationStart(Animation animation) {
                                                                bubbleAlphaAnimation();
                                                            }

                                                            @Override
                                                            public void onAnimationEnd(Animation animation) {
                                                                bubble.setVisibility(View.INVISIBLE);
                                                                Timer timer = new Timer();
                                                                timer.schedule(new TimerTask() {
                                                                    public void run() {
                                                                        startActivity(new Intent(SellingAnimation.this, DistractDriving.class));
                                                                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                                                    }

                                                                }, FINISHWAITTIME);
                                                            }

                                                            @Override
                                                            public void onAnimationRepeat(Animation animation) {

                                                            }
                                                        });
                                                        bubble.startAnimation(anim2);
                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(Animation animation) {

                                                    }
                                                });
                                                bubble.startAnimation(anim1);
                                            }
                                        });

                                        AnimatorSet set = new AnimatorSet();
                                        set.play(anim3).before(anim40);
                                        set.play(anim40).before(anim44);
                                        set.play(anim40).before(anim4);
                                        set.start();
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                bubble.startAnimation(anim2);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        bubble.startAnimation(anim1);
                        super.onAnimationEnd(animation);
                    }
                });
                ObjectAnimator anim01 = ObjectAnimator.ofFloat(rightView, "alpha", INVISIBLE, VISIBLE);
                anim01.setDuration(LEFTVIEWFADEDURATION);
                AnimatorSet set = new AnimatorSet();
                set.play(anim00).with(anim01);
                set.start();
            }
        });


        AnimatorSet set = new AnimatorSet();
        set.play(anim1).before(anim2);
        set.play(anim11).with(anim1);
        set.play(anim12).with(anim1);
        set.start();
    }

    private void bubbleAlphaAnimation(){
        ObjectAnimator anim0 = ObjectAnimator.ofFloat(bubble, "alpha", INVISIBLE, VISIBLE);
        anim0.setDuration(BUBBLEMOVEDURATION / 2);
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(bubble, "alpha", VISIBLE, INVISIBLE);
        anim1.setDuration(BUBBLEMOVEDURATION / 2);
        AnimatorSet set = new AnimatorSet();
        set.play(anim0).before(anim1);
        set.start();
    }

    @Override
    public void onBackPressed() {
        Utilities.goBackHome(this);
    }
}
