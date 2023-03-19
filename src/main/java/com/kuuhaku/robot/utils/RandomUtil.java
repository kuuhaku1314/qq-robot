package com.kuuhaku.robot.utils;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author by kuuhaku
 * @Date 2021/4/24 20:26
 * @Description 判断是否通过概率判断
 */
public class RandomUtil {

    /**
     * 通过概率
     *
     * @param probability 概率百分比
     * @return 是否通过
     */
    public static boolean isPass(int probability) {
        int nextInt = new Random(System.currentTimeMillis()).nextInt(100) + 1;
        return nextInt <= probability;
    }

    /**
     * 获取随机数0-max-1
     *
     * @param max 右边边界
     * @return 随机数
     */
    public static int random(int max) {
        return RandomUtils.nextInt(0, max);
    }

    /**
     * 获取string参数的数字值，若不是指定范围内的值，则返回默认值
     *
     * @param str          需要校验的值
     * @param min          最小值
     * @param max          最大值
     * @param defaultValue 默认值
     * @return int值
     */
    public static int ParseIntWithRange(String str, int min, int max, int defaultValue) {
        int i = 0;
        boolean isFind = false;
        if (StringUtils.isNumeric(str)) {
            int anInt = Integer.parseInt(str);
            if (min <= anInt && anInt <= max) {
                i = anInt;
                isFind = true;
            }
        }
        return isFind ? i : defaultValue;
    }

    /**
     * 效率很一般，但不占内存
     *
     * @param min
     * @param max
     * @param size
     * @param isDifferent
     * @return
     */
    public static int[] generateNumbers(int min, int max, int size, boolean isDifferent) {
        if (size <= 0 || max < min) {
            throw new RuntimeException("invalid param");
        }
        if (!isDifferent) {
            return new Random(System.currentTimeMillis()).ints(min, max + 1)
                    .limit(size).toArray();
        }
        int maxSize = max - min + 1;
        if (maxSize < size) {
            throw new RuntimeException("invalid param");
        }
        boolean userEliminationFlag = false;
        int generateSize = size;
        if (maxSize / 2 < size) {
            userEliminationFlag = true;
            generateSize = maxSize - size;
        }
        Set<Integer> numberSet = new HashSet<>((int) (generateSize / 0.75f + 1));
        Random random = new Random(System.nanoTime());
        for (int i = 0; i < generateSize; i++) {
            int number = random.nextInt(max - min + 1) + min;
            while (numberSet.contains(number)) {
                number++;
                if (number > max) {
                    number = min;
                }
            }
            numberSet.add(number);
        }
        // 正向生成
        if (!userEliminationFlag) {
            Integer[] numbers = numberSet.toArray(new Integer[0]);
            return Arrays.stream(numbers).mapToInt(Integer::intValue).toArray();
        }
        // 排除法生成
        int[] results = new int[size];
        for (int i = min, j = 0; i <= max && j < size; i++) {
            if (!numberSet.contains(i)) {
                results[j] = i;
                j++;
            }
        }
        return results;
    }

    /**
     * 随机指定范围内N个不重复的数
     * 在初始化的无重复待选数组中随机产生一个数放入结果中，
     * 将待选数组被随机到的数，用待选数组(len-1)下标对应的数替换
     * 然后从len-2里随机产生下一个随机数，如此类推，效率高，但范围大内存要炸
     *
     * @param max 指定范围最大值
     * @param min 指定范围最小值
     * @param n   随机数个数
     * @return int[] 随机数结果集
     */
    public static int[] randomArray(int min, int max, int n) {
        int len = max - min + 1;

        if (max < min || n > len) {
            return null;
        }

        //初始化给定范围的待选数组
        int[] source = new int[len];
        for (int i = min; i < min + len; i++) {
            source[i - min] = i;
        }

        int[] result = new int[n];
        Random rd = new Random();
        int index = 0;
        for (int i = 0; i < result.length; i++) {
            //待选数组0到(len-2)随机一个下标
            index = Math.abs(rd.nextInt() % len--);
            //将随机到的数放入结果集
            result[i] = source[index];
            //将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换
            source[index] = source[len];
        }
        return result;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        //generateNumbers(1,10000,1000, true);
        Arrays.stream(generateNumbers(1, 50, 40, true)).forEach(System.out::println);
        // randomArray(1, 20000000, 19990000);
        System.out.println(System.currentTimeMillis() - start);
    }
}
