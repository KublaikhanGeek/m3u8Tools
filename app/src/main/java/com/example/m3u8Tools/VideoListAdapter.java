package com.example.m3u8Tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/11/21
 * 描    述:
 * ================================================
 */
public class VideoListAdapter extends ArrayAdapter<VideoBean> {

    private int resourceId;

    public VideoListAdapter(Context context, int resource, List<VideoBean> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VideoBean mediaBean = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView urlName = (TextView) view.findViewById(R.id.video_name);
        urlName.setText(mediaBean.name);
        return view;
    }
}
