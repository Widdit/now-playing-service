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

}
