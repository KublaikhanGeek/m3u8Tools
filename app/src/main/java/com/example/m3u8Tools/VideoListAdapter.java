package com.example.m3u8Tools;

import android.content.Context;
import android.content.Intent;
import android.text.NoCopySpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

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

    private String m3u8 = "/index.m3u8";
    private LayoutInflater mLayoutInflater;
    private List<VideoBean> mListData;
    private int mExpandedMenuPos = -1;
    private int mLastPos = -1;
    private OnMenuClickListenser mOnMenuClickListenser = new OnMenuClickListenser();

    public VideoListAdapter(Context context, int resource, List<VideoBean> objects) {
        super(context, resource, objects);
        mLayoutInflater = LayoutInflater.from(context);
        mListData = objects;
    }

    private class ViewHolder {
        public ViewHolder (View viewRoot) {
            root = viewRoot;
            fileNameText = (TextView)viewRoot.findViewById(R.id.item_name);
            linearLayout = (LinearLayout) viewRoot.findViewById(R.id.layout_other);
            playLinearLayout = (LinearLayout) viewRoot.findViewById(R.id.item_play);
            convertLinearLayout = (LinearLayout) viewRoot.findViewById(R.id.item_convert);
            delLinearLayout = (LinearLayout) viewRoot.findViewById(R.id.item_del);
        }

        public View root;
        public TextView fileNameText;
        public LinearLayout linearLayout;
        public LinearLayout playLinearLayout;
        public LinearLayout convertLinearLayout;
        public LinearLayout delLinearLayout;
    }

    @Override
    public int getCount() {
        return mListData == null ? 0 : mListData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.listview_menu_item, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }

        if (convertView != null && convertView.getTag() instanceof ViewHolder) {
            final ViewHolder holder = (ViewHolder)convertView.getTag();
            VideoBean bean = getItem(position);
            holder.fileNameText.setText(bean.name);

            if (position == mExpandedMenuPos)
            {
                if (position == mLastPos)
                {
                    holder.linearLayout.setVisibility(View.VISIBLE == holder.linearLayout.getVisibility() ? View.GONE : View.VISIBLE);
                }
                else
                {
                    holder.linearLayout.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                holder.linearLayout.setVisibility(View.GONE);
            }
            holder.linearLayout.getVisibility();
            holder.playLinearLayout.setOnClickListener(mOnMenuClickListenser);
            holder.convertLinearLayout.setOnClickListener(mOnMenuClickListenser);
            holder.delLinearLayout.setOnClickListener(mOnMenuClickListenser);
        }
        return convertView;
    }

    private class OnMenuClickListenser implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final int id = v.getId();
            if (id == R.id.item_play) {
                Toast.makeText(mLayoutInflater.getContext(), "播放" + mExpandedMenuPos, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mLayoutInflater.getContext(),FullScreenActivity.class);
                intent.putExtra("M3U8_URL", getItem(mExpandedMenuPos).url + m3u8);
                mLayoutInflater.getContext().startActivity(intent);
            } else if (id == R.id.item_convert) {
                Toast.makeText(mLayoutInflater.getContext(), "转换" + mExpandedMenuPos, Toast.LENGTH_SHORT).show();
                Converter converter = new Converter(getItem(mExpandedMenuPos).url, getItem(mExpandedMenuPos).name);
                converter.convertVideo();
            } else if (id == R.id.item_del) {
                Toast.makeText(mLayoutInflater.getContext(), "删除" + mExpandedMenuPos, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setOnClickItemId(int position) {mLastPos = mExpandedMenuPos; mExpandedMenuPos = position; }
}
