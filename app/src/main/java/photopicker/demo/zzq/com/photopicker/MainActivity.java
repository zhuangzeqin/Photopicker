package photopicker.demo.zzq.com.photopicker;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

import photopicker.demo.zzq.com.photopicker.utils.ImageLoader;
import photopicker.demo.zzq.com.photopicker.view.CircleImageView;

public class MainActivity extends Activity {

    private Button btn_photo;

    private final int PICK_PHOTO = 1000;

    private final int PICK_HEAD = 1001;

    private GridView gridView ;//多张选择图片显示

    private CircleImageView imageView;//圆形头像显示



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = (GridView) findViewById(R.id.gridView);
        imageView=(CircleImageView)findViewById(R.id.civ_headPortrait);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PhotoPickerActivity.class);
                intent.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA, true);//是否显示照相机
                intent.putExtra(PhotoPickerActivity.EXTRA_SELECT_MODE, PhotoPickerActivity.MODE_SINGLE);
                intent.putExtra(PhotoPickerActivity.EXTRA_MAX_MUN, 1);
                startActivityForResult(intent, PICK_HEAD);
            }
        });

        btn_photo = (Button) this.findViewById(R.id.btn_photo);
        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PhotoPickerActivity.class);
                intent.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA, true);//是否显示照相机
                intent.putExtra(PhotoPickerActivity.EXTRA_SELECT_MODE, PhotoPickerActivity.MODE_MULTI);
                intent.putExtra(PhotoPickerActivity.EXTRA_MAX_MUN, PhotoPickerActivity.DEFAULT_NUM);
                startActivityForResult(intent, PICK_PHOTO);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_PHOTO) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> result = data.getStringArrayListExtra(PhotoPickerActivity.KEY_RESULT);
                //do what you want to to.
                for (String path : result) {
                    Log.i("MainActivity", path);
                }
                Toast.makeText(this,result.size()+"",Toast.LENGTH_SHORT).show();
            }
        }
        else if (PICK_HEAD==requestCode)
        {
            if (resultCode == RESULT_OK) {
                ArrayList<String> result = data.getStringArrayListExtra(PhotoPickerActivity.KEY_RESULT);
                //do what you want to to.
                if (result!=null && result.size()>0)
                {
                    String imagePaht = result.get(0);//头像选择照片回来的路径
                    ImageLoader.getInstance().display(imagePaht,imageView,80,80);
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
