package org.javasealmaster;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lei
 * @date 2024/3/12 11:27
 */
public class Utils {

    public static void main(String[] args) {
        // 加载OpenCV库
        System.setProperty("java.awt.headless", "false");
        System.out.println(System.getProperty("java.library.path"));
        System.load("D:\\opencv\\build\\java\\x64\\opencv_java490.dll");




    }

    // 辅助方法：显示图像
    public static void displayImage(Mat image) {
        // 创建一个窗口
        HighGui.namedWindow("test", HighGui.WINDOW_NORMAL);
        HighGui.imshow("test", image);

        // 等待用户关闭窗口
        HighGui.waitKey(0);

        // 销毁窗口
        HighGui.destroyAllWindows();
    }


    public static Det getArea(List<MatOfPoint> contours, int maxIdx, String category) {
        MatOfPoint cnt = contours.get(maxIdx);
        int bx, by, bw, bh, bt;

        if (category.equals("Rectangle")) {
            // 拟合矩形
            RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(cnt.toArray()));
            Point center = rect.center;
            Size size = rect.size;
            double angle = rect.angle;
            int rx = (int) center.x;
            int ry = (int) center.y;
            int rw = (int) size.width;
            int rh = (int) size.height;
            int rt = (int) angle;
            bx = rx;
            by = ry;
            bw = rw;
            bh = rh;
            bt = rt;
        } else if (category.equals("Circle")) {
            // 拟合圆
            MatOfPoint2f cnt2f = new MatOfPoint2f(cnt.toArray());
            Point center = new Point();
            float[] radius = new float[1];
            Imgproc.minEnclosingCircle(cnt2f, center, radius);
            int cx = (int) center.x;
            int cy = (int) center.y;
            int cr = (int) radius[0];
            bx = cx;
            by = cy;
            bw = 2 * cr;
            bh = 2 * cr;
            bt = 0;
        } else {
            // 拟合椭圆
            if (cnt.rows() > 5) {  // 需要5个点以上才能拟合椭圆，否则只能用圆形
                MatOfPoint2f cnt2f = new MatOfPoint2f(cnt.toArray());
                RotatedRect ellipse = Imgproc.fitEllipse(cnt2f);
                Point center = ellipse.center;
                Size size = ellipse.size;
                double angle = ellipse.angle;
                int ex = (int) center.x;
                int ey = (int) center.y;
                int ea = (int) (size.width / 2);
                int eb = (int) (size.height / 2);
                int et = (int) angle;
                int ew = 2 * ea;
                int eh = 2 * eb;
                bx = ex;
                by = ey;
                bw = ew;
                bh = eh;
                bt = et;
            } else {
                MatOfPoint2f cnt2f = new MatOfPoint2f(cnt.toArray());
                Point center = new Point();
                float[] radius = new float[1];
                Imgproc.minEnclosingCircle(cnt2f, center, radius);
                int ex = (int) center.x;
                int ey = (int) center.y;
                int ea = (int) radius[0];
                int eb = (int) radius[0];
                int et = 0;
                int ew = 2 * ea;
                int eh = 2 * eb;
                bx = ex;
                by = ey;
                bw = ew;
                bh = eh;
                bt = et;
            }
        }


