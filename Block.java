import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: Kang
 * @Version 1.1
 * @Package: PACKAGE_NAME
 * @CreateTime: 2021/12/2 0:07
 * @Software: IntelliJ IDEA
 */

public class Block {
    private final File blockFile;
    private final File blockBitMap;
    private final File recover;
    private FileWriter bitWriter;
    private FileWriter recoverWriter;
    private int fileNum;
    private double space;
    public int[][] bitmap = new int[32][32];
    private final Map<String, int[][]> filesBit = new HashMap<String, int[][]>();
    private final ArrayList<File> files = new ArrayList<File>();

    public Block(int name, File file, boolean rec) throws IOException {
        blockFile = file;
        blockBitMap = new File(blockFile.getPath() + File.separator + name + "BitMap&&Fat.txt");
        recover = new File(blockFile.getPath() + File.separator + "recover.txt");
        if (!rec) {
            space = 0;
            fileNum = 0;
            blockFile.mkdir();
            blockBitMap.createNewFile();
            bitWriter = new FileWriter(blockBitMap);
            for (int i = 0; i < 32; i++) {
                for (int k = 0; k < 32; k++) {
                    bitmap[i][k] = 0;
                    bitWriter.write("0");
                }
                bitWriter.write("\r\n");
            }
            bitWriter.flush();

            recover.createNewFile();
            recoverWriter = new FileWriter(recover);
            recoverWriter.write(String.valueOf(space) + "\r\n");
            recoverWriter.write(String.valueOf(fileNum) + "\r\n");
            for (int i = 0; i < 32; i++) {
                for (int k = 0; k < 32; k++) {
                    if (bitmap[i][k] == 0) {
                        recoverWriter.write("0\r\n");
                    } else {
                        recoverWriter.write("1\r\n");
                    }
                }
            }
            recoverWriter.flush();
        } else {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(recover));
                space = Double.parseDouble(reader.readLine());
                fileNum = Integer.parseInt(reader.readLine());
                for (int i = 0; i < 32; i++) {
                    for (int k = 0; k < 32; k++) {
                        if (Integer.parseInt(reader.readLine()) == 0) {
                            bitmap[i][k] = 0;
                        } else {
                            bitmap[i][k] = 1;
                        }
                    }
                }
                String temp;
                while ((temp = reader.readLine()) != null) {
                    File myFile = new File(blockFile.getPath() + File.separator + temp);
                    files.add(myFile);
                    int[][] tempBit = new int[32][32];
                    for (int i = 0; i < 32; i++) {
                        for (int k = 0; k < 32; k++) {
                            if (Integer.parseInt(reader.readLine()) == 0) {
                                tempBit[i][k] = 0;
                            } else {
                                tempBit[i][k] = 1;
                            }
                        }
                    }
                    filesBit.put(myFile.getName(), tempBit);
                }
                reader.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "出错了，请重试！",
                        "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
    }

    public File getBlockFile() {
        return blockFile;
    }


    // 文件进程
    public void putFCB(File file, double capacity) throws IOException {
        FileWriter newFileWriter = new FileWriter(file);
        newFileWriter.write("文件\r\n");
        newFileWriter.write(String.valueOf(capacity) + "\r\n");
        newFileWriter.write("文件名: " + file.getName() + "\r\n");
        newFileWriter.write("路径: " + file.getPath() + "\r\n");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String ctime = dateFormat.format(new Date(file.lastModified()));
        newFileWriter.write("最后修改时间: " + ctime + "\r\n");
        newFileWriter.close();
    }


    public void rewriteBitMap() throws IOException {
        bitWriter = new FileWriter(blockBitMap);
        bitWriter.write("");
        for (int i = 0; i < 32; i++) {
            for (int k = 0; k < 32; k++) {
                if (bitmap[i][k] == 0) {
                    bitWriter.write("0");
                } else {
                    bitWriter.write("1");
                }
            }
            bitWriter.write("\r\n");
        }
        for (File file : files) {
            bitWriter.write(file.getName() + ":");
            for (int k = 0; k < 32; k++) {
                for (int j = 0; j < 32; j++) {
                    try {
                        if (filesBit.get(file.getName())[k][j] == 1) {
                            bitWriter.write(String.valueOf(k * 32 + j) + " ");
                        }
                    } catch (Exception e) {
                        System.out.println("错误！");
                    }
                }
            }
            bitWriter.write("\r\n");
        }
        bitWriter.flush();
    }


