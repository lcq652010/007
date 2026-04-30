package com.jsontool.cli;

import java.util.ArrayList;
import java.util.List;

public class CommandLineArgs {
    private Mode mode;
    private List<String> filePaths;
    private List<String> directoryPaths;
    private boolean overwrite;
    private boolean minify;
    private boolean recursive;
    private String outputPath;
    private boolean showHelp;
    private String errorMessage;

    public CommandLineArgs() {
        this.mode = Mode.UNKNOWN;
        this.filePaths = new ArrayList<String>();
        this.directoryPaths = new ArrayList<String>();
        this.overwrite = false;
        this.minify = false;
        this.recursive = false;
        this.showHelp = false;
        this.errorMessage = null;
    }

    public ParseResult parse(String[] args) {
        if (args.length == 0) {
            this.showHelp = true;
            return new ParseResult(true, "显示帮助");
        }

        int i = 0;
        while (i < args.length) {
            String arg = args[i];

            if (arg.equals("--validate") || arg.equals("-v")) {
                if (this.mode == Mode.UNKNOWN || this.mode == Mode.VALIDATE) {
                    this.mode = Mode.VALIDATE;
                } else {
                    return new ParseResult(false, "不能同时指定多个操作模式");
                }
                i++;
            } else if (arg.equals("--format") || arg.equals("-f")) {
                if (this.mode == Mode.UNKNOWN || this.mode == Mode.FORMAT) {
                    this.mode = Mode.FORMAT;
                } else {
                    return new ParseResult(false, "不能同时指定多个操作模式");
                }
                i++;
            } else if (arg.equals("--minify") || arg.equals("-m")) {
                this.minify = true;
                if (this.mode == Mode.UNKNOWN) {
                    this.mode = Mode.FORMAT;
                }
                i++;
            } else if (arg.equals("--overwrite") || arg.equals("-w")) {
                this.overwrite = true;
                i++;
            } else if (arg.equals("--recursive") || arg.equals("-r")) {
                this.recursive = true;
                i++;
            } else if (arg.equals("--output") || arg.equals("-o")) {
                if (i + 1 < args.length) {
                    this.outputPath = args[i + 1];
                    i += 2;
                } else {
                    return new ParseResult(false, "--output 选项需要指定输出路径");
                }
            } else if (arg.equals("--directory") || arg.equals("-d")) {
                if (i + 1 < args.length) {
                    this.directoryPaths.add(args[i + 1]);
                    if (this.mode == Mode.UNKNOWN) {
                        this.mode = Mode.VALIDATE;
                    }
                    i += 2;
                } else {
                    return new ParseResult(false, "--directory 选项需要指定目录路径");
                }
            } else if (arg.equals("--help") || arg.equals("-h") || arg.equals("-?")) {
                this.showHelp = true;
                return new ParseResult(true, "显示帮助");
            } else if (arg.startsWith("-")) {
                return new ParseResult(false, "未知选项: " + arg);
            } else {
                this.filePaths.add(arg);
                if (this.mode == Mode.UNKNOWN) {
                    this.mode = Mode.VALIDATE;
                }
                i++;
            }
        }

        if (this.showHelp) {
            return new ParseResult(true, "显示帮助");
        }

        if (this.mode == Mode.UNKNOWN) {
            if (this.filePaths.isEmpty() && this.directoryPaths.isEmpty()) {
                this.showHelp = true;
                return new ParseResult(true, "显示帮助");
            }
            this.mode = Mode.VALIDATE;
        }

        if (this.filePaths.isEmpty() && this.directoryPaths.isEmpty()) {
            return new ParseResult(false, "没有指定文件或目录");
        }

        return new ParseResult(true, "参数解析成功");
    }

    public static String getHelp() {
        return "JSON 工具 - 纯原生 Java 实现的 JSON 文件格式化与校验工具\n" +
               "适用于 Java 8 及以上版本\n\n" +
               "用法: java -jar json-tool.jar [选项] [文件...]\n\n" +
               "操作模式:\n" +
               "  -v, --validate        校验 JSON 文件的语法合法性 (默认模式)\n" +
               "  -f, --format          格式化 JSON 文件 (美化输出)\n" +
               "  -m, --minify          压缩 JSON 文件 (去除空白字符)\n\n" +
               "文件/目录选项:\n" +
               "  -d, --directory <dir> 处理指定目录下的 JSON 文件\n" +
               "  -r, --recursive       递归处理子目录 (与 -d 配合使用)\n" +
               "  -o, --output <path>   指定输出文件路径 (仅适用于单个文件格式化)\n" +
               "  -w, --overwrite       覆盖原文件 (格式化时使用)\n\n" +
               "其他选项:\n" +
               "  -h, --help            显示此帮助信息\n\n" +
               "示例:\n" +
               "  # 校验单个 JSON 文件\n" +
               "  java -jar json-tool.jar -v data.json\n\n" +
               "  # 校验多个 JSON 文件\n" +
               "  java -jar json-tool.jar -v file1.json file2.json\n\n" +
               "  # 格式化单个 JSON 文件并输出到新文件\n" +
               "  java -jar json-tool.jar -f input.json -o output.json\n\n" +
               "  # 格式化并覆盖原文件\n" +
               "  java -jar json-tool.jar -f -w data.json\n\n" +
               "  # 压缩 JSON 文件\n" +
               "  java -jar json-tool.jar -f -m -w data.json\n\n" +
               "  # 校验目录下所有 JSON 文件\n" +
               "  java -jar json-tool.jar -v -d ./config\n\n" +
               "  # 递归校验目录下所有 JSON 文件\n" +
               "  java -jar json-tool.jar -v -d ./project -r";
    }

    public Mode getMode() {
        return mode;
    }

    public List<String> getFilePaths() {
        return filePaths;
    }

    public List<String> getDirectoryPaths() {
        return directoryPaths;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public boolean isMinify() {
        return minify;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public enum Mode {
        VALIDATE,
        FORMAT,
        UNKNOWN
    }

    public static class ParseResult {
        private final boolean success;
        private final String message;

        public ParseResult(boolean success, String message) {
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
}
