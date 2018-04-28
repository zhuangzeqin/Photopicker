package photopicker.demo.zzq.com.photopicker.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import photopicker.demo.zzq.com.photopicker.beans.Photo;
import photopicker.demo.zzq.com.photopicker.beans.PhotoFloder;


/**
 * 操作图片工具类
 * @Class: PhotoUtils
 * @Description:
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/4
 */
public class PhotoUtils {
	/**
     * 利用ContentProvider扫描手机中的图片;
     * 不同文件夹下的图片;
     * key:文件夹路径
     * value:相片文件夹实体类
     * @param context
     * @return map 不同文件夹下的图片
     */
    public static Map<String, PhotoFloder> getPhotos(Context context) {
        Map<String, PhotoFloder> floderMap = new HashMap<String, PhotoFloder>();

        String allPhotosKey = "所有图片";
        PhotoFloder allFloder = new PhotoFloder();//相片文件夹实体类
        allFloder.setName(allPhotosKey);//文件夹名
        allFloder.setDirPath(allPhotosKey);//文件夹路径
        allFloder.setPhotoList(new ArrayList<Photo>());//该文件夹下图片列表
        floderMap.put(allPhotosKey, allFloder);//放到map 里
        /** 开始扫描图片 **/
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = context.getContentResolver();

        // 只查询jpeg和png的图片
        Cursor mCursor = mContentResolver.query(imageUri, null,
                MediaStore.Images.Media.MIME_TYPE + " in(?, ?)",
                new String[] { "image/jpeg", "image/png" },
                MediaStore.Images.Media.DATE_MODIFIED + " desc");

        int pathIndex = mCursor
                .getColumnIndex(MediaStore.Images.Media.DATA);

        mCursor.moveToFirst();
        while (mCursor.moveToNext()) {
            // 获取图片的路径
            String path = mCursor.getString(pathIndex);

            // 获取该图片的父路径名
            File parentFile = new File(path).getParentFile();
            if (parentFile == null) {
                continue;
            }
            String dirPath = parentFile.getAbsolutePath();//图片的父路径

            if (floderMap.containsKey(dirPath)) {//如果存在，将图片放入图片文件夹中
                PhotoFloder photoFloder = floderMap.get(dirPath);
                Photo photo = new Photo(path);
                photoFloder.getPhotoList().add(photo);//将图片放入图片文件夹中
                /**  添加到所有的集合中*/
                floderMap.get(allPhotosKey).getPhotoList().add(photo);
                continue;
            } else {
                // 初始化imageFloder;添加一个新的图片文件夹组进来
                PhotoFloder photoFloder = new PhotoFloder();//图片文件夹
                List<Photo> photoList = new ArrayList<Photo>();//图片集合
                Photo photo = new Photo(path);
                photoList.add(photo);//添加到集合
                //图片文件夹底下图片集合
                photoFloder.setPhotoList(photoList);
                //图片文件夹路径
                photoFloder.setDirPath(dirPath);
                //图片文件夹名称
                photoFloder.setName(dirPath.substring(dirPath.lastIndexOf(File.separator) + 1, dirPath.length()));
                floderMap.put(dirPath, photoFloder);//将图片放入图片新的文件夹中(key:路径地址；value://图片文件夹实体类)
                /**  添加到所有的集合中*/
                floderMap.get(allPhotosKey).getPhotoList().add(photo);
            }
        }
        mCursor.close();
        return floderMap;
    }

}
