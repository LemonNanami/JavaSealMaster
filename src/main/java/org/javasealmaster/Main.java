package org.javasealmaster;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author lei
 * @date 2024/3/12 11:20
 */
public class Main {

    static{
        // 加载OpenCV库
        System.setProperty("java.awt.headless", "false");
        System.out.println(System.getProperty("java.library.path"));
        System.load("opencv_java490.dll");
    }

    public static void main(String[] args) {
        Mat mat = Imgcodecs.imread("INPUT_PATH");
        Utils utils = new Utils();
        List<Mat> mats = utils.extract(mat);
        for (Mat m : mats ){
            Imgcodecs.imwrite("OUT_PATH"+ UUID.randomUUID()+".png", utils.circle2Rectangle(m, 0));
        }
    }

}