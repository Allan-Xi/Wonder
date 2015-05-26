package org.wondertech.wonder;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class slideFragment extends Fragment {
	public static final String ARG_PAGE = "page";
	private int mPageNumber;
	private ImageView stick;
	private ImageView img;
	private TextView tv1;
	private TextView tv2;
	private Spannable span;
    private RelativeLayout bg;
    private FrameLayout bg2;
    
	
    
    public static slideFragment create(int pageNumber) {
    	slideFragment fragment = new slideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);

    }
    
    public int getPageNumber() {
        return mPageNumber;
    }
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup rootView;
        
        switch(mPageNumber)
        {
        case 0:
        	rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_slide2, container, false);
        	tv1 = (TextView)rootView.findViewById(R.id.slide2_text1);
        	tv1.setText("\u2705Amelia");
        	tv1 = (TextView)rootView.findViewById(R.id.slide2_text2);
        	tv1.setText("\u2705Alex");
         	bg = (RelativeLayout)rootView.findViewById(R.id.slide2_bg1);
        	bg.setBackgroundResource(R.drawable.discripton_page01_2);
        	//bg = (RelativeLayout)rootView.findViewById(R.id.slide2_bg2);
        	//bg.setBackgroundResource(R.drawable.discripton_page01_3);
        	break;
        case 1:
        	rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_slide2, container, false);
        	tv1 = (TextView)rootView.findViewById(R.id.slide2_text1);
        	tv1.setText("\uD83D\uDD34\uD83D\uDE97Amelia");
        	span = new SpannableString(tv1.getText());
        	span.setSpan(new RelativeSizeSpan(0.6f), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	tv1.setText(span);
        	tv1 = (TextView)rootView.findViewById(R.id.slide2_text2);
        	tv1.setText("\uD83D\uDD34\uD83D\uDE97Alex");
        	span = new SpannableString(tv1.getText());
        	span.setSpan(new RelativeSizeSpan(0.6f), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	tv1.setText(span);
        	bg = (RelativeLayout)rootView.findViewById(R.id.slide2_bg1);
        	bg.setBackgroundResource(R.drawable.discripton_page02_2);
        	bg = (RelativeLayout)rootView.findViewById(R.id.slide2_bg2);
        	bg.setBackgroundResource(R.drawable.discripton_page02_3);
        	break;
        case 2:
        	rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_slide, container, false);
        	tv1 = (TextView)rootView.findViewById(R.id.slide_text1);
        	tv1.setText("\uD83D\uDD34\uD83D\uDE97");
        	tv1 = (TextView)rootView.findViewById(R.id.stick_tv1);
            tv2 = (TextView)rootView.findViewById(R.id.stick_tv2);
            stick = (ImageView)rootView.findViewById(R.id.slide_stick);
            bg2 = (FrameLayout)rootView.findViewById(R.id.slide_layout);
            bg2.setBackgroundResource(R.drawable.discripton_page03_2);
            break;
        case 3:
        	rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_slide, container, false);
        	tv1 = (TextView)rootView.findViewById(R.id.slide_text1);
        	tv1.setText("\uD83D\uDD34\uD83D\uDE97");
        	tv1 = (TextView)rootView.findViewById(R.id.stick_tv1);
            tv2 = (TextView)rootView.findViewById(R.id.stick_tv2);
            stick = (ImageView)rootView.findViewById(R.id.slide_stick);
            bg2 = (FrameLayout)rootView.findViewById(R.id.slide_layout);
            bg2.setBackgroundResource(R.drawable.discripton_page05_2);
            break;
        default:
        	rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_slide2, container, false);
        }
        
        return rootView;
    }
	
	
	@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
      super.setUserVisibleHint(isVisibleToUser);
      if (isVisibleToUser) {
          if (mPageNumber == 2)
          {
        	bg2.setBackgroundResource(R.drawable.discripton_page03_2);  
          	ObjectAnimator animator1 = ObjectAnimator.ofFloat(stick, "scaleY", 
              		0f, 1f);
              animator1.setDuration(900);
              animator1.start();
              animator1.addListener(new AnimatorListenerAdapter() {
      	        	 
              	@Override
              	public void onAnimationEnd(Animator animation) {
              	    super.onAnimationEnd(animation);
              	    bg2.setBackgroundResource(R.drawable.discripton_page03_3);
              	}

              	});
          }
          else if (mPageNumber == 3)
          {
        	bg2.setBackgroundResource(R.drawable.discripton_page05_2);   
          	stick.setImageResource(R.drawable.pink_stick);
          	tv1.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 214f));
          	stick.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 488f));
          	tv2.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0f));
          	ObjectAnimator animator1 = ObjectAnimator.ofFloat(stick, "scaleY", 
              		0f, 1f);
              animator1.setDuration(900);
              animator1.start();
              animator1.addListener(new AnimatorListenerAdapter() {
      	        	 
              	@Override
              	public void onAnimationEnd(Animator animation) {
              	    super.onAnimationEnd(animation);
              	    bg2.setBackgroundResource(R.drawable.discripton_page05_3);
              	    /*done.setVisibility(View.VISIBLE);
              	    done.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Log.v("done","123");
			    			new setTokenID().execute();						}
					});*/
              	}

              	});

          } 
      }
    }
	
	
}
