package com.gfk.s2s.ui.videolist.videoparams;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gfk.s2s.demo.s2s.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VideoParamsAdapter extends BaseAdapter {

    private List<Map.Entry<String,String>> listItems = new ArrayList();
    private LayoutInflater minflater;
    private VideoParamsDialog.VideoParamManager paramManager;

    VideoParamsAdapter(Context mContext, Set<Map.Entry<String, String>> listItems, VideoParamsDialog.VideoParamManager paramManager) {
        this.listItems.addAll(listItems);
        this.minflater = LayoutInflater.from(mContext);
        this.paramManager = paramManager;
    }

    @Override
    public int getCount() {
        return listItems != null ? listItems.size() : 0;
    }

    @Override
    public Map.Entry getItem(int location) {
        return listItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = minflater.inflate(R.layout.dialog_item_row, parent, false);

            holder = new ViewHolder();
            holder.key = convertView.findViewById(R.id.row_key);
            holder.value = convertView.findViewById(R.id.row_value);
            holder.delete = convertView.findViewById(R.id.row_delete);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Map.Entry<String,String> mListItem = listItems.get(position);

        holder.key.setText(mListItem.getKey());
        holder.value.setText(mListItem.getValue());
        holder.delete.setTag(position);
        holder.delete.setOnClickListener(v -> {
            int position1 = (Integer) v.getTag();
            deleteItem(position1);
        });

        return convertView;
    }

    void updateItems(Set<Map.Entry<String, String>> items) {
        listItems.clear();
        listItems.addAll(items);
        this.notifyDataSetChanged();
    }

    private void deleteItem(int position) {
        Map.Entry entry = listItems.get(position);
        paramManager.getVideoParams().remove(entry.getKey());
        listItems.remove(entry);
        this.notifyDataSetChanged();
    }

    class ViewHolder {
        TextView key;
        TextView value;
        ImageButton delete;
    }
}
