package com.example.ezpath;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class SavedExpandableAdapter extends BaseExpandableListAdapter {
    Context context;
    String listGroup = "LOAD PATH";
    ArrayList<String> savedPathNames;

    public SavedExpandableAdapter(Context context, ArrayList<String> savedPathNames) {
        this.context = context;
        this.savedPathNames = savedPathNames;
    }


    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return savedPathNames.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listGroup;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return savedPathNames.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String group = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.listTitle);
        listTitleTextView.setText(group);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);

        }
        TextView expandedListTextView = (TextView) convertView.findViewById(R.id.expandedListItem);
        expandedListTextView.setText(expandedListText);

        expandedListTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SavedPath path = ((MainActivity) context).uiThreadRealm.where(SavedPath.class).equalTo("pathName", expandedListText).findFirst();
                ((MainActivity) context).load(path);
            }
        });
        Button xButton = (Button) convertView.findViewById(R.id.x_button);
        xButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savedPathNames.remove(childPosition);
                ((MainActivity) context).uiThreadRealm.executeTransaction(r -> {
                    SavedPath p = r.where(SavedPath.class).equalTo("pathName", expandedListText).findFirst();
                    p.deleteFromRealm();
                    notifyDataSetChanged();
                });
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
