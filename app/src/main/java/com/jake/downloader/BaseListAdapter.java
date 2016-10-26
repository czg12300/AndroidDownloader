package com.jake.downloader;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：listView或者gridView的adapter基类
 *
 * @author jake
 * @since 2016/9/17 12:56
 */

public abstract class BaseListAdapter<Entity, ViewHolder extends BaseViewHolder> extends BaseAdapter {

    protected Context mContext;

    protected List<Entity> mList = new ArrayList<Entity>();

    public BaseListAdapter(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public Entity getItem(int position) {
        return mList == null ? null : mList.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder v;
        if (convertView == null) {
            v = onCreateViewHolder(viewGroup, getItemViewType(position));
            convertView = v.itemView;
            convertView.setTag(convertView.getId(), v);
        } else {
            v = (ViewHolder) convertView.getTag(convertView.getId());
        }
        onBindViewHolder(v, position);
        return convertView;
    }

    /**
     * 绑定数据
     *
     * @param holder
     * @param position
     */
    public abstract void onBindViewHolder(ViewHolder holder, int position);

    /**
     * 创建viewHolder
     *
     * @param parent
     * @param viewType
     * @return
     */
    public abstract ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    /**
     * 获取所有数据
     *
     * @return
     */
    public List<Entity> getDatas() {
        return mList;
    }


    /**
     * 添加新数据到末尾
     *
     * @param data
     */
    public void add(Entity data) {
        if (data != null) {
            checkDataIsNull();
            mList.add(data);
        }
    }

    private void checkDataIsNull() {
        if (mList == null) {
            mList = new ArrayList<>();
        }
    }

    /**
     * 添加数据到指定位置
     *
     * @param position
     * @param data
     */
    public void add(int position, Entity data) {
        if (data != null) {
            checkDataIsNull();
            mList.add(position, data);
        }
    }

    /**
     * 添加新数据到末尾
     *
     * @param datas
     */
    public void add(List<Entity> datas) {
        if (isNotNull(datas)) {
            checkDataIsNull();
            mList.addAll(datas);
        }
    }

    /**
     * 添加数据到指定位置
     *
     * @param position
     * @param data
     */
    public void add(int position, List<Entity> data) {
        if (isNotNull(data)) {
            checkDataIsNull();
            mList.addAll(position, data);
        }
    }

    /**
     * 清空列表，
     * 放入新数据
     *
     * @param data
     */
    public void set(Entity data) {
        if (isNotNull(mList)) {
            mList.clear();
        } else {
            mList = new ArrayList<>();
        }
        if (isNotNull(data)) {
            mList.add(data);
        }
    }


    /**
     * 清空列表，
     * 放入新数据
     *
     * @param datas
     */
    public void set(List<Entity> datas) {
        if (isNotNull(datas)) {
            mList = datas;
        }
    }


    /**
     * 删除指定的位置数据
     *
     * @param position
     * @return
     */
    public Entity del(int position) {
        if (isNotNull(mList) && position < mList.size()) {
            Entity t = mList.remove(position);
            return t;
        }
        return null;
    }


    public void clear() {
        if (isNotNull(mList)) {
            mList.clear();
        }
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }


    public static boolean isNotNull(Object object) {
        return object != null;
    }
}