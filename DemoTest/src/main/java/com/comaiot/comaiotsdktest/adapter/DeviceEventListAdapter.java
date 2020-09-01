package com.comaiot.comaiotsdktest.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.comaiot.comaiotsdktest.R;
import com.comaiot.comaiotsdktest.act.PlayVideoActivity;
import com.comaiot.comaiotsdktest.util.AppUtils;
import com.comaiot.net.library.bean.DeviceEventListEntity;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.text.SimpleDateFormat;
import java.util.List;

@SuppressWarnings("all")
public class DeviceEventListAdapter extends BaseAdapter {

    private final SimpleDateFormat simpleDateFormat;
    private Context mContext;
    private List<DeviceEventListEntity> mList;
    private OnItemDeleteClickListener mListener;

    public DeviceEventListAdapter(Context context, List<DeviceEventListEntity> deviceEventList) {
        this.mContext = context;
        this.mList = deviceEventList;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public DeviceEventListEntity getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device_event, null);
            holder.item_time_layout = convertView.findViewById(R.id.item_time_layout);
            holder.item_date = convertView.findViewById(R.id.item_date);
            holder.content_device = convertView.findViewById(R.id.content_device);
            holder.message_image_layout = convertView.findViewById(R.id.message_image);
            holder.mImgView = convertView.findViewById(R.id.item_thumbnail);
            holder.item_thumbnail_video = convertView.findViewById(R.id.item_thumbnail_video);
            holder.swipView = convertView.findViewById(R.id.swip_view);
            holder.btnDelete = convertView.findViewById(R.id.btnDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DeviceEventListEntity deviceEventListEntity = mList.get(position);
        String fileUrl = deviceEventListEntity.getUrl();
        String msgType = deviceEventListEntity.getMsg_type();
        String fileType = deviceEventListEntity.getFile_type();
        String srcFilename = deviceEventListEntity.getSrc_filename();
        long uploadDate = deviceEventListEntity.getUpload_date();

        String formatTime = simpleDateFormat.format(uploadDate);

        if (msgType.equals("InnerCall")) {
            holder.item_date.setTextIsSelectable(false);
            holder.item_date.setText("");
            holder.message_image_layout.setVisibility(View.GONE);
            holder.item_thumbnail_video.setVisibility(View.GONE);
            holder.content_device.setText("室内通话事件");
        } else if (msgType.equals("alarm")) {
            holder.item_date.setTextIsSelectable(false);
            holder.item_date.setText(srcFilename.substring(1, srcFilename.length() - 4).replaceAll("-", ":"));
            holder.message_image_layout.setVisibility(View.VISIBLE);
            holder.content_device.setText("侦测报警事件");

            if (fileType.equals("image")) {
                loadFileUrl(fileUrl, holder.mImgView);
            } else {
                loadFileUrl(fileUrl, holder.mImgView);
                holder.item_thumbnail_video.setVisibility(View.VISIBLE);

                holder.item_thumbnail_video.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, PlayVideoActivity.class);
                        intent.putExtra("fileUrl", fileUrl);
                        mContext.startActivity(intent);
                    }
                });
            }
        } else if (msgType.equals("call")) {
            holder.item_date.setTextIsSelectable(false);
            holder.item_date.setText(srcFilename.substring(1, srcFilename.length() - 4).replaceAll("-", ":"));
            holder.message_image_layout.setVisibility(View.VISIBLE);
            holder.item_thumbnail_video.setVisibility(View.GONE);
            holder.content_device.setText("门铃事件");
            loadFileUrl(fileUrl, holder.mImgView);
        } else if (msgType.equals("log")) {
            holder.message_image_layout.setVisibility(View.GONE);
            holder.item_thumbnail_video.setVisibility(View.GONE);
            holder.content_device.setText("设备上传日志");
            holder.item_date.setText("日志下载地址: " + fileUrl);
            holder.item_date.setTextIsSelectable(true);
        } else if (msgType.equals("low_battery")) {
            holder.item_date.setTextIsSelectable(false);
            holder.item_date.setText("");
            holder.message_image_layout.setVisibility(View.GONE);
            holder.item_thumbnail_video.setVisibility(View.GONE);
            holder.content_device.setText("低电量报警");
        }

        ViewHolder finalHolder = holder;
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalHolder.swipView.smoothClose();
                if (null != mListener) {
                    mListener.onItemDeleteClicked(position);
                }
            }
        });

        return convertView;
    }

    private void loadFileUrl(String fileUrl, ImageView imgView) {
        AppUtils.d("fileUrl= " + fileUrl);
        Glide.with(mContext.getApplicationContext())
                .load(fileUrl)
                .error(R.mipmap.ic_launcher_round)
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        AppUtils.d("onLoadFailed= " + e.toString());
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgView);
    }

    public void setOnItemDeleteClickListener(OnItemDeleteClickListener listener) {
        this.mListener = listener;
    }

    static class ViewHolder {
        public TextView content_device;
        public ImageView mImgView;
        public ImageView item_thumbnail_video;
        public TextView item_date;
        public LinearLayout item_time_layout;
        public RelativeLayout message_image_layout;
        public Button btnDelete;
        public SwipeMenuLayout swipView;
    }

    public interface OnItemDeleteClickListener {
        void onItemDeleteClicked(int position);
    }
}
