package com.example.m3u8Tools;

import android.content.Context;
import android.os.FileUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.elvishew.xlog.XLog;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.os.FileUtils.*;
import static com.example.m3u8Tools.FileUtils.readFile;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/11/21
 * 描    述:
 * ================================================
 */
public class Converter {

    //合并后的文件存储目录
    private String dir;

    //合并后的视频文件名称
    private String fileName;

    //解密算法名称
    private String method;

    //密钥
    private String key = "";

    //密钥字节
    private byte[] keyBytes = new byte[16];

    //key是否为字节
    private boolean isByte = true;

    //IV
    private String iv = "";

    private boolean isEncrypt = false;

    //所有ts
    private Set<String> tsSet = new LinkedHashSet<>();

    public Converter(String path, String name) {
        dir = path;
        fileName = name;
        String content = "";
        try {
            content = readFile(path + "/index.m3u8");
        } catch (Exception e) {
        }


        //判断是否是m3u8链接
        if (!content.contains("#EXTM3U"))
            throw new M3u8Exception(path + "不包含m3u8链接！");
        String[] split = content.split("\\n");
        for (String s : split) {
            //如果含有此字段，则获取加密算法以及获取密钥的链接
            if (s.contains("#EXT-X-KEY")) {
                isEncrypt = true;
                String[] split1 = s.split(",");
                for (String s1 : split1) {
                    if (s1.contains("METHOD")) {
                        method = s1.split("=", 2)[1];
                        continue;
                    }
                    if (s1.contains("URI")) {
                        key = s1.split("=", 2)[1];
                        continue;
                    }
                    if (s1.contains("IV"))
                        iv = s1.split("=", 2)[1];
                }
            }


            //将ts片段链接加入set集合
            for (int i = 0; i < split.length; i++) {
                String str = split[i];
                if (str.contains("#EXTINF")) {
                    String s1 = split[++i];
                    int pos = s1.lastIndexOf('/');
                    if (pos == -1) {
                        tsSet.add(s1);
                    } else {
                        tsSet.add(s1.substring(pos + 1));
                    }
                }
            }
        }

        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(dir + "/key.key")));
            dis.read(keyBytes);
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void convertVideo() {
        if (isEncrypt) {
            decryptDir(tsSet);
        }
        mergeTs();
        deleteFiles();
    }

    /**
     * 合并下载好的ts片段
     */
    private void mergeTs() {
        try {
            File file = new File(dir + "/" + fileName + ".mp4");
            System.gc();
            if (file.exists())
                return;
            else file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] b = new byte[4096];
            for (String s : tsSet) {
                try {
                    String filePath;
                    if (isEncrypt) {
                        filePath = dir + "/" + s + "_de";
                    } else {
                        filePath = dir + "/" + s;
                    }
                    Log.d("m3u8TOOls", filePath);
                    XLog.d(filePath);
                    File fileTs = new File(filePath);
                    FileInputStream fileInputStream = new FileInputStream(fileTs);
                    int len;
                    while ((len = fileInputStream.read(b)) != -1) {
                        fileOutputStream.write(b, 0, len);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                }catch (Exception e) {
                    e.printStackTrace();
                    XLog.d(e.toString());
                }
            }
            fileOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            XLog.d(e.toString());
        }
    }

    /**
     * 删除下载好的片段
     */
    private void deleteFiles() {
        File file = new File(dir);
        for (File f : file.listFiles()) {
            if (f.getName().endsWith("_de"))
                f.delete();
        }
    }

    private void decryptDir(Set<String> files) {
        for (String s : files) {
            try {
                File inFile = new File(dir + "/" + s);
                InputStream inputStream1 = new FileInputStream(inFile);
                int available = inputStream1.available();
                byte[] bytes = new byte[available];
                inputStream1.read(bytes);
                byte[] decrypt = decrypt(bytes, available, "0123456789abcdef", "", method);
                File file = new File(dir + "/" + s + "_de");
                XLog.d("[decryptDir] " + dir + "/" + s + "_de");
                OutputStream outputStream1 = new FileOutputStream(file);
                outputStream1.write(decrypt);
                inputStream1.close();
                outputStream1.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 解密ts
     *
     * @param sSrc   ts文件字节数组
     * @param length
     * @param sKey   密钥
     * @return 解密后的字节数组
     */
    private byte[] decrypt(byte[] sSrc, int length, String sKey, String iv, String method) throws Exception {
        if (StringUtils.isNotEmpty(method) && !method.contains("AES"))
            throw new M3u8Exception("未知的算法！");
        // 判断Key是否正确
        if (StringUtils.isEmpty(sKey))
            return null;
        // 判断Key是否为16位
        if (sKey.length() != 16 && !isByte) {
            throw new M3u8Exception("Key长度不是16位！");
        }
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        SecretKeySpec keySpec = new SecretKeySpec(isByte ? keyBytes : sKey.getBytes(StandardCharsets.UTF_8), "AES");
        byte[] ivByte;
        if (iv.startsWith("0x"))
            ivByte = StringUtils.hexStringToByteArray(iv.substring(2));
        else ivByte = iv.getBytes();
        if (ivByte.length != 16)
            ivByte = new byte[16];
        //如果m3u8有IV标签，那么IvParameterSpec构造函数就把IV标签后的内容转成字节数组传进去
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivByte);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
        return cipher.doFinal(sSrc, 0, length);
    }


}
