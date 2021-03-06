package com.fable.kscc.api.skyCloud;

import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils
{
    public static void main(String[] args)
    {
        System.out.println("我们计算的文件MD5:" + MD5Utils.fileMD5("E:/idea/web/test/oos/ts-1503468493105.zip"));
        System.out.println("我们计算的文件MD5:" + MD5Utils.fileMD5("E:/idea/web/test/oos/ts-1503468645867.zip"));
    }
    
    public static String fileMD5(String inputFile)
    {
        
        // 缓冲区大小（这个可以抽出一个参数）
        
        int bufferSize = 256 * 1024;
        
        FileInputStream fileInputStream = null;
        
        DigestInputStream digestInputStream = null;
        
        try
        {
            
            // 拿到一个MD5转换器（同样，这里可以换成SHA1）
            
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            
            // 使用DigestInputStream
            
            fileInputStream = new FileInputStream(inputFile);
            
            digestInputStream = new DigestInputStream(fileInputStream, messageDigest);
            
            // read的过程中进行MD5处理，直到读完文件
            
            byte[] buffer = new byte[bufferSize];
            
            while (digestInputStream.read(buffer) > 0)
                
                // 获取最终的MessageDigest
                
                messageDigest = digestInputStream.getMessageDigest();
            
            // 拿到结果，也是字节数组，包含16个元素
            
            byte[] resultByteArray = messageDigest.digest();
            
            // 同样，把字节数组转换成字符串
            
            return byteArrayToHex(resultByteArray);
            
        }
        catch (Exception e)
        {
            
            return null;
            
        }
        finally
        {
            try
            {
                
                digestInputStream.close();
                
            }
            catch (Exception e)
            {
                
            }
            try
            {
                
                fileInputStream.close();
                
            }
            catch (Exception e)
            {
                
            }
        }
    }
    
    public static String stringMD5(String input)
    {
        
        try
        {
            // 拿到一个MD5转换器（如果想要SHA1参数换成”SHA1”）
            
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            
            // 输入的字符串转换成字节数组
            
            byte[] inputByteArray = input.getBytes();
            
            // inputByteArray是输入字符串转换得到的字节数组
            
            messageDigest.update(inputByteArray);
            
            // 转换并返回结果，也是字节数组，包含16个元素
            
            byte[] resultByteArray = messageDigest.digest();
            
            // 字符数组转换成字符串返回
            
            return byteArrayToHex(resultByteArray);
            
        }
        catch (NoSuchAlgorithmException e)
        {
            
            return null;
        }
    }
    
    public static String byteArrayToHex(byte[] byteArray)
    {
        // 首先初始化一个字符数组，用来存放每个16进制字符
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
        char[] resultCharArray = new char[byteArray.length * 2];
        // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        int index = 0;
        for (byte b : byteArray)
        {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        // 字符数组组合成字符串返回
        return new String(resultCharArray);
    }
}
