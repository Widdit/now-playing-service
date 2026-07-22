package com.widdit.nowplaying.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OutputServiceTest {

    @Test
    void writesObsTextFilesAsUtf8(@TempDir Path tempDir) throws IOException {
        Path outputFile = tempDir.resolve("custom.txt");
        String output = "BGM：Burning Desires 绝望吧台 - 三Z-STUDIO";

        ReflectionTestUtils.invokeMethod(
                new OutputService(),
                "writeText",
                outputFile.toString(),
                output);

        assertArrayEquals(
                output.getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(outputFile));
        assertEquals(output, new String(Files.readAllBytes(outputFile), StandardCharsets.UTF_8));
    }
}
