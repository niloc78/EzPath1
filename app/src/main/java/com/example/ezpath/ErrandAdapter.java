package com.example.ezpath;

import android.app.Activity;
import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.core.content.ContextCompat;

import com.daimajia.swipe.adapters.ArraySwipeAdapter;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.button.MaterialButton;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ErrandAdapter extends ArraySwipeAdapter<ErrandResults> {

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
            holder.textView3 = (TextView) row.findViewById(R.id.place_name);
            holder.remove = (MaterialButton) row.findViewById(R.id.remove_button);
            holder.check = (Button) row.findViewById(R.id.check_button);
            Button b = holder.check;

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        ErrandResults errResults = data.get(position);
        holder.textView1.setText(errResults.getErrand());
        Result bestResult = errResults.getBestPlace();
        Button b = holder.check;
        holder.textView2.setText(bestResult.getFormatted_address());
        holder.textView3.setText(bestResult.getName());
        holder.remove.setTag(position);
        holder.check.setTag(position);
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.remove(position);
                ((MainActivity)context).removeErrand(position);
                notifyDataSetChanged();
                ((MainActivity)context).updatePolyMap();
            }
        });


        holder.check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!b.isSelected()) {
                    b.setSelected(true);
                    ((MainActivity)context).markers.get(position).setIcon(BitmapDescriptorFactory.defaultMarker((float) 90));
                    notifyDataSetChanged();

                } else {
                    b.setSelected(false);
                    ((MainActivity)context).markers.get(position).setIcon(BitmapDescriptorFactory.defaultMarker());
                    notifyDataSetChanged();

                }

            }
        });


        return row;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return this.layoutResourceId;
    }

    static class ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3;
        MaterialButton remove;
        Button check;
        boolean checked = false;

    }


}
