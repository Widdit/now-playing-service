package com.widdit.nowplaying.util;

import com.widdit.nowplaying.entity.cmd.Args;
import com.widdit.nowplaying.entity.cmd.Option;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ConsoleUtil {

    /**
     * 开启一个进程在控制台执行 EXE 程序，获得输出
     * @param exePath EXE 程序路径
     * @return 控制台输出
     * @throws Exception
     */
    public static String runGetStdOut(String exePath) throws Exception {
        return runGetStdOut(exePath, null);
    }

    /**
     * 开启一个进程在控制台执行 EXE 程序，获得输出
     * @param exePath EXE 程序路径
     * @param args 参数对象
     * @return 控制台输出
     * @throws Exception
     */
    public static String runGetStdOut(String exePath, Args args) throws Exception {
        List<String> command = getCommand(exePath, args);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        StringBuilder output = new StringBuilder();

        // 启动进程
        Process process = processBuilder.start();

        // 获取进程的输入流（即 EXE 程序的输出）
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        // 等待进程结束
        process.waitFor();

        return output.toString().trim();
    }

    /**
     * 运行程序并等待
     * @param exePath EXE 程序路径
     * @return exitCode
     * @throws Exception
     */
    public static int runWait(String exePath) throws Exception {
        return runWait(exePath, null);
    }

    /**
     * 运行程序并等待
     * @param exePath EXE 程序路径
     * @param args 参数对象
     * @return exitCode
     * @throws Exception
     */
    public static int runWait(String exePath, Args args) throws Exception {
        List<String> command = getCommand(exePath, args);

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // 启动进程
        Process process = processBuilder.start();

        // 等待进程结束
        return process.waitFor();
    }

    /**
     * 运行程序
     * @param exePath EXE 程序路径
     * @throws Exception
     */
    public static void run(String exePath) throws Exception {
        run(exePath, null);
    }

    /**
     * 运行程序
     * @param exePath EXE 程序路径
     * @param args 参数对象
     * @throws Exception
     */
    public static void run(String exePath, Args args) throws Exception {
        List<String> command = getCommand(exePath, args);

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // 启动进程
        processBuilder.start();
    }

    /**
     * 结束进程，并等待结束
     * @param processName 进程名称（xxx.exe）
     * @throws Exception
     */
    public static void kill(String processName) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("taskkill", "/F", "/IM", processName);
        Process process = processBuilder.start();
        process.waitFor();
    }

    /**
     * 以管理员身份运行程序，并等待结束
     * @param exePath
     * @throws Exception
     */
    public static void runWaitAsAdmin(String exePath) throws Exception {
        String command = "powershell Start-Process -FilePath \"" + exePath + "\" -Verb RunAs -Wait";

        Process process = Runtime.getRuntime().exec(command);

        process.waitFor();
    }

    /**
     * 根据 EXE 路径和参数对象，获取命令列表（作为 ProcessBuilder 构造方法的参数）
     * @param exePath EXE 程序路径
     * @param args 参数对象
     * @return
     */
    public static List<String> getCommand(String exePath, Args args) {
        List<String> command = new ArrayList<>();
        command.add(exePath);

        if (args != null) {
            for (Option option : args.getOptions()) {
                String name = option.getName();
                if (!name.startsWith("--")) {
                    name = "--" + name;
                }

                command.add(name);
                command.add(option.getValue());
            }
        }

        return command;
    }

    /**
     * 运行程序（显示控制台窗口）
     * @param exePath EXE 程序路径
     * @throws Exception
     */
    public static void runWithConsole(String exePath) throws Exception {
        runWithConsole(exePath, null);
    }

    /**
     * 运行程序（显示控制台窗口）
     * @param exePath EXE 程序路径
     * @param args 参数对象
     * @throws Exception
     */
    public static void runWithConsole(String exePath, Args args) throws Exception {
        // 获取原始命令（exePath + 参数）
        List<String> command = getCommand(exePath, args);

        // 拼接为一个完整的命令行字符串
        StringBuilder cmdLine = new StringBuilder();
        for (String part : command) {
            // 带空格的部分要加引号
            if (part.contains(" ")) {
                cmdLine.append("\"").append(part).append("\"");
            } else {
                cmdLine.append(part);
            }
            cmdLine.append(" ");
        }

        // 使用 cmd.exe /c start "" 启动新控制台
        List<String> finalCommand = new ArrayList<>();
        finalCommand.add("cmd.exe");
        finalCommand.add("/c");
        finalCommand.add("start");
        finalCommand.add("\"\""); // 窗口标题，占位符
        finalCommand.add(cmdLine.toString().trim());

        ProcessBuilder processBuilder = new ProcessBuilder(finalCommand);
        processBuilder.start();
    }

}
