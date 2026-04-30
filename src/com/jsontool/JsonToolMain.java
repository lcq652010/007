package com.jsontool;

import com.jsontool.cli.CommandLineArgs;
import com.jsontool.formatter.JsonFormatter;
import com.jsontool.parser.JsonParser;
import com.jsontool.util.FileUtils;
import com.jsontool.validator.JsonValidator;

import java.util.ArrayList;
import java.util.List;

public class JsonToolMain {
    private final JsonValidator validator;
    private final JsonParser parser;
    private final JsonFormatter formatter;
    private final FileUtils fileUtils;
    private final CommandLineArgs args;

    public JsonToolMain() {
        this.validator = new JsonValidator();
        this.parser = new JsonParser();
        this.formatter = new JsonFormatter();
        this.fileUtils = new FileUtils();
        this.args = new CommandLineArgs();
    }

    public static void main(String[] commandArgs) {
        JsonToolMain tool = new JsonToolMain();
        int exitCode = tool.run(commandArgs);
        System.exit(exitCode);
    }

    public int run(String[] commandArgs) {
        CommandLineArgs.ParseResult parseResult = args.parse(commandArgs);

        if (args.isShowHelp()) {
            System.out.println(CommandLineArgs.getHelp());
            return 0;
        }

        if (!parseResult.isSuccess()) {
            System.err.println("错误: " + parseResult.getMessage());
            System.err.println();
            System.err.println("使用 --help 查看帮助信息");
            return 1;
        }

        List<String> allFiles = collectFiles();

        if (allFiles.isEmpty()) {
            System.err.println("没有找到任何 JSON 文件");
            return 1;
        }

        if (args.getMode() == CommandLineArgs.Mode.VALIDATE) {
            return validateFiles(allFiles);
        } else if (args.getMode() == CommandLineArgs.Mode.FORMAT) {
            return formatFiles(allFiles);
        }

        return 0;
    }

    private List<String> collectFiles() {
        List<String> allFiles = new ArrayList<String>();

        for (String filePath : args.getFilePaths()) {
            allFiles.add(filePath);
        }

        for (String dirPath : args.getDirectoryPaths()) {
            FileUtils.FileListResult listResult;
            if (args.isRecursive()) {
                listResult = fileUtils.listJsonFiles(dirPath);
            } else {
                FileUtils.FileListResult tempResult = fileUtils.listJsonFiles(dirPath);
                List<String> filtered = new ArrayList<String>();
                if (tempResult.isSuccess() && tempResult.getFiles() != null) {
                    for (String f : tempResult.getFiles()) {
                        String relative = f.substring(dirPath.length());
                        if (!relative.contains("\\") && !relative.contains("/")) {
                            filtered.add(f);
                        }
                    }
                }
                listResult = new FileUtils.FileListResult(tempResult.isSuccess(), tempResult.getMessage(), filtered);
            }

            if (listResult.isSuccess() && listResult.getFiles() != null) {
                allFiles.addAll(listResult.getFiles());
            }
        }

        return allFiles;
    }

    private int validateFiles(List<String> files) {
        int successCount = 0;
        int failCount = 0;

        System.out.println("=== JSON 校验结果 ===");
        System.out.println();

        for (String filePath : files) {
            FileUtils.FileReadResult readResult = fileUtils.readFile(filePath);

            if (!readResult.isSuccess()) {
                System.out.println("[错误] " + filePath);
                System.out.println("       " + readResult.getMessage());
                failCount++;
                continue;
            }

            JsonValidator.ValidationResult validationResult = validator.validate(readResult.getContent());

            if (validationResult.isValid()) {
                System.out.println("[通过] " + filePath);
                successCount++;
            } else {
                System.out.println("[失败] " + filePath);
                System.out.println("       " + validationResult.getMessage());
                failCount++;
            }
        }

        System.out.println();
        System.out.println("=== 统计 ===");
        System.out.println("总计: " + files.size() + " 个文件");
        System.out.println("通过: " + successCount + " 个");
        System.out.println("失败: " + failCount + " 个");

        return failCount > 0 ? 1 : 0;
    }

    private int formatFiles(List<String> files) {
        int successCount = 0;
        int failCount = 0;

        System.out.println("=== JSON 格式化结果 ===");
        System.out.println();

        if (files.size() == 1 && args.getOutputPath() != null) {
            return formatSingleFileWithOutput(files.get(0), args.getOutputPath());
        }

        for (String filePath : files) {
            FileUtils.FileReadResult readResult = fileUtils.readFile(filePath);

            if (!readResult.isSuccess()) {
                System.out.println("[错误] " + filePath);
                System.out.println("       " + readResult.getMessage());
                failCount++;
                continue;
            }

            JsonParser.ParseResult parseResult = parser.parse(readResult.getContent());

            if (!parseResult.isSuccess()) {
                System.out.println("[失败] " + filePath);
                System.out.println("       解析错误: " + parseResult.getMessage());
                failCount++;
                continue;
            }

            JsonFormatter.FormatResult formatResult;
            if (args.isMinify()) {
                formatResult = formatter.formatMinified(parseResult.getValue());
            } else {
                formatResult = formatter.format(parseResult.getValue());
            }

            if (!formatResult.isSuccess()) {
                System.out.println("[失败] " + filePath);
                System.out.println("       格式化错误: " + formatResult.getMessage());
                failCount++;
                continue;
            }

            FileUtils.FileWriteResult writeResult = fileUtils.writeFile(
                filePath, formatResult.getFormatted(), args.isOverwrite());

            if (writeResult.isSuccess()) {
                System.out.println("[成功] " + filePath);
                successCount++;
            } else {
                System.out.println("[失败] " + filePath);
                System.out.println("       " + writeResult.getMessage());
                failCount++;
            }
        }

        System.out.println();
        System.out.println("=== 统计 ===");
        System.out.println("总计: " + files.size() + " 个文件");
        System.out.println("成功: " + successCount + " 个");
        System.out.println("失败: " + failCount + " 个");

        return failCount > 0 ? 1 : 0;
    }

    private int formatSingleFileWithOutput(String inputPath, String outputPath) {
        System.out.println("输入文件: " + inputPath);
        System.out.println("输出文件: " + outputPath);
        System.out.println();

        FileUtils.FileReadResult readResult = fileUtils.readFile(inputPath);

        if (!readResult.isSuccess()) {
            System.err.println("[错误] " + readResult.getMessage());
            return 1;
        }

        JsonParser.ParseResult parseResult = parser.parse(readResult.getContent());

        if (!parseResult.isSuccess()) {
            System.err.println("[失败] 解析错误: " + parseResult.getMessage());
            return 1;
        }

        JsonFormatter.FormatResult formatResult;
        if (args.isMinify()) {
            formatResult = formatter.formatMinified(parseResult.getValue());
        } else {
            formatResult = formatter.format(parseResult.getValue());
        }

        if (!formatResult.isSuccess()) {
            System.err.println("[失败] 格式化错误: " + formatResult.getMessage());
            return 1;
        }

        FileUtils.FileWriteResult writeResult = fileUtils.writeFile(
            outputPath, formatResult.getFormatted(), args.isOverwrite());

        if (writeResult.isSuccess()) {
            System.out.println("[成功] " + outputPath);
            return 0;
        } else {
            System.err.println("[失败] " + writeResult.getMessage());
            return 1;
        }
    }
}