        return new Det(category, new Box(bx, by, bw, bh, bt));
    }

    public String filling(Mat image) {
        try{
            // 查找轮廓
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

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

            // 椭圆拟合
            MatOfPoint2f curve = new MatOfPoint2f(contours.get(maxContourIdx).toArray());
            RotatedRect ellipse = Imgproc.fitEllipse(curve);

            // 获取椭圆的长轴和短轴长度
            double majorAxis = Math.max(ellipse.size.width, ellipse.size.height);
            double minorAxis = Math.min(ellipse.size.width, ellipse.size.height);

            // 判断图案是椭圆还是圆形
            double threshold = 1.2;  // 长轴和短轴长度的差异阈值
            if (majorAxis / minorAxis < threshold) {
                System.out.println("Circle");
                return "Circle";
            } else {
                System.out.println("Ellipse");
                return "Ellipse";
            }
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 提取图像
     *
     * @param image
     * @return
     */
    public List<Mat> extract(Mat image){
        //放大原图
        Core.copyMakeBorder(image,image,
                (int) (image.size().width*0.5),
                (int) (image.size().width*0.5),
                (int) (image.size().height*0.5),
                (int) (image.size().height*0.5),
                0
                );

        Mat differ;
        differ = redColorExtraction(image);

        Mat erode;
        erode = erodeDilate(differ);

        // 进行边缘检测
        Mat edges = new Mat();
        Imgproc.Canny(erode, edges, 50, 150);

        // 膨胀,模糊掉边缘中断的细节
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.dilate(edges, edges, kernel);

        // 查找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Mat> res = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++) {
            Mat resultImage = new Mat();
            image.copyTo(resultImage);
            Det det = getArea(contours, i, filling(edges));
            resultImage = rotateCut(resultImage, det);
            res.add(resultImage);
        }

        return res;
    }

    public static Mat rotateCut(Mat img, Det det) {
        // 旋转中心，旋转角度，缩放比例
        Mat matRotate = Imgproc.getRotationMatrix2D(new Point(det.getBox().getBx(), det.getBox().getBy()), det.getBox().getBt(), 1);
        Mat imgRotated = new Mat();
        Imgproc.warpAffine(img, imgRotated, matRotate, new Size(img.cols(), img.rows()));

        // 通过中心坐标、长、宽，获取目标的box
        int x = det.getBox().getBx();
        int y = det.getBox().getBy();
        int w = det.getBox().getBw();
        int h = det.getBox().getBh();
        int p1X = x - w / 2;
        int p1Y = y - h / 2;
        int p2X = x + w / 2;
        int p2Y = y + h / 2;
        Rect roi = new Rect(p1X, p1Y, p2X - p1X, p2Y - p1Y);
        Mat imgCropped = new Mat(imgRotated, roi);
        Mat res = new Mat();
        if (imgCropped.rows() < imgCropped.cols()) {
            Core.transpose(img, res);
            Core.flip(res, res, 1);
        } else {
            img.copyTo(res);
        }

        return imgCropped;
    }

    public static Mat redColorExtraction(Mat originalImage) {
        // 创建空白图像，与原始图像大小和类型相同
        Mat resultImage = Mat.zeros(originalImage.size(), originalImage.type());

        // 提取红色区域
        Mat mask = new Mat();
        Core.inRange(originalImage, new Scalar(0, 0, 0), new Scalar(50, 50, 255), mask);

        // 将红色区域复制到结果图像中
        originalImage.copyTo(resultImage, mask);

        Imgproc.cvtColor(resultImage, resultImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(resultImage, resultImage, 0, 255, Imgproc.THRESH_BINARY);

        return resultImage;
    }

    public static Mat erodeDilate(Mat originalImage) {
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Mat resImage = new Mat();
        Imgproc.dilate(originalImage, resImage, kernel);
        Imgproc.erode(resImage, resImage, kernel);
        Imgproc.dilate(resImage, resImage, kernel);

        return resImage;
    }

    public Mat circle2Rectangle(Mat srcImage, double start){
        int x0 = (int) (srcImage.size().height / 2);
        int y0 = (int) (srcImage.size().width / 2);

        int radius = (x0 + y0) / 2;
        int dstHeight = radius;
        int dstWidth = (int) (2 * Math.PI * radius);

        Mat dstImg = new Mat(dstHeight, dstWidth, Imgproc.COLOR_BGR2RGB);

        for (int j = 0; j < dstWidth; j++) {
            // 将j转换成double 防止精度丢失
            double theta = 2 * Math.PI * ((double) j / dstWidth) + 2 * Math.PI * (start / 360);
            for (int i = 0; i < dstHeight; i++) {
                //适应椭圆
                double x = (x0 - i) * Math.cos(theta) + x0;
                double y = (y0 - i) * Math.sin(theta) + y0;
                try{
                    dstImg.put(i, j, srcImage.get((int) x, (int) y));
                }catch (Exception ignored){
                }
            }
        }

        Core.flip(dstImg, dstImg, 1);
        Core.hconcat(List.of(new Mat[]{dstImg, dstImg}), dstImg);
        return dstImg;
    }
}
