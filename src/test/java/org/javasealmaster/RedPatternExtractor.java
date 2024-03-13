package org.javasealmaster;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class RedPatternExtractor {
    public static void main(String[] args) {
        // 加载OpenCV库
        System.setProperty("java.awt.headless", "false");
        System.out.println(System.getProperty("java.library.path"));
        System.load("D:\\opencv\\build\\java\\x64\\opencv_java490.dll");

        // 读取图像
        Mat image = Imgcodecs.imread("C:\\Users\\OS\\Pictures\\2.jpg");

        // 提取红色图案
        List<Mat> redPatterns = extractRedPattern(image);

        // 生成红色图案的最小矩形图片
        for (int i = 0; i < redPatterns.size(); i++) {
            Mat pattern = redPatterns.get(i);
            // 保存结果图像
            Imgcodecs.imwrite("C:\\Users\\OS\\Pictures\\out\\9-"+i+".png", pattern);
        }

        System.out.println("红色图案提取完成。");

        String imagePath = "C:\\Users\\OS\\Pictures\\out\\9-0.png";
        String shape = detectShape(imagePath);
        System.out.println("形状: " + shape);
    }

    public static List<Mat> extractRedPattern(Mat inputImage) {
        // 转换图像到HSV颜色空间
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(inputImage, hsvImage, Imgproc.COLOR_BGR2HSV);

        // 定义红色范围
        Scalar lowerRed = new Scalar(0, 100, 100);
        Scalar upperRed = new Scalar(10, 255, 255);

        // 根据红色范围创建掩码
        Mat mask1 = new Mat();
        Mat mask2 = new Mat();
        Core.inRange(hsvImage, lowerRed, upperRed, mask1);

        Scalar lowerRed2 = new Scalar(160, 100, 100);
        Scalar upperRed2 = new Scalar(179, 255, 255);
        Core.inRange(hsvImage, lowerRed2, upperRed2, mask2);

        Mat mask = new Mat();
        Core.add(mask1, mask2, mask);

        // 使用形态学操作去除噪音
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);

        Mat kernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10, 10));
        Imgproc.dilate(mask, mask, kernel2);

        // 在原始图像上绘制红色图案区域
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<Mat> redPatterns = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            // 获取最小外接矩形
            RotatedRect rect = Imgproc.minAreaRect(contour2f);
            // 将矩形区域提取出来
            Mat pattern = new Mat();
            Imgproc.getRectSubPix(inputImage, rect.size, rect.center, pattern);
            redPatterns.add(pattern);
        }

        return redPatterns;
    }

    public static String detectShape(String imagePath) {

        // 读取图像
        Mat image = Imgcodecs.imread(imagePath);

        // 将图像转换为灰度图像
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // 对图像进行模糊处理
        Mat blurredImage = new Mat();
        Imgproc.GaussianBlur(grayImage, blurredImage, new Size(5, 5), 0);

        // 进行边缘检测
        Mat edges = new Mat();
        Imgproc.Canny(blurredImage, edges, 50, 150);

        // 查找轮廓
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 遍历每个轮廓
        for (MatOfPoint contour : contours) {
            // 多边形逼近轮廓
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            double epsilon = 0.02 * Imgproc.arcLength(curve, true);
            Imgproc.approxPolyDP(curve, approxCurve, epsilon, true);

            // 进行椭圆拟合
            if (approxCurve.total() >= 5) {
                RotatedRect ellipse = Imgproc.fitEllipse(approxCurve);

                // 计算椭圆的长轴和短轴
                double majorAxis = Math.max(ellipse.size.width, ellipse.size.height);
                double minorAxis = Math.min(ellipse.size.width, ellipse.size.height);

                // 判断形状是圆形还是椭圆
                double aspectRatio = majorAxis / minorAxis;
                if (aspectRatio >= 0.9 && aspectRatio <= 1.1) {
                    return "圆形";
                } else {
                    return "椭圆";
                }
            }
        }

        // 如果没有找到轮廓，则无法确定形状
        return "未知";
    }


}