package com.widdit.nowplaying.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleUtil {

    /**
     * 开启一个进程在控制台执行 EXE 程序，获得输出
     * @param exePath EXE 程序路径
     * @return 控制台输出
     * @throws Exception
     */
    public static String runGetStdOut(String exePath) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(exePath);
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

}
