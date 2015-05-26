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
import android.widget.ImageButton;
import android.widget.ImageView;

import org.wondertech.wonder.Utils.Utilities;


public class DistractDriving extends Activity {
    private static float SMALLSIZE = 0;
    private static float BIGSIZE = 1;
    private static long BLACKCARMOVEDURATION = 800;
    private static long BLACKCARMOVEDELAY = 2000;
    private static float INVISIBLE = 0;
    private static float VISIBLE = 1;
    private static long DOTSHOWWAITTIME = 1500;
    private static long DOTFADEDURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_distract_driving);
        final ImageView blackCar = (ImageView)findViewById(R.id.distract_blackcar);
        final ImageView firstDot = (ImageView)findViewById(R.id.distracting_firstdot);
        final ImageView secondDot = (ImageView)findViewById(R.id.distracting_seconddot);
        final ImageButton cloud = (ImageButton)findViewById(R.id.distracting_cloud);
        final ImageButton replay = (ImageButton)findViewById(R.id.distracting_replay);
        cloud.setEnabled(false);
        replay.setEnabled(false);
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(blackCar, "scaleX",SMALLSIZE,BIGSIZE);
        anim1.setDuration(BLACKCARMOVEDURATION);
        anim1.setStartDelay(BLACKCARMOVEDELAY);
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                blackCar.setAlpha(VISIBLE);
            }
        });
        ObjectAnimator anim11 = ObjectAnimator.ofFloat(firstDot, "alpha",INVISIBLE,INVISIBLE);
        anim11.setDuration(DOTSHOWWAITTIME);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(firstDot, "alpha",INVISIBLE,VISIBLE);
        anim2.setDuration(DOTFADEDURATION);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(secondDot, "alpha",INVISIBLE,VISIBLE);
        anim3.setDuration(DOTFADEDURATION);
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(cloud, "alpha",INVISIBLE,VISIBLE);
        anim4.setDuration(DOTFADEDURATION);
        ObjectAnimator anim5 = ObjectAnimator.ofFloat(cloud, "alpha",VISIBLE,VISIBLE);
        anim5.setDuration(DOTFADEDURATION);
        anim5.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cloud.setImageResource(R.drawable.cloud_tap);
                cloud.setEnabled(true);
                replay.setAlpha(VISIBLE);
                replay.setEnabled(true);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(anim1).before(anim11);
        set.play(anim11).before(anim2);
        set.play(anim2).before(anim3);
        set.play(anim3).before(anim4);
        set.play(anim4).before(anim5);
        set.start();
        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DistractDriving.this, SellingAnimation.class));
            }
        });
        cloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DistractDriving.this, WhoisDriving.class));
                overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );

            }
        });
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Utilities.goBackHome(this);
    }
}
