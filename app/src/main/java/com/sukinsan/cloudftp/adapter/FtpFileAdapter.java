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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by victor on 1/3/2018.
 */

public class FtpFileAdapter extends RecyclerView.Adapter<FtpFileAdapter.Holder> {
    private static final String TAG = FtpFileAdapter.class.getSimpleName();

    public interface Event {
        boolean isSynced(FtpItem ftpItem);

        void OnActionFtpBack();

        void OnActionExecute(FtpItem ftpItem);

        void OnActionDownload(FtpItem ftpItem);

        void OnActionMoveToDownload(FtpItem ftpItem);

        void OnActionSync(FtpItem ftpItem);

        void OnActionUnSync(FtpItem ftpItem);

        void OnActionDelete(FtpItem ftpItem);

    }

    public class Holder extends RecyclerView.ViewHolder {
        private View root;
        private View actions;
        private View bgType;
        private TextView name;

        public Holder(View itemView) {
            super(itemView);
            root = itemView;
            actions = itemView.findViewById(R.id.actions);
            name = itemView.findViewById(R.id.name);
            bgType = itemView.findViewById(R.id.bgType);
        }

        public void bindTopFolder(final Event callback) {
            actions.setVisibility(View.GONE);
            bgType.getBackground().setLevel(0);
            name.setText("..");
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.OnActionFtpBack();
                }
            });
        }

        public void bind(final FtpItem ftpItem, final Event callback) {
            if (ftpItem.isDirectory()) {
                bgType.getBackground().setLevel(0);
                name.setText("[" + ftpItem.getName() + "]");
            } else {
                if (callback.isSynced(ftpItem)) {
                    bgType.getBackground().setLevel(2);
                } else {
                    bgType.getBackground().setLevel(1);
                }
                name.setText(ftpItem.getName());
            }

            if (selected != null && ftpItem.getPath().equals(selected.getPath())) {
                actions.setVisibility(View.VISIBLE);
            } else {
                actions.setVisibility(View.GONE);
            }

            root.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    selected = ftpItem;
                    notifyDataSetChanged();
                    return true;
                }
            });

            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected = null;
                    if (ftpItem.isDirectory() || callback.isSynced(ftpItem)) {
                        callback.OnActionExecute(ftpItem);
                    } else {
                        selected = ftpItem;
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private Event callback;
    private List<FtpItem> items;
    private FtpItem selected;

    public FtpFileAdapter(@NonNull Event callback) {
        this.callback = callback;
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
            holder.bindTopFolder(callback);
        } else {
            FtpItem ftpItem = items.get(position - 1);
            holder.bind(ftpItem, callback);
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + 1;
    }
}