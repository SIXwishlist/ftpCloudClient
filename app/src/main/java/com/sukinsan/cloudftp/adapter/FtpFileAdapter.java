package com.sukinsan.cloudftp.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sukinsan.cloudftp.Constant;
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

        void OnActionExecute(FtpItem ftpItem);

        void OnActionDownload(FtpItem ftpItem);

        void OnActionSync(FtpItem ftpItem);

        void OnActionDelete(FtpItem ftpItem);

        void OnActionShare(FtpItem ftpItem);

    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final View
                fileLayout,
                fileType,
                actionsLayout,
                actionSync,
                actionDownload,
                actionDelete,
                actionShare;
        private TextView fileName, fileSize, actionExe;

        public Holder(View itemView) {
            super(itemView);

            fileLayout = itemView.findViewById(R.id.fileLayout);
            fileName = itemView.findViewById(R.id.name);
            fileSize = itemView.findViewById(R.id.file_size);
            fileType = itemView.findViewById(R.id.bgType);

            actionsLayout = itemView.findViewById(R.id.actions);
            actionExe = itemView.findViewById(R.id.action_exe);
            actionSync = itemView.findViewById(R.id.action_sync);
            actionDownload = itemView.findViewById(R.id.action_download);
            actionDelete = itemView.findViewById(R.id.action_delete);
            actionShare = itemView.findViewById(R.id.action_share);

            fileLayout.setOnClickListener(this);
            actionExe.setOnClickListener(this);
            actionSync.setOnClickListener(this);
            actionDownload.setOnClickListener(this);
            actionDelete.setOnClickListener(this);
            actionShare.setOnClickListener(this);
        }

        public void bind(final FtpItem ftpItem) {
            if (ftpItem.isDirectory()) {
                fileType.getBackground().setLevel(0);
                fileName.setText("[" + ftpItem.getName() + "]");
                fileSize.setText(null);

                actionExe.setText(R.string.action_open);
                actionShare.setVisibility(View.INVISIBLE);
            } else {
                actionExe.setText(R.string.action_execute);
                actionShare.setVisibility(View.VISIBLE);

                if (callback.isSynced(ftpItem)) {
                    fileType.getBackground().setLevel(2);
                } else {
                    fileType.getBackground().setLevel(1);
                }
                fileSize.setText(Constant.getSize(ftpItem.length()));
                fileName.setText(ftpItem.getName());
            }

            if (selected == null || !ftpItem.getPath().equals(selected.getPath())) {
                actionsLayout.setVisibility(View.GONE);
            } else {
                actionsLayout.setVisibility(View.VISIBLE);
                actionExe.setOnClickListener(this);
            }

            fileLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    selected = ftpItem;
                    notifyDataSetChanged();
                    return true;
                }
            });

            fileLayout.setOnClickListener(new View.OnClickListener() {
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

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.action_exe:
                    callback.OnActionExecute(selected);
                    break;
                case R.id.action_sync:
                    callback.OnActionSync(selected);
                    break;
                case R.id.action_download:
                    callback.OnActionDownload(selected);
                    break;
                case R.id.action_delete:
                    callback.OnActionDelete(selected);
                    break;
                case R.id.action_share:
                    callback.OnActionShare(selected);
                    break;
            }
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

        FtpItem ftpItem = items.get(position);
        holder.bind(ftpItem);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}