
package com.jake.library.utils;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * File Utils
 *
 * @author jake 2012-5-12
 */
public class DownloadUtils {

    public final static String FILE_EXTENSION_SEPARATOR = ".";

    /**
     * read file
     *
     * @param filePath
     * @return if file not exist, return null, else return content of file
     * @throws IOException if an error occurs while operator BufferedReader
     */
    public static StringBuilder readFile(String filePath) {
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (file == null || !file.isFile()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            reader.close();
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * write file
     *
     * @param filePath
     * @param content
     * @param append   is append, if true, write to the end of file, else clear
     *                 content of file and write into it
     * @return return true
     * @throws IOException if an error occurs while operator FileWriter
     */
    public static boolean writeFile(String filePath, String content, boolean append) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * write file
     *
     * @param filePath
     * @param stream
     * @return return true
     * @throws IOException if an error occurs while operator FileWriter
     */
    public static boolean writeFile(String filePath, InputStream stream) {
        OutputStream o = null;
        try {
            o = new FileOutputStream(filePath);
            byte data[] = new byte[1024];
            int length = -1;
            while ((length = stream.read(data)) != -1) {
                o.write(data, 0, length);
            }
            o.flush();
            return true;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException occurred. ", e);
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (o != null) {
                try {
                    o.close();
                    stream.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * read file to string list, a element of list is a line
     *
     * @param filePath
     * @return if file not exist, return null, else return content of file
     * @throws IOException if an error occurs while operator BufferedReader
     */
    public static List<String> readFileToList(String filePath) {
        File file = new File(filePath);
        List<String> fileContent = new ArrayList<String>();
        if (file == null || !file.isFile()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                fileContent.add(line);
            }
            reader.close();
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    public static boolean writeListToFile(String filePath, List<String> list) {
        if (list == null || list.size() == 0) {
            return false;
        }
        FileWriter fileWriter = null;
        try {
            StringBuilder builder = new StringBuilder();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                builder.append(list.get(i));
                if (i + 1 < size) {
                    builder.append("\n");
                }
            }
            fileWriter = new FileWriter(filePath, false);
            fileWriter.write(builder.toString());
            fileWriter.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * get file name from path, not include suffix
     * <p>
     * <pre>
     *      getFileNameWithoutExtension(null)               =   null
     *      getFileNameWithoutExtension("")                 =   ""
     *      getFileNameWithoutExtension("   ")              =   "   "
     *      getFileNameWithoutExtension("abc")              =   "abc"
     *      getFileNameWithoutExtension("a.mp3")            =   "a"
     *      getFileNameWithoutExtension("a.b.rmvb")         =   "a.b"
     *      getFileNameWithoutExtension("c:\\")              =   ""
     *      getFileNameWithoutExtension("c:\\a")             =   "a"
     *      getFileNameWithoutExtension("c:\\a.b")           =   "a"
     *      getFileNameWithoutExtension("c:a.txt\\a")        =   "a"
     *      getFileNameWithoutExtension("/home/admin")      =   "admin"
     *      getFileNameWithoutExtension("/home/admin/a.txt/b.mp3")  =   "b"
     * </pre>
     *
     * @param filePath
     * @return file name from path, not include suffix
     * @see
     */
    public static String getFileNameWithoutExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (filePosi == -1) {
            return (extenPosi == -1 ? filePath : filePath.substring(0, extenPosi));
        }
        if (extenPosi == -1) {
            return filePath.substring(filePosi + 1);
        }
        return (filePosi < extenPosi ? filePath.substring(filePosi + 1, extenPosi) : filePath
                .substring(filePosi + 1));
    }

    /**
     * get file name from path, include suffix
     * <p>
     * <pre>
     *      getFileName(null)               =   null
     *      getFileName("")                 =   ""
     *      getFileName("   ")              =   "   "
     *      getFileName("a.mp3")            =   "a.mp3"
     *      getFileName("a.b.rmvb")         =   "a.b.rmvb"
     *      getFileName("abc")              =   "abc"
     *      getFileName("c:\\")              =   ""
     *      getFileName("c:\\a")             =   "a"
     *      getFileName("c:\\a.b")           =   "a.b"
     *      getFileName("c:a.txt\\a")        =   "a"
     *      getFileName("/home/admin")      =   "admin"
     *      getFileName("/home/admin/a.txt/b.mp3")  =   "b.mp3"
     * </pre>
     *
     * @param filePath
     * @return file name from path, include suffix
     */
    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? filePath : filePath.substring(filePosi + 1);
    }

    /**
     * get folder name from path
     * <p>
     * <pre>
     *      getFolderName(null)               =   null
     *      getFolderName("")                 =   ""
     *      getFolderName("   ")              =   ""
     *      getFolderName("a.mp3")            =   ""
     *      getFolderName("a.b.rmvb")         =   ""
     *      getFolderName("abc")              =   ""
     *      getFolderName("c:\\")              =   "c:"
     *      getFolderName("c:\\a")             =   "c:"
     *      getFolderName("c:\\a.b")           =   "c:"
     *      getFolderName("c:a.txt\\a")        =   "c:a.txt"
     *      getFolderName("c:a\\b\\c\\d.txt")    =   "c:a\\b\\c"
     *      getFolderName("/home/admin")      =   "/home"
     *      getFolderName("/home/admin/a.txt/b.mp3")  =   "/home/admin/a.txt"
     * </pre>
     *
     * @param filePath
     * @return
     */
    public static String getFolderName(String filePath) {

        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

    /**
     * get suffix of file from path
     * <p>
     * <pre>
     *      getFileExtension(null)               =   ""
     *      getFileExtension("")                 =   ""
     *      getFileExtension("   ")              =   "   "
     *      getFileExtension("a.mp3")            =   "mp3"
     *      getFileExtension("a.b.rmvb")         =   "rmvb"
     *      getFileExtension("abc")              =   ""
     *      getFileExtension("c:\\")              =   ""
     *      getFileExtension("c:\\a")             =   ""
     *      getFileExtension("c:\\a.b")           =   "b"
     *      getFileExtension("c:a.txt\\a")        =   ""
     *      getFileExtension("/home/admin")      =   ""
     *      getFileExtension("/home/admin/a.txt/b")  =   ""
     *      getFileExtension("/home/admin/a.txt/b.mp3")  =   "mp3"
     * </pre>
     *
     * @param filePath
     * @return
     */
    public static String getFileExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (extenPosi == -1) {
            return "";
        }
        return (filePosi >= extenPosi) ? "" : filePath.substring(extenPosi + 1);
    }

    /**
     * Creates the directory named by the trailing filename of this file,
     * including the complete directory path required to create this directory. <br/>
     * <br/>
     * <ul>
     * <strong>Attentions：</strong>
     * <li>makeDirs("C:\\Users\\Trinea") can only create users folder</li>
     * <li>makeFolder("C:\\Users\\Trinea\\") can create Trinea folder</li>
     * </ul>
     *
     * @param filePath
     * @return true if the necessary directories have been created or the target
     * directory already exists, false one of the directories can not be
     * created.
     * <ul>
     * <li>if {@link DownloadUtils#getFolderName(String)} return null, return
     * false</li>
     * <li>if target directory already exists, return true</li>
     * </ul>
     */
    public static boolean createFolder(String filePath, boolean recreate) {
        String folderName = getFolderName(filePath);
        if (TextUtils.isEmpty(folderName)) {
            return false;
        }

        File folder = new File(folderName);
        if (folder.exists()) {
            if (recreate) {
                deleteFile(folderName);
                return folder.mkdirs();
            } else {
                return true;
            }
        } else {
            return folder.mkdirs();
        }
    }

    /**
     * @param filePath
     * @return
     */
    public static boolean createFolder(String filePath) {
        return createFolder(filePath, false);
    }

    /**
     * Indicates if this file represents a file on the underlying file system.
     *
     * @param filePath
     * @return
     */
    public static boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    /**
     * Indicates if this file represents a directory on the underlying file
     * system.
     *
     * @param directoryPath
     * @return
     */
    public static boolean isFolderExist(String directoryPath) {
        if (TextUtils.isEmpty(directoryPath)) {
            return false;
        }

        File dire = new File(directoryPath);
        return (dire.exists() && dire.isDirectory());
    }

    /**
     * delete file or directory
     * <ul>
     * <li>if path is null or empty, return true</li>
     * <li>if path not exist, return true</li>
     * <li>if path exist, delete recursion. return true</li>
     * <ul>
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return true;
        }

        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    /**
     * get file size
     * <ul>
     * <li>if path is null or empty, return -1</li>
     * <li>if path exist and it is a file, return file size, else return -1</li>
     * <ul>
     *
     * @param path
     * @return
     */
    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }

        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    /**
     * 重命名文件/文件夹
     *
     * @param path
     * @param newName
     */
    public static boolean rename(final String path, final String newName) {
        boolean result = false;
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(newName)) {
            return result;
        }
        try {
            File file = new File(path);
            if (file.exists()) {
                result = file.renameTo(new File(newName));
            }
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * 读取文件
     *
     * @param filePath
     * @return 输入流
     */
    public static InputStream readFileInputStream(String filePath) {
        if (filePath == null || filePath.length() == 0) {
            return null;
        }

        InputStream is = null;
        try {
            if (isFileExist(filePath)) {
                File f = new File(filePath);
                is = new FileInputStream(f);
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
        return is;
    }

    /**
     * 创建一个空的文件
     *
     * @param filePath
     * @param recreate 是否删除重建
     * @return
     */
    public static boolean createFile(String filePath, boolean recreate) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        try {
            File file = new File(filePath);
            if (file.exists()) {
                if (recreate) {
                    file.delete();
                    file.createNewFile();
                }
            } else {
                // 如果路径不存在，先创建路径
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.createNewFile();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 获取MD5
     *
     * @param input 字符串
     * @return
     */
    public static String getMd5(String input) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(input.getBytes());
            return toHexString(md5.digest());
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取MD5
     *
     * @param input  字符串
     * @param encode 编码
     * @return
     */
    public static String getMd5(String input, String encode) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(input.getBytes(encode));
            return toHexString(md5.digest());
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取MD5
     *
     * @param input
     * @return
     */
    public static String getMd5(byte[] input) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(input);
            return toHexString(md5.digest());
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * byte数组转成十六进制字符串
     *
     * @param b
     * @return
     * @throws Exception
     */
    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
            sb.append(hexChar[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    private static char[] hexChar = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static final String HEX_NUMS_STR = "0123456789ABCDEF";

    /**
     * 将16进制字符串转换成字节数组
     *
     * @param hex
     * @return
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] hexChars = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (HEX_NUMS_STR.indexOf(hexChars[pos]) << 4 | HEX_NUMS_STR
                    .indexOf(hexChars[pos + 1]));
        }
        return result;
    }

    /**
     * 根据url获取文件名
     *
     * @param url
     * @return
     */
    public synchronized static String formatFileNameByUrl(String url) {
        if (url != null && url.contains("/")) {
            return url.substring(url.lastIndexOf("/"));
        }
        return url;
    }

    /**
     * 获取默认线程池
     *
     * @return
     */
    public static ExecutorService getDownloadDefaultThreadExecutor() {
        // 线程池核心线程数
        final int corePoolSize = 5;
        // 线程池最大线程数
        final int maximumPoolSize = 100;
        // 额外线程空状态生存时间
        final int keepAliveTime = 10000;
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10), new ThreadFactory() {
            private final AtomicInteger integer = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "myThreadPool thread:" + integer.getAndIncrement());
            }
        });
    }

    /**
     * 作为数据库保存的唯一标识
     *
     * @param url
     * @return
     */
    public synchronized static String getKey(String url) {
        return DownloadUtils.getMd5(url);
    }

    /**
     * 关闭io
     *
     * @param closeable
     */
    public static void closeIO(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long getContentLengthByURL(URL url) {
        int result = 0;
        if (url != null) {
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(2500);
                conn.setReadTimeout(2500);
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.connect();
                result = conn.getContentLength();
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
