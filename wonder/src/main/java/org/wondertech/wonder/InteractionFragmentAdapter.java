package org.wondertech.wonder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class InteractionFragmentAdapter extends FragmentPagerAdapter{
    private int mCount = 1;

    public InteractionFragmentAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
    	switch(position)
    	{
    	case 0:
    		return InteractionFragment.newInstance();
    	case 1:
    		return InviteFragment.newInstance();
    	default:
    		return InteractionFragment.newInstance();
    	}
    }

    @Override
    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}