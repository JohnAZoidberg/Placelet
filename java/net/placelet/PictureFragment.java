package net.placelet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import net.placelet.data.Bracelet;

public class PictureFragment extends Fragment {
    private BraceletActivity braceletActivity;

    private PictureDetailAdapter adapter;
    private ExpandableListView list;

    private Bracelet bracelet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_picture, container, false);
        braceletActivity = (BraceletActivity) getActivity();
        bracelet = braceletActivity.bracelet;
        list = (ExpandableListView) rootView.findViewById(R.id.listView1);
        // collapses previous group if new one is expanded
        list.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            private int prevPosition = -1;
            @Override
            public void onGroupExpand(int groupPosition) {
                if(prevPosition != -1 && prevPosition != groupPosition) {
                    list.collapseGroup(prevPosition);
                }
                prevPosition = groupPosition;
            }
        });
        adapter = new PictureDetailAdapter(braceletActivity, 0, bracelet.pictures);
        list.setAdapter(adapter);
        return rootView;
    }

    public void updateData() {
        adapter.notifyDataSetChanged();
    }
}
