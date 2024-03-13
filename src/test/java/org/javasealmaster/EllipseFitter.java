package org.javasealmaster;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EllipseFitter {
    public static String fitting(File file) {
        // 加载OpenCV库
        System.setProperty("java.awt.headless", "false");
        System.out.println(System.getProperty("java.library.path"));
        System.load("D:\\opencv\\build\\java\\x64\\opencv_java490.dll");

        // 读取图像
        Mat image = Imgcodecs.imread("C:\\Users\\OS\\Pictures\\9.png");

        // 转换为灰度图像
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // 进行边缘检测
        Mat edges = new Mat();
        Imgproc.Canny(grayImage, edges, 50, 150);

        // 膨胀,模糊掉边缘中断的细节
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.dilate(edges, edges, kernel);

        // 查找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 找到最大轮廓
        double maxArea = -1;
        int maxContourIdx = -1;
        for (int i = 0; i < contours.size(); i++) {
            double area = Imgproc.contourArea(contours.get(i));
            if (area > maxArea) {
                maxArea = area;
                maxContourIdx = i;
            }
        }

        // 绘制最大轮廓
        Mat resultImage = new Mat();
        image.copyTo(resultImage);

        // 椭圆拟合
        MatOfPoint2f curve = new MatOfPoint2f(contours.get(maxContourIdx).toArray());
        RotatedRect ellipse = Imgproc.fitEllipse(curve);

        // 获取椭圆的长轴和短轴长度
        double majorAxis = Math.max(ellipse.size.width, ellipse.size.height);
        double minorAxis = Math.min(ellipse.size.width, ellipse.size.height);

        // 判断图案是椭圆还是圆形
        double threshold = 1;  // 长轴和短轴长度的差异阈值
        if (majorAxis - minorAxis < threshold) {
            System.out.println("Circle");
            return "Circle";
        } else {
            System.out.println("Ellipse");
            return "Ellipse";
        }
    }
}