package com.example.m3u8Tools;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    int REQUESTCODE_FROM_ACTIVITY = 1000;
    private VideoListAdapter adapter = null;
    private List<VideoBean> videos = new ArrayList<VideoBean>();
    private String m3u8 = "/index.m3u8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestAppPermissions();
    }

    private void initListView() {
        adapter = new VideoListAdapter(MainActivity.this, R.layout.video_list, videos);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = videos.get(position).url + m3u8;
                File file = new File(url);
                if (file.exists()){
/*
                    Toast.makeText(getApplicationContext(),"本地文件已下载，正在播放中！！！", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,FullScreenActivity.class);
                    intent.putExtra("M3U8_URL", url);
                    startActivity(intent);
*/
                Converter converter = new Converter(videos.get(position).url, videos.get(position).name);
                converter.convertVideo();
                }else {
                    Toast.makeText(getApplicationContext(), "未发现m3u8！！！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.clear_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(REQUESTCODE_FROM_ACTIVITY)
                        .withIconStyle(Constant.ICON_STYLE_GREEN )
                        .withStartPath("/storage/emulated/0/abcplayer")//指定初始显示路径
                        .withChooseMode(false)
                        .withMutilyMode(true)
                        .start();
            }});

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FROM_ACTIVITY) {
                //如果是文件夹选择模式，需要获取选择的文件夹路径
                String path = data.getStringExtra("path");
                // Toast.makeText(getApplicationContext(), "选中的路径为" + path, Toast.LENGTH_SHORT).show();
                VideoBean video = new VideoBean();
                video.name = path.substring(path.lastIndexOf("/") + 1);
                video.url = path;
                videos.add(video);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void requestAppPermissions() {
        Dexter.withActivity(this)
                .withPermissions(PERMISSIONS)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            initListView();
                            Toast.makeText(getApplicationContext(),"权限获取成功",Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(getApplicationContext(),"权限获取失败",Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
                .check();
    }


}

