package com.sukinsan.cloudftp.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sukinsan.cloudftp.R;
import com.sukinsan.koshcloudcore.item.FtpItem;
import com.sukinsan.koshcloudcore.util.CloudSyncUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by victor on 1/3/2018.
 */

public class FtpFileAdapter extends RecyclerView.Adapter<FtpFileAdapter.Holder> {
    private static final String TAG = FtpFileAdapter.class.getSimpleName();

    public class Holder extends RecyclerView.ViewHolder {
        public View root;
        public View bgType;
        public TextView name;

        public Holder(View itemView) {
            super(itemView);
            root = itemView;
            name = itemView.findViewById(R.id.name);
            bgType = itemView.findViewById(R.id.bgType);
        }
    }

    public interface Event {
        void OnFtpBackClick();

        void OnFtpItemClick(FtpItem ftpItem);
    }

    private Event callback;
    private List<FtpItem> items;
    private CloudSyncUtil cloudSyncUtil;

    public FtpFileAdapter(@NonNull Event callback, CloudSyncUtil cloudSyncUtil) {
        this.callback = callback;
        this.cloudSyncUtil = cloudSyncUtil;
        setNewItems(new ArrayList<FtpItem>());
    }

    public void setNewItems(List<FtpItem> items) {
        Log.i(TAG, "setNewItems " + items);
        this.items = items;
        notifyDataSetChanged();
    }

    public void clear() {
        this.items.clear();
        notifyDataSetChanged();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new Holder(inflater.inflate(R.layout.adapter_ftp_file, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Log.i(TAG, "onBindViewHolder " + position);

        if (position == 0) {
            holder.bgType.getBackground().setLevel(1);
            holder.name.setText("..");
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.OnFtpBackClick();
                }
            });
        } else {
            final FtpItem ftpItem = items.get(position - 1);
            if (ftpItem.isDirectory()) {
                holder.bgType.getBackground().setLevel(1);
                holder.name.setText("[" + ftpItem.getName() + "]");
            } else {
                holder.bgType.getBackground().setLevel(0);
                if (cloudSyncUtil.isSynced(ftpItem)) {
                    holder.name.setText(ftpItem.getName() + "*");
                } else {
                    holder.name.setText(ftpItem.getName());
                }
            }

            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.OnFtpItemClick(ftpItem);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + 1;
    }
}