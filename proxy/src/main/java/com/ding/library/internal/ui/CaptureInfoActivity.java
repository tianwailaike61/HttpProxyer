package com.ding.library.internal.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ding.library.internal.utils.CacheUtils;
import com.ding.library.internal.utils.GetCaptureDataUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.http.proxy.R;

import java.util.List;

/**
 * author:DingDeGao
 * time:2019-10-31-15:40
 * function: CaptureInfoActivity
 */
public class CaptureInfoActivity extends AppCompatActivity implements GetCaptureDataUtils.CallBackSlide {

    private DrawerLayout drawerLayout;
    private CaptureContentAdapter contentAdapter;
    private RecyclerView rvList;

    private final CacheUtils.ICacheListener listener = new CacheUtils.ICacheListener() {
        @Override
        public void onClear() {
            GetCaptureDataUtils.getSlidData(CaptureInfoActivity.this);
            if (contentAdapter != null) {
                contentAdapter.clear();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_slide);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Capture");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CacheUtils.getInstance().cleanCache();
                Snackbar.make(view, R.string.clear_log, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
        CacheUtils.getInstance().addObserver(listener);
        initView();
    }

    private void initView() {
        rvList = findViewById(R.id.rvList);
        RecyclerView rvContent = findViewById(R.id.rvContent);
        drawerLayout = findViewById(R.id.drawer_layout);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvContent.setLayoutManager(new LinearLayoutManager(this));

        contentAdapter = new CaptureContentAdapter(this);
        rvContent.setAdapter(contentAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        GetCaptureDataUtils.getSlidData(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CacheUtils.getInstance().removeObserver(listener);
    }

    @Override
    public void success(List<UIItemEntity> list) {
        CaptureUIAdapter captureUIAdapter = new CaptureUIAdapter(list, contentAdapter);
        rvList.setAdapter(captureUIAdapter);
        drawerLayout.openDrawer(GravityCompat.START);
        if (list.size() > 0 && list.get(0).subFileList != null
                && list.get(0).subFileList.size() > 0) {

            GetCaptureDataUtils.getData(this, list.get(0).name, list.get(0).subFileList.get(0).name,
                    new GetCaptureDataUtils.CallBack() {
                        @Override
                        public void success(List<CaptureContentAdapter.Entity> list) {
                            contentAdapter.setData(list);
                        }
                    });
        }
    }
}
