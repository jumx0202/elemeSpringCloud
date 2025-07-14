package org.example.captcha.service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

public class CaptchaImageGenerator {

    private static final Color[] COLORS = {Color.BLACK, Color.BLUE, Color.RED, Color.decode("#006600"), Color.MAGENTA, Color.DARK_GRAY};
    private static final Random random = new Random();

    // 用给定的验证码字符串、宽度和高度生成验证码jpg图片，放入一个字节流中返回
    public static byte[] generateJpegImg(String text, int width, int height) throws IOException {

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();

        // 填充背景
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);

        // 设置字体
        int fontSize= height - 5;
        graphics.setFont(new Font("Serif", Font.BOLD, fontSize));

        // 计算字符所在的Y坐标
        int posY = height/2 + fontSize/2 - 5;

        // 画出随机字符系列
        for (int i = 0; i < text.length(); i++) {
            // 取出第i个子字符串
            String str = text.substring(i, i + 1);
            // 随机颜色
            graphics.setColor(COLORS[random.nextInt(COLORS.length)]);
            // 随机位置 X
            int posX = Math.min(20 * i + random.nextInt(8)+5, width-10);
            // 随机旋转角度 -30 ~ 30 度
            double angle = Math.toRadians(random.nextInt(60) - 30);
            graphics.rotate(angle, posX, posY);
            // 绘制字符
            graphics.drawString(str, posX,  posY);
            // 旋转回去
            graphics.rotate(-angle, posX, posY);
        }

        // 添加一些干扰线
        for (int i = 0; i < 15; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(30);
            int yl = random.nextInt(30);
            graphics.setColor(COLORS[random.nextInt(COLORS.length)]);
            graphics.drawLine(x, y, x + xl, y + yl);
        }
        // 释放资源
        graphics.dispose();

        // 将图片写入字节流
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

}