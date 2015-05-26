package org.wondertech.wonder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.wondertech.wonder.R;

public class drawerAdapter extends BaseAdapter{
	static class ViewHolder  
    {   
        public TextView category;
        public ImageView icon;
    }
	//private SharedPreferences userInfo;
	private LayoutInflater mInflater = null;
    private String[] mDrawerItems;
    private int[] mDrawerIcons = {R.drawable.main_icon,
    		R.drawable.notification_bell,
    		R.drawable.account_icon, R.drawable.menu_question, R.drawable.icon_invite_menu};
	Context context;
	public drawerAdapter(Context context)
    {  
		this.context = context;
        this.mInflater = LayoutInflater.from(context); 
        mDrawerItems = context.getResources().getStringArray(R.array.drawers_array);
    }  

    @Override  
    public int getCount() {  
        //How many items are in the data set represented by this Adapter.  
        //在此适配器中所代表的数据集中的条目数  
        return mDrawerItems.length;  
    }  

    @Override  
    public Object getItem(int position) {  
        // Get the data item associated with the specified position in the data set.  
        //获取数据集中与指定索引对应的数据项  
        return position;  
    }  

    @Override  
    public long getItemId(int position) {  
        //Get the row id associated with the specified position in the list.  
        //获取在列表中与指定索引对应的行id  
        return position;  
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position != 4)
		{
			ViewHolder holder = null;
	    	if(convertView == null)  
	        {  
	            holder = new ViewHolder();  
	            //根据自定义的Item布局加载布局  
	            convertView = mInflater.inflate(R.layout.drawer_item, null);  
	            holder.icon = (ImageView)convertView.findViewById(R.id.drawer_icon); 
	            holder.category = (TextView)convertView.findViewById(R.id.drawer_title);
	            convertView.setTag(holder);  
	        }else  
	        {  
	        	holder = (ViewHolder)convertView.getTag();
	        }
	    	holder.icon.setImageResource(mDrawerIcons[position]);
	    	holder.category.setText(mDrawerItems[position]);
		}
		
		else if (position == 4)
    	{
			if(convertView == null)
				convertView = mInflater.inflate(R.layout.drawer_item2, null);
    		ImageView iv = (ImageView)convertView.findViewById(R.id.drawer_invite);
    		iv.setImageResource(R.drawable.invite_button_ham);
    	}
    	return convertView;
	}  
    
}
