package com.comaiot.comaiotsdktest.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.comaiot.comaiotsdktest.R;
import com.comaiot.net.library.bean.AppQuerySharedDeviceEntity;

import java.util.List;

public class SharedUsrListAdapter extends BaseAdapter {

    private Context mContext;
    private List<AppQuerySharedDeviceEntity.ShareUser> mList;

    public SharedUsrListAdapter(Context context, List<AppQuerySharedDeviceEntity.ShareUser> shareUsers) {
        this.mContext = context;
        this.mList = shareUsers;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public AppQuerySharedDeviceEntity.ShareUser getItem(int position) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_shared_usr_list, null);

            holder.shareName = convertView.findViewById(R.id.share_name);
            holder.sharePhoneNumber = convertView.findViewById(R.id.share_phone_number);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppQuerySharedDeviceEntity.ShareUser shareUser = mList.get(position);
        holder.shareName.setText(shareUser.getNickname() == null ? "" : shareUser.getNickname());
        holder.sharePhoneNumber.setText(shareUser.getPhone_num() == null ? "" : shareUser.getPhone_num());

        return convertView;
    }

    static class ViewHolder {
        TextView shareName;
        TextView sharePhoneNumber;
    }
}
