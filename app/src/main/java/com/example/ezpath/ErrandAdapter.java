package com.example.ezpath;

import android.app.Activity;
import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;



import java.util.ArrayList;

public class ErrandAdapter extends ArrayAdapter<ErrandResults> {

    private final Context context;
    private final ArrayList<ErrandResults> data;
    private final int layoutResourceId;

    public ErrandAdapter(Context context, int layoutResourceId, ArrayList<ErrandResults> errandResults) {
        super(context, layoutResourceId, errandResults);
        this.context = context;
        this.data = errandResults;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();

            holder.textView1 = (TextView) row.findViewById(R.id.errand_name);
            holder.textView2 = (TextView) row.findViewById(R.id.address);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        ErrandResults errResults = data.get(position);
        holder.textView1.setText(errResults.getErrand());
        holder.textView2.setText(errResults.getBestPlace().getFormatted_address());

        return row;
    }

    static class ViewHolder {
        TextView textView1;
        TextView textView2;
    }


}
