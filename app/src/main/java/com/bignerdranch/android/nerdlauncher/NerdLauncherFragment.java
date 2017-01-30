package com.bignerdranch.android.nerdlauncher;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by RBanks on 10/5/2016.
 */

public class NerdLauncherFragment extends Fragment {
    private static final String TAG = "NerdLauncherFragment";

    private RecyclerView m_recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_nerd_launcher, container, false);
        m_recyclerView = (RecyclerView) v.findViewById(R.id.fragment_nerd_launcher_recycler_view);
        m_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();

        return v;
    }

    public static NerdLauncherFragment newInstance() {
        return new NerdLauncherFragment();
    }

    private void setupAdapter(){
        Intent startupIntent = new Intent(Intent.ACTION_MAIN);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        //package manager is the guy that opens the window to all activities on the device
        final PackageManager pm = getActivity().getPackageManager();
        //this will list all launcher activities
        List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);

        //now we are going to sort through all those activities
        Collections.sort(activities, new Comparator<ResolveInfo>() {
            public int compare (ResolveInfo a, ResolveInfo b) {
                return String.CASE_INSENSITIVE_ORDER.compare(
                        a.loadLabel(pm).toString(),
                        b.loadLabel(pm).toString()
                );
            }
        });

        Log.i(TAG, "Found " + activities.size() + " activities");
        m_recyclerView.setAdapter(new ActivityAdpater(activities));
    }

    private class ActivityHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //we are going to use this member variable alot later on
        private ResolveInfo m_resolveInfo;
        //holds the text of our label
        private TextView m_nameTextView;
        private ImageView m_icon;

        public ActivityHolder(View view) {
            super(view);
            m_nameTextView = (TextView)view.findViewById(R.id.launcher_name);
            m_icon = (ImageView)view.findViewById(R.id.icon);
            view.setOnClickListener(this);
        }

        //binds the resolve info to this viewHolder for later usesage...each viewHolder will have it's own resolveInfo
        public void bindActivity(ResolveInfo resolveInfo) {
            m_resolveInfo = resolveInfo;
            PackageManager pm = getActivity().getPackageManager();
            String appName = m_resolveInfo.loadLabel(pm).toString();
            m_nameTextView.setText(appName);
            m_icon.setImageDrawable(m_resolveInfo.loadIcon(pm));
        }

        @Override
        public void onClick(View v) {
            ActivityInfo activityInfo = m_resolveInfo.activityInfo;

            Intent i = new Intent(Intent.ACTION_MAIN)
                    .setClassName(activityInfo.applicationInfo.packageName,
                            activityInfo.name);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(i);
        }
    }

    //this private class will hold our views and make sure they transition safely
    private class ActivityAdpater extends RecyclerView.Adapter<ActivityHolder> {
        private final List<ResolveInfo> m_activities;

        public ActivityAdpater(List<ResolveInfo> activities) {
            m_activities = activities;
        }

        @Override
        public ActivityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_layout, parent, false);

            return new ActivityHolder(view);
        }

        @Override
        public void onBindViewHolder(ActivityHolder holder, int position) {
            ResolveInfo resolveInfo = m_activities.get(position);
            holder.bindActivity(resolveInfo);
        }

        @Override
        public int getItemCount() {
            return m_activities.size();
        }
    }
}
