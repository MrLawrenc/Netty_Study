package com.gtdq.netty.netty.random_access_file;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * @author : LiuMingyao
 * @date : 2019/8/12 16:49
 * @description : TODO
 */
public class RandomAccesFileDemo {

    public static void main(String[] args) throws Exception{
        File file = new File("E:\\project\\study_netty\\src\\main\\java\\com\\gtdq\\netty\\netty06_file\\client\\liu.txt");


//        RandomAccessFile randomAccessFile=new RandomAccessFile(netty06_file,"r");
//        int start=0;
//        testRandomAccessFile(start,randomAccessFile);

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        int start = 0;
        testRandomAccessFile1(start, randomAccessFile);
    }
    /**
     * @author : LiuMing
     * @date : 2019/8/12 16:32
     * @description :   每次读一个字节
     */
    public static void testRandomAccessFile(int start, RandomAccessFile randomAccessFile) {
        try {
            System.out.println("文件长度是:" + randomAccessFile.length());

            byte[] bytes = new byte[1];
            int read = randomAccessFile.read(bytes);
            if (read != -1) {
                System.out.println((char) bytes[0]);
                randomAccessFile.seek(start++);
                testRandomAccessFile(start, randomAccessFile);
            } else {
                System.out.println("文件读取完毕!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/12 16:32
     * @description :   一次性全部读写，如果刚好是文件那么多的字节数组则一切正常，如果故意多读一个字节，会出现null的情况
     */
    public static void testRandomAccessFile1(int start, RandomAccessFile randomAccessFile) {
        try {

            byte[] bytes = new byte[(int) randomAccessFile.length()+1];//故意多读一个字节，会出现null的问题
            System.out.println("文件长度是:" + randomAccessFile.length() + "   " + bytes.length);
            int read = randomAccessFile.read(bytes);
            if (read != -1) {
                File file = new File("E://tmp//liu11.txt");
                RandomAccessFile r1 = new RandomAccessFile(file, "rw");
                r1.write(bytes);
                System.out.println("文件写完毕!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/12 16:42
     * @description :   先读出部分字节，再读剩下的字节，最后组合成一个byte数组写入文件（此操作不会缺失和乱码）
     */

    public static void testRandomAccessFile2(int start, RandomAccessFile randomAccessFile) {
        try {

            byte[] bytes0 = new byte[2];
            byte[] bytes = new byte[(int) randomAccessFile.length() - 2];
            System.out.println("文件长度是:" + randomAccessFile.length() + "   " + bytes.length);


            //先写2个字节，再把剩下的字节全部写完
            randomAccessFile.seek(0);
            randomAccessFile.read(bytes0);

            randomAccessFile.seek(2);
            int read = randomAccessFile.read(bytes);


            if (read != -1) {
                File file = new File("E://tmp//liu2.txt");
                RandomAccessFile r1 = new RandomAccessFile(file, "rw");
                byte byte2[] = new byte[bytes0.length + bytes.length];
                for (int i = 0; i < bytes0.length + bytes.length; i++) {
                    if (i < bytes0.length) {
                        byte2[i] = bytes0[i];
                    } else {
                        byte2[i] = bytes[i - 2];
                    }

                }
                r1.write(byte2);
                System.out.println("文件写完毕!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/12 16:42
     * @description :   先读出部分字节，写入文件，再读剩下的字节，再写入文件,这种情况也不会乱码，也不会缺失
     * <p>重点掌握这种情况，在netty中断点续传可以使用</p>
     */

    public static void testRandomAccessFile3(int start, RandomAccessFile randomAccessFile) {
        try {

            //文件是这种格式的:a我awn        先读两个字节会读取到汉字的一部分字节
            byte[] bytes0 = new byte[2];
            byte[] bytes = new byte[(int) randomAccessFile.length() - 2];
            System.out.println("文件长度是:" + randomAccessFile.length());


            //先写2个字节，再把剩下的字节全部写完
            randomAccessFile.seek(0);
            randomAccessFile.read(bytes0);

            File file = new File("E://tmp//liu3.txt");
            RandomAccessFile r1 = new RandomAccessFile(file, "rw");
            r1.write(bytes0);

            randomAccessFile.seek(2);
            int read = randomAccessFile.read(bytes);
            r1.seek(2);
            r1.write(bytes);

            System.out.println("文件写完毕!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}