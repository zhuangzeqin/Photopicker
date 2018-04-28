package photopicker.demo.zzq.com.photopicker.beans;

import java.io.Serializable;
 /**
  * 描述：照片实体
  * 作者：zhuangzeqin
  * 时间: 2018/4/28-16:16
  * 邮箱：zzq@eeepay.cn
  */
public class Photo implements Serializable {

    private int id;
    private String path;  //路径

    public Photo(String path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id=" + id +
                ", path='" + path + '\'' +
                '}';
    }
}
