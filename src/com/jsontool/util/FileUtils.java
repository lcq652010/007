package com.jsontool.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    public FileReadResult readFile(String filePath) {
        File file = new File(filePath);
        
        if (!file.exists()) {
            return new FileReadResult(false, "文件不存在: " + filePath, null);
        }
        
        if (!file.isFile()) {
            return new FileReadResult(false, "路径不是文件: " + filePath, null);
        }

        FileInputStream fis = null;
        BufferedReader reader = null;
        
        try {
            fis = new FileInputStream(file);
            
            byte[] bomBuffer = new byte[3];
            int bomRead = fis.read(bomBuffer);
            boolean hasBom = false;
            
            if (bomRead >= 3) {
                if (bomBuffer[0] == UTF8_BOM[0] && 
                    bomBuffer[1] == UTF8_BOM[1] && 
                    bomBuffer[2] == UTF8_BOM[2]) {
                    hasBom = true;
                }
            }
            
            fis.close();
            fis = new FileInputStream(file);
            
            if (hasBom) {
                fis.skip(3);
            }
            
            reader = new BufferedReader(new InputStreamReader(fis, DEFAULT_CHARSET));
            
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
            }
            
            String result = content.toString();
            
            if (result.length() > 0 && result.charAt(0) == '\uFEFF') {
                result = result.substring(1);
            }
            
            return new FileReadResult(true, "读取成功" + (hasBom ? " (已跳过UTF-8 BOM)" : ""), result);
        } catch (IOException e) {
            return new FileReadResult(false, "读取文件失败: " + e.getMessage(), null);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    public FileWriteResult writeFile(String filePath, String content) {
        return writeFile(filePath, content, false);
    }

    public FileWriteResult writeFile(String filePath, String content, boolean overwrite) {
        File file = new File(filePath);
        
        if (file.exists() && !overwrite) {
            return new FileWriteResult(false, "文件已存在，使用 --overwrite 参数覆盖: " + filePath);
        }

        BufferedWriter writer = null;
        
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8));
            
            writer.write(content);
            return new FileWriteResult(true, "写入成功: " + filePath);
        } catch (IOException e) {
            return new FileWriteResult(false, "写入文件失败: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    public FileListResult listJsonFiles(String directoryPath) {
        File dir = new File(directoryPath);
        
        if (!dir.exists()) {
            return new FileListResult(false, "目录不存在: " + directoryPath, null);
        }
        
        if (!dir.isDirectory()) {
            return new FileListResult(false, "路径不是目录: " + directoryPath, null);
        }

        List<String> jsonFiles = new ArrayList<String>();
        findJsonFiles(dir, jsonFiles);
        
        return new FileListResult(true, "找到 " + jsonFiles.size() + " 个JSON文件", jsonFiles);
    }

    private void findJsonFiles(File dir, List<String> jsonFiles) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                findJsonFiles(file, jsonFiles);
            } else if (file.getName().toLowerCase().endsWith(".json")) {
                jsonFiles.add(file.getAbsolutePath());
            }
        }
    }

    public static class FileReadResult {
        private final boolean success;
        private final String message;
        private final String content;

        public FileReadResult(boolean success, String message, String content) {
            this.success = success;
            this.message = message;
            this.content = content;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getContent() {
            return content;
        }
    }

    public static class FileWriteResult {
        private final boolean success;
        private final String message;

        public FileWriteResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class FileListResult {
        private final boolean success;
        private final String message;
        private final List<String> files;

        public FileListResult(boolean success, String message, List<String> files) {
            this.success = success;
            this.message = message;
            this.files = files;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public List<String> getFiles() {
            return files;
        }
    }
}
