package com.example.backend.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static com.example.backend.utils.PinyinUtil.getFullPinyin;

public class ImageProcessUtil {
    public static final int MIN_CHAR_WIDTH = 55; // 最小字符宽度

    private static String removePunctuation(String pinyin) {
        // 去除所有标点符号，包括音调符号
        return pinyin.replaceAll("[\\p{P}\\p{S}]", "");
    }

    public static String imgToBase64(String hint) {
        // 首先获取拼音数组用于计算宽度
        String pinyinText = getFullPinyin(hint);
        String[] pinyinArray = pinyinText.split(" ");

        // 去除拼音中的标点符号
        for (int i = 0; i < pinyinArray.length; i++) {
            pinyinArray[i] = removePunctuation(pinyinArray[i]);
        }

        // 创建临时图像用于计算文字宽度
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D tempG2d = tempImage.createGraphics();
        Font pinyinFont = new Font("Arial", Font.PLAIN, 20);
        tempG2d.setFont(pinyinFont);

        // 计算每个字符需要的宽度
        int[] charWidths = new int[hint.length()];
        int totalWidth = 20; // 左边距
        for (int i = 0; i < hint.length(); i++) {
            String pinyin = (i < pinyinArray.length) ? pinyinArray[i] : "";
            int pinyinWidth = tempG2d.getFontMetrics().stringWidth(pinyin);
            charWidths[i] = Math.max(MIN_CHAR_WIDTH, pinyinWidth + 10); // 确保至少有MIN_CHAR_WIDTH的宽度
            totalWidth += charWidths[i];
        }
        totalWidth += 20; // 右边距
        tempG2d.dispose();

        // 设置图像宽高
        int height = 100;
        BufferedImage image = new BufferedImage(totalWidth, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 启用抗锯齿和高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 设置背景颜色为白色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, totalWidth, height);

        // 设置中文字体为楷体
        Font chineseFont = new Font("楷体", Font.BOLD, 28);
        g2d.setFont(chineseFont);

        // 设置文字颜色为黑色
        g2d.setColor(Color.BLACK);

        int x = 20; // 起始横坐标
        int y = 40; // 起始纵坐标

        for (int i = 0; i < hint.length(); i++) {
            String character = String.valueOf(hint.charAt(i));
            String pinyin = (i < pinyinArray.length) ? pinyinArray[i] : "";

            // 计算拼音的居中位置
            g2d.setFont(pinyinFont);
            int pinyinWidth = g2d.getFontMetrics().stringWidth(pinyin);
            int pinyinX = x + (charWidths[i] - pinyinWidth) / 2;
            g2d.drawString(pinyin, pinyinX, y);

            // 计算汉字的居中位置
            g2d.setFont(chineseFont);
            int charWidth = g2d.getFontMetrics().stringWidth(character);
            int charX = x + (charWidths[i] - charWidth) / 2;
            g2d.drawString(character, charX, y + 30);

            // 更新 x 坐标
            x += charWidths[i];
        }

        // 释放 Graphics2D 资源
        g2d.dispose();

        // 将 BufferedImage 转换为 Base64 字符串
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            baos.close();
            return base64Image;
        } catch (Exception e) {
            System.out.println("Failed to convert image to base64: " + e.getMessage());
        }
        return null;
    }

    static public void main(String[] args) {
        System.out.println(imgToBase64("我的成长？"));
    }
}