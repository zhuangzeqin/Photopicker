package photopicker.demo.zzq.com.photopicker;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import photopicker.demo.zzq.com.photopicker.adapters.FloderAdapter;
import photopicker.demo.zzq.com.photopicker.adapters.PhotoAdapter;
import photopicker.demo.zzq.com.photopicker.beans.Photo;
import photopicker.demo.zzq.com.photopicker.beans.PhotoFloder;
import photopicker.demo.zzq.com.photopicker.utils.LogUtils;
import photopicker.demo.zzq.com.photopicker.utils.OtherUtils;
import photopicker.demo.zzq.com.photopicker.utils.PhotoUtils;

 /**
  * 描述：照片选择界面
  * 作者：zhuangzeqin
  * 时间: 2018/4/28-16:16
  * 邮箱：zzq@eeepay.cn
  */
public class PhotoPickerActivity extends Activity implements PhotoAdapter.PhotoClickCallBack {

    public final static String TAG = "PhotoPickerActivity";//TAG 标签
    /**
     * 返回选择图片的key值
     */
    public final static String KEY_RESULT = "picker_result";

    /**
     * 系统相机拍照的请求码
     */
    public final static int REQUEST_CAMERA = 1;

    /**
     * 是否显示相机
     */
    public final static String EXTRA_SHOW_CAMERA = "is_show_camera";
    /**
     * 照片选择模式
     */
    public final static String EXTRA_SELECT_MODE = "select_mode";
    /**
     * 最大选择数量
     */
    public final static String EXTRA_MAX_MUN = "max_num";
    /**
     * 单选
     */
    public final static int MODE_SINGLE = 0;
    /**
     * 多选
     */
    public final static int MODE_MULTI = 1;
    /**
     * 默认最大选择数量
     */
    public final static int DEFAULT_NUM = 9;
    public final static int PHOTO_PERMISS = 100;
    /**
     * 所有图片的key
     */
    private final static String ALL_PHOTO = "所有图片";
    /**
     * 是否显示相机，默认不显示
     */
    private boolean mIsShowCamera = false;
    /**
     * 照片选择模式，默认是单选模式
     */
    private int mSelectMode = 0;
    /**
     * 最大选择数量，仅多选模式有用
     */
    private int mMaxNum;
    /**
     * 显示图片的网格布局
     */
    private GridView mGridView;
    /**
     * 不同文件夹下的图片map
     */
    private Map<String, PhotoFloder> mFloderMap;
    /**
     * 图片集合
     */
    private List<Photo> mPhotoLists = new ArrayList<Photo>();
    /**
     * 图片的选择集合
     */
    private ArrayList<String> mSelectList = new ArrayList<String>();
    /**
     * 图片适配器
     */
    private PhotoAdapter mPhotoAdapter;
    /**
     * 扫描时的加载提示框
     */
    private ProgressDialog mProgressDialog;
    /**
     * 文件夹列表
     */
    private ListView mFloderListView;
    /**
     * 某个文件夹显示图片数量
     * 比如 1024张
     */
    private TextView mPhotoNumTV;
    /**
     * 文件夹名称
     * 比如：全部图片
     */
    private TextView mPhotoNameTV;
    /**
     * 确定按钮
     */
    private Button mCommitBtn;
    /**
     * 文件夹列表是否处于显示状态
     */
    boolean mIsFloderViewShow = false;
    /**
     * 文件夹列表是否被初始化，确保只被初始化一次
     */
    boolean mIsFloderViewInit = false;

    /**
     * 拍照时存储拍照结果的临时文件
     */
    private File mTmpFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全屏无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_photo_picker);

        /** 初始化传过来的选项参数；比如单选，多选；是否显示照相机等 **/
        initIntentParams();
        /** 初始化view 控件对象 **/
        initView();

        if (!OtherUtils.isExternalStorageAvailable()) {
            Toast.makeText(this, "SD card 不可用!", Toast.LENGTH_SHORT).show();
            return;
        }
        /**  获取照片的异步任务 **/
        getPhotosTask.execute();
    }

    /**
     * 初始化view 控件对象
     */
    private void initView() {
        mGridView = (GridView) findViewById(R.id.photo_gridview);
        mPhotoNumTV = (TextView) findViewById(R.id.photo_num);
        mPhotoNameTV = (TextView) findViewById(R.id.floder_name);
        ((RelativeLayout) findViewById(R.id.bottom_tab_bar)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //消费触摸事件，防止触摸底部tab栏也会选中图片
                return true;
            }
        });
        ((ImageView) findViewById(R.id.btn_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 初始化选项参数
     */
    private void initIntentParams() {
        mIsShowCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, false);
        mSelectMode = getIntent().getIntExtra(EXTRA_SELECT_MODE, MODE_SINGLE);
        mMaxNum = getIntent().getIntExtra(EXTRA_MAX_MUN, DEFAULT_NUM);
        if (mSelectMode == MODE_MULTI) {
            //如果是多选模式，需要将确定按钮初始化以及绑定事件
            mCommitBtn = (Button) findViewById(R.id.commit);
            mCommitBtn.setVisibility(View.VISIBLE);
            /** 确定按钮点击事件 **/
            mCommitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectList.addAll(mPhotoAdapter.getmSelectedPhotos());
                    returnData();
                }
            });
        }
    }

    /**
     * 刷新UI
     */
    private void refreshPhotosUI() {
        mProgressDialog.dismiss();
        if (mFloderMap==null) return;
        /**  默认获取所有照片 **/
        mPhotoLists.addAll(mFloderMap.get(ALL_PHOTO).getPhotoList());
        /** 图片数量 **/
        mPhotoNumTV.setText(OtherUtils.formatResourceString(getApplicationContext(),
                R.string.photos_num, mPhotoLists.size()));
        /** 适配器填充 **/
        mPhotoAdapter = new PhotoAdapter(this.getApplicationContext(), mPhotoLists);
        mPhotoAdapter.setIsShowCamera(mIsShowCamera);
        mPhotoAdapter.setSelectMode(mSelectMode);
        mPhotoAdapter.setMaxNum(mMaxNum);
        mPhotoAdapter.setPhotoClickCallBack(this);
        mGridView.setAdapter(mPhotoAdapter);
        /** 循环map 获取key 集合**/
        Set<String> keys = mFloderMap.keySet();
        /** 图片文件夹列表集合 **/
        final List<PhotoFloder> floders = new ArrayList<PhotoFloder>();
        for (String key : keys) {
            if (ALL_PHOTO.equals(key)) {//所有照片
                PhotoFloder floder = mFloderMap.get(key);
                floder.setIsSelected(true);//默认标识选择所有
                floders.add(0, floder);//添加到第一个显示
            } else {//其它的直接添加进来
                floders.add(mFloderMap.get(key));
            }
        }
        /** 选择不同的文件夹名称 **/
        mPhotoNameTV.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                toggleFloderList(floders);
            }
        });
        /**图片列表点击事件  **/
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /**图片列表第一个是调用系统照相机  **/
                if (mPhotoAdapter.isShowCamera() && position == 0) {
                    showCamera();
                    return;
                }
                /** 点击选择某张照片 **/
                selectPhoto(mPhotoAdapter.getItem(position));
            }
        });
    }

    @Override
    protected void onDestroy() {
        mSelectList.clear();
        super.onDestroy();
    }

    /**
     * 点击选择某张照片
     *
     * @param photo
     */
    private void selectPhoto(Photo photo) {
        LogUtils.e(TAG, "selectPhoto");
        if (photo == null) {
            return;
        }
        String path = photo.getPath();
        //如果等于单选默认；直接返回
        if (mSelectMode == MODE_SINGLE) {
            mSelectList.add(path);
            returnData();
        }
    }

    @Override
    public void onPhotoClick() {
        LogUtils.e(TAG, "onPhotoClick");
        List<String> list = mPhotoAdapter.getmSelectedPhotos();
        if (list != null && list.size() > 0) {
            /** 确定 **/
            mCommitBtn.setEnabled(true);
            mCommitBtn.setText(OtherUtils.formatResourceString(getApplicationContext(),
                    R.string.commit_num, list.size(), mMaxNum));
            /** 预览 **/
            mPhotoNumTV.setEnabled(true);
            mPhotoNumTV.setText(OtherUtils.formatResourceString(getApplicationContext(), R.string.preview_num, list.size(), mMaxNum));
        } else {
            /** 确定 **/
            mCommitBtn.setEnabled(false);
            mCommitBtn.setText(R.string.commit);
            /** 预览 **/
            mPhotoNumTV.setEnabled(false);
            /** 图片数量 **/
            mPhotoNumTV.setText(OtherUtils.formatResourceString(getApplicationContext(),
                    R.string.photos_num, list.size()));


        }
    }

    /**
     * 返回选择图片的路径
     */
    private void returnData() {
        // 返回已选择的图片数据
        Intent data = new Intent();
        data.putStringArrayListExtra(KEY_RESULT, mSelectList);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 显示或者隐藏文件夹列表
     *
     * @param floders
     */
    private void toggleFloderList(final List<PhotoFloder> floders) {
        //初始化文件夹列表
        if (!mIsFloderViewInit) {
            ViewStub floderStub = (ViewStub) findViewById(R.id.floder_stub);
            floderStub.inflate();
            View dimLayout = findViewById(R.id.dim_layout);
            mFloderListView = (ListView) findViewById(R.id.listview_floder);
            final FloderAdapter adapter = new FloderAdapter(this, floders);
            mFloderListView.setAdapter(adapter);
            mFloderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    for (PhotoFloder floder : floders) {
                        floder.setIsSelected(false);
                    }
                    PhotoFloder floder = floders.get(position);
                    floder.setIsSelected(true);
                    adapter.notifyDataSetChanged();

                    mPhotoLists.clear();
                    mPhotoLists.addAll(floder.getPhotoList());
                    if (ALL_PHOTO.equals(floder.getName())) {
                        mPhotoAdapter.setIsShowCamera(mIsShowCamera);
                    } else {
                        mPhotoAdapter.setIsShowCamera(false);
                    }
                    //这里重新设置adapter而不是直接notifyDataSetChanged，是让GridView返回顶部
                    mGridView.setAdapter(mPhotoAdapter);
                    mPhotoNumTV.setText(OtherUtils.formatResourceString(getApplicationContext(),
                            R.string.photos_num, mPhotoLists.size()));
                    mPhotoNameTV.setText(floder.getName());
                    toggle();
                }
            });
            dimLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mIsFloderViewShow) {
                        toggle();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            initAnimation(dimLayout);
            mIsFloderViewInit = true;
        }
        toggle();
    }

    /**
     * 弹出或者收起文件夹列表
     */
    private void toggle() {
        if (mIsFloderViewShow) {
            outAnimatorSet.start();
            mIsFloderViewShow = false;
        } else {
            inAnimatorSet.start();
            mIsFloderViewShow = true;
        }
    }


    /**
     * 初始化文件夹列表的显示隐藏动画
     */
    AnimatorSet inAnimatorSet = new AnimatorSet();
    AnimatorSet outAnimatorSet = new AnimatorSet();

    private void initAnimation(View dimLayout) {
        ObjectAnimator alphaInAnimator, alphaOutAnimator, transInAnimator, transOutAnimator;
        //获取actionBar的高
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        /**
         * 这里的高度是，屏幕高度减去上、下tab栏，并且上面留有一个tab栏的高度
         * 所以这里减去3个actionBarHeight的高度
         */
        int height = OtherUtils.getHeightInPx(this) - 3 * actionBarHeight;
        alphaInAnimator = ObjectAnimator.ofFloat(dimLayout, "alpha", 0f, 0.7f);
        alphaOutAnimator = ObjectAnimator.ofFloat(dimLayout, "alpha", 0.7f, 0f);
        transInAnimator = ObjectAnimator.ofFloat(mFloderListView, "translationY", height, 0);
        transOutAnimator = ObjectAnimator.ofFloat(mFloderListView, "translationY", 0, height);

        LinearInterpolator linearInterpolator = new LinearInterpolator();

        inAnimatorSet.play(transInAnimator).with(alphaInAnimator);
        inAnimatorSet.setDuration(300);
        inAnimatorSet.setInterpolator(linearInterpolator);
        outAnimatorSet.play(transOutAnimator).with(alphaOutAnimator);
        outAnimatorSet.setDuration(300);
        outAnimatorSet.setInterpolator(linearInterpolator);
    }

    /**
     * 选择文件夹
     *
     * @param photoFloder
     */
    public void selectFloder(PhotoFloder photoFloder) {
        mPhotoAdapter.setDatas(photoFloder.getPhotoList());
        mPhotoAdapter.notifyDataSetChanged();
    }

    /**
     * 获取照片的异步任务
     */
    private final AsyncTask getPhotosTask = new AsyncTask() {
        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(PhotoPickerActivity.this, null, "loading...");
        }

        @Override
        protected Object doInBackground(Object[] params) {
            requestPhotoPermiss();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            /** 扫描 完成之后；刷新UI**/
            refreshPhotosUI();
        }
    };

    /**
     * 权限的声请
     */
    private void requestPhotoPermiss() {
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedCloseButtonText("关闭")
                .setGotoSettingButtonText("设置")
                .setDeniedMessage("您的功能需要用到相应的权限，\n\n 请到设置里吧权限打开！")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .check();
    }

    /**
     * 权限的监听接口
     */
    private final PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            /** 利用ContentProvider扫描手机中的图片 封装到map中**/
            mFloderMap = PhotoUtils.getPhotos(
                    PhotoPickerActivity.this.getApplicationContext());
            refreshPhotosUI();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(PhotoPickerActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }


    };

    /**
     * 选择相机
     */
    private void showCamera() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            mTmpFile = OtherUtils.createFile(getApplicationContext());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(getApplicationContext(),
                    R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 相机拍照完成后，返回图片路径
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    mSelectList.add(mTmpFile.getAbsolutePath());
                    returnData();
                }
            } else {
                if (mTmpFile != null && mTmpFile.exists()) {
                    mTmpFile.delete();
                }
            }
        }
    }
}
