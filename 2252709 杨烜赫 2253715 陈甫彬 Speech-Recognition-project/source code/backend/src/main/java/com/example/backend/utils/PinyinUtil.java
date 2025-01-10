package com.example.backend.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtil {
    /**
     * 获取中文全拼
     *
     * @param name 需要转换的中文
     * @return 全拼结果
     **/
    public static String getFullPinyin(String name) {
        // 配置拼音输出格式
        HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
        outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE); // 小写拼音
        outputFormat.setToneType(HanyuPinyinToneType.WITH_TONE_MARK); // 带声调符号
        outputFormat.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE); // 使用 Unicode 编码的 ü

        // 用于存储拼音结果
        StringBuilder result = new StringBuilder();

        // 将输入的字符串转换为字符数组，逐个处理
        char[] charArray = name.toCharArray();

        // 遍历每个字符
        for (char c : charArray) {
            // 如果字符是汉字
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                String[] pinyinArray = new String[0];
                try {
                    // 获取汉字的拼音（可能有多种拼音），返回拼音数组
                    pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, outputFormat);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    // 异常处理，如果发生错误，打印错误信息
                    System.out.println("Error processing character: " + c + " - " + e.getMessage());
                }

                // 如果拼音数组不为空，取第一个拼音（通常是最常用的拼音）
                if (pinyinArray != null && pinyinArray.length > 0) {
                    result.append(pinyinArray[0]);  // 将拼音追加到结果中
                }
            } else {
                // 如果是非汉字字符，直接追加到结果中
                result.append(c);
            }

            // 每个拼音或非汉字字符之间添加一个空格，便于分隔观察
            result.append(" ");
        }

        // 去掉结果字符串最后一个多余的空格
        if (!result.isEmpty()) {
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }

    public static void main(String[] args) {
        System.out.println(getFullPinyin("这"));
    }

}
