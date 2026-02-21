package com.widdit.nowplaying.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
public class FileUtil {

    /**
     * 清空目录内的所有内容（保留目录本身）
     *
     * @param directory 要清空的目录
     * @throws IOException 如果删除过程中发生错误
     */
    public static void clearDirectory(Path directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("目录路径不能为空");
        }

        if (!Files.exists(directory)) {
            // 目录不存在，无需清空
            return;
        }

        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("指定路径不是目录: " + directory);
        }

        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(directory))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("删除文件/目录失败: {}，原因: {}", path, e.getMessage());
                        }
                    });
        }
    }

    /**
     * 递归复制目录内容到目标目录
     *
     * @param sourceDir 源目录
     * @param targetDir 目标目录
     * @return int数组 [复制的文件数, 复制的目录数]
     * @throws IOException 如果复制过程中发生错误
     */
    public static int[] copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        if (sourceDir == null || targetDir == null) {
            throw new IllegalArgumentException("源目录和目标目录路径不能为空");
        }

        if (!Files.exists(sourceDir)) {
            throw new IllegalArgumentException("源目录不存在: " + sourceDir);
        }

        if (!Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("源路径不是目录: " + sourceDir);
        }

        // 确保目标目录存在
        ensureDirectoryExists(targetDir);

        int[] counter = {0, 0}; // [文件数, 目录数]

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetPath = targetDir.resolve(sourceDir.relativize(dir));

                if (!dir.equals(sourceDir)) {
                    if (!Files.exists(targetPath)) {
                        Files.createDirectories(targetPath);
                        counter[1]++;
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetPath = targetDir.resolve(sourceDir.relativize(file));

                Path parentDir = targetPath.getParent();
                if (parentDir != null && !Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                }

                Files.copy(file, targetPath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES);
                counter[0]++;

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                log.warn("无法访问文件: {}，原因: {}，跳过继续处理", file, exc.getMessage());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                if (exc != null) {
                    log.warn("遍历目录时发生错误: {}，原因: {}", dir, exc.getMessage());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return counter;
    }

    /**
     * 确保目录存在，如果不存在则创建
     *
     * @param directory 目录路径
     * @throws IOException 如果创建目录失败
     */
    public static void ensureDirectoryExists(Path directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("目录路径不能为空");
        }

        if (Files.exists(directory)) {
            if (!Files.isDirectory(directory)) {
                throw new IOException("路径已存在但不是目录: " + directory);
            }
        } else {
            Files.createDirectories(directory);
        }
    }

    /**
     * 检查目录是否存在且可读
     *
     * @param directory 目录路径
     * @return true 如果目录存在且可读
     */
    public static boolean isDirectoryReadable(Path directory) {
        return directory != null
                && Files.exists(directory)
                && Files.isDirectory(directory)
                && Files.isReadable(directory);
    }

    /**
     * 删除目录及其所有内容
     *
     * @param directory 要删除的目录
     * @throws IOException 如果删除过程中发生错误
     */
    public static void deleteDirectory(Path directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("目录路径不能为空");
        }

        if (!Files.exists(directory)) {
            // 目录不存在，无需删除
            return;
        }

        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("删除文件/目录失败: {}，原因: {}", path, e.getMessage());
                        }
                    });
        }
    }

    /**
     * 复制单个文件
     *
     * @param source 源文件
     * @param target 目标文件
     * @param replaceExisting 是否替换已存在的文件
     * @throws IOException 如果复制失败
     */
    public static void copyFile(Path source, Path target, boolean replaceExisting) throws IOException {
        if (source == null || target == null) {
            throw new IllegalArgumentException("源文件和目标文件路径不能为空");
        }

        if (!Files.exists(source)) {
            throw new IllegalArgumentException("源文件不存在: " + source);
        }

        if (Files.isDirectory(source)) {
            throw new IllegalArgumentException("源路径是目录而非文件: " + source);
        }

        // 确保目标文件的父目录存在
        Path parentDir = target.getParent();
        if (parentDir != null) {
            ensureDirectoryExists(parentDir);
        }

        if (replaceExisting) {
            Files.copy(source, target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES);
        } else {
            Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    /**
     * 判断目录是否为空
     *
     * @param directory 目录路径
     * @return true 如果目录为空或不存在
     * @throws IOException 如果读取目录失败
     */
    public static boolean isDirectoryEmpty(Path directory) throws IOException {
        if (directory == null || !Files.exists(directory)) {
            return true;
        }

        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("指定路径不是目录: " + directory);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            return !stream.iterator().hasNext();
        }
    }

}