    // 重写，刷新
    public void rewriteRecoverWriter() throws IOException {
        recoverWriter = new FileWriter(recover);
        recoverWriter.write("");

        recoverWriter.write(String.valueOf(space) + "\r\n");
        recoverWriter.write(String.valueOf(fileNum) + "\r\n");
        recWriter(bitmap);
        for (File file : files) {
            recoverWriter.write(file.getName() + "\r\n");
            int[][] bitTemp = filesBit.get(file.getName());
            recWriter(bitTemp);
        }
        recoverWriter.flush();
    }


    // 覆盖
    public void recWriter(int[][] bitmap) throws IOException {
        for (int i = 0; i < 32; i++) {
            for (int k = 0; k < 32; k++) {
                if (bitmap[i][k] == 0) {
                    recoverWriter.write("0\r\n");
                } else {
                    recoverWriter.write("1\r\n");
                }
            }
        }
    }


    // 新建文件
    public boolean createFile(File file, double capacity) throws IOException {
        files.add(file);
        file.createNewFile();
        int cap[][] = new int[32][32];
        for (int i = 0; i < 32; i++) {
            for (int k = 0; k < 32; k++)
                cap[i][k] = 0;
        }
        BufferedReader in = new BufferedReader(new FileReader(blockBitMap));
        int count = (int) capacity;
        for (int i = 0; i < 32; i++) {
            String line = in.readLine();
            for (int k = 0; k < 32; k++) {
                if (count > 0) {
                    if (line.charAt(k) == '0') {
                        count--;
                        cap[i][k] = 1;
                        bitmap[i][k] = 1;
                    }
                }
            }
        }
        if (count > 0) {
            JOptionPane.showMessageDialog(null, "内存不足!", "失败", JOptionPane.ERROR_MESSAGE);
            file.delete();
            for (int i = 0; i < 32; i++) {
                for (int k = 0; k < 32; k++) {
                    if (cap[i][k] == 1) {
                        bitmap[i][k] = 0;
                    }
                }
            }
            return false;
        } else {
            fileNum++;
            space += capacity;
            filesBit.put(file.getName(), cap);
            rewriteBitMap();
            rewriteRecoverWriter();
            // Put FCB
            putFCB(file, capacity);

            return true;
        }
    }


    // 删除文件
    public boolean deleteFile(File file, double capacity) {
        System.out.println(file);
        try {
            if (file.isFile()) {
                try {
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                space -= capacity;
                fileNum--;
                int[][] fileStore = filesBit.get(file.getName());
                for (int i = 0; i < 32; i++) {
                    for (int k = 0; k < 32; k++) {
                        if (bitmap[i][k] == 1 && fileStore[i][k] == 1) {
                            bitmap[i][k] = 0;
                        }
                    }
                }
                filesBit.remove(file.getName());
                for (int i = 0; i < files.size(); i++) {
                    if (files.get(i).getName().equals(file.getName())) {
                        files.remove(i);
                        break;
                    }
                }
            } else {
                File[] files = file.listFiles();
                assert files != null;
                for (File myFile : files) {
                    deleteFile(myFile, capacity);
                }
                while (file.exists()) {
                    file.delete();
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("失败");
            return false;
        }
    }


    // 重命名文件
    public boolean renameFile(File file, String name, double capacity) throws IOException {
        String oldName = file.getName();
        int[][] tempBit = filesBit.get(oldName);
        String c = file.getParent();
        File mm;
        if (file.isFile()) {
            mm = new File(c + File.separator + name);
            if (file.renameTo(mm)) {
                file = mm;
                filesBit.remove(oldName);
                filesBit.put(file.getName(), tempBit);
                // 执行文件进程
                putFCB(file, capacity);
                for (int i = 0; i < files.size(); i++) {
                    if (files.get(i).getName().equals(oldName)) {
                        files.remove(i);
                        files.add(file);
                        break;
                    }
                }
                rewriteBitMap();
                rewriteRecoverWriter();
                return true;
            } else {
                return false;
            }
        } else {
            mm = new File(c + File.separator + name);
            file.renameTo(mm);
            return true;
        }
    }

    public int getFileNum() {
        return fileNum;
    }

    public double getSpace() {
        return space;
    }
}
