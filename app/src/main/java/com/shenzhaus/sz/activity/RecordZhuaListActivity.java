package com.shenzhaus.sz.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.shenzhaus.sz.R;
import com.shenzhaus.sz.adapter.RecordZqRecyclerListAdapter;
import com.shenzhaus.sz.base.BaseActivity;
import com.shenzhaus.sz.common.Api;
import com.shenzhaus.sz.intf.OnRecyclerViewItemClickListener;
import com.shenzhaus.sz.intf.OnRequestDataListener;
import com.shenzhaus.sz.model.DanmuMessage;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;

public class RecordZhuaListActivity extends BaseActivity implements OnRecyclerViewItemClickListener {

    @Bind(R.id.layout_top_title)
    TextView mTextTopTitle;
    @Bind(R.id.layout_top_back)
    ImageView mImageTopBack;

    @Bind(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;
    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerView;
    private String mToken;
    private ArrayList<DanmuMessage> mListData = new ArrayList();
    private RecordZqRecyclerListAdapter mAdapter = new RecordZqRecyclerListAdapter(this,mListData);

    @OnClick(R.id.layout_top_back)
    public void back(View v){
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextTopTitle.setText(getResources().getString(R.string.zhuaqu_record));
        mToken = SPUtils.getInstance().getString("token");
        mRefreshLayout.autoRefresh();
        initGameList();
    }

    private void initGameList() {
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        //mRecyclerView.addItemDecoration(new SpaceItemDecoration(SizeUtils.dp2px(10)));
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                getGameData(0);
            }
        });
        mRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                getGameData(mListData.size());
            }
        });
        mAdapter.setOnRecyclerViewItemClickListener(this);
    }

    private void getGameData(final int limit_begin) {
        JSONObject params = new JSONObject();
        params.put("token",mToken);
        params.put("limit_begin",limit_begin);
        params.put("limit_num",10);
        Api.getZhuaRecord(this, params, new OnRequestDataListener() {
            @Override
            public void requestSuccess(int code, JSONObject data) {
                if(limit_begin == 0){
                    mListData.clear();
                }
                if(mRefreshLayout.isRefreshing()){
                    mRefreshLayout.finishRefresh();
                }
                if(mRefreshLayout.isLoading()){
                    mRefreshLayout.finishLoadmore();
                }
                JSONArray list = data.getJSONArray("info");
                for (int i = 0; i < list.size(); i++) {
                    DanmuMessage g = new DanmuMessage();
                    JSONObject t = list.getJSONObject(i);
                    g.setUserName(t.getString("name"));
                    g.setAvator(t.getString("img"));
                    g.setUid(t.getString("play_time"));
                    g.setMessageContent(t.getString("play_result"));
                    g.setId(t.getString("smeta"));
                    g.setRemoteUid(t.getString("video_status"));
                    mListData.add(g);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void requestFailure(int code, String msg) {
                toast(msg);
                if(mRefreshLayout.isRefreshing()){
                    mRefreshLayout.finishRefresh();
                }
                if(mRefreshLayout.isLoading()){
                    mRefreshLayout.finishLoadmore();
                }
            }
        });
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_record_zhua_list;
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        DanmuMessage t = mListData.get(position);
        if(t != null && "1".equals(t.getRemoteUid())){
            Bundle d = new Bundle();
            d.putSerializable("item",t);
            ActivityUtils.startActivity(d,VideoPlayerActivity.class);
        }else {
            toast(getString(R.string.record_error));
        }
    }
}