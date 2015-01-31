package com.stbam.rssnewsreader.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.stbam.rssnewsreader.R;

// tomado de http://developer.android.com/guide/topics/ui/layout/gridview.html
public class GridViewAdapter extends BaseAdapter {
    private Context mContext;

    public GridViewAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setPadding(5, 5, 5, 5);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.food_category, R.drawable.gaming_category,
            R.drawable.music_category, R.drawable.news_category,
            R.drawable.sports_category, R.drawable.tech_category,
            R.drawable.food_category, R.drawable.gaming_category,
            R.drawable.music_category, R.drawable.news_category,
            R.drawable.sports_category, R.drawable.tech_category,
            R.drawable.food_category, R.drawable.gaming_category,
            R.drawable.music_category, R.drawable.news_category,
            R.drawable.sports_category, R.drawable.tech_category
    };
}