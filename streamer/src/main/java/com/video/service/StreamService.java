package com.video.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Logger;
import jakarta.annotation.PostConstruct;

@Service
public class StreamService {

    private final String CONTENT_TYPE = "Content-Type";
    private final String CONTENT_LENGTH = "Content-Length";
    private final String VIDEO_CONTENT = "video/mp4";
    private final String CONTENT_RANGE = "Content-Range";
    private final String ACCEPT_RANGES = "Accept-Ranges";
    private final String BYTES = "bytes";
    private final int CHUNK_SIZE = 10000;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(StreamService.class);

    private String staticFilesPath;


    public ResponseEntity < byte[] > streamVideoInChunks(final String fileName, final String range) {
        try {
            long rangeStart = 0;
            long rangeEnd = CHUNK_SIZE;
            final Long fileSize = getFileSize(fileName);

            if (range == null || range.equals("")) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(CONTENT_TYPE, VIDEO_CONTENT)
                    .header(ACCEPT_RANGES, BYTES)
                    .header(CONTENT_LENGTH, String.valueOf(rangeEnd))
                    .header(CONTENT_RANGE, BYTES + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                    .header(CONTENT_LENGTH, String.valueOf(fileSize))
                    .body(readByteRange(fileName, rangeStart, rangeEnd));
            }

            String[] ranges = range.split("-");

            rangeStart = Long.parseLong(ranges[0].substring(6));

            if (ranges.length > 1) {
                rangeEnd = Long.parseLong(ranges[1]);
            } else {
                rangeEnd = rangeStart + CHUNK_SIZE;
            }

            if (rangeEnd > fileSize - 1) {
                rangeEnd = fileSize - 1;
            }

            final byte[] data = readByteRange(fileName, rangeStart, rangeEnd);
            final String contentLength = String.valueOf((rangeEnd - rangeStart) + 1);

            HttpStatus httpStatus = HttpStatus.PARTIAL_CONTENT;

            if (rangeEnd >= fileSize) {
                httpStatus = HttpStatus.OK;
            }

            return ResponseEntity.status(httpStatus)
                .header(CONTENT_TYPE, VIDEO_CONTENT)
                .header(ACCEPT_RANGES, BYTES)
                .header(CONTENT_LENGTH, contentLength)
                .header(CONTENT_RANGE, BYTES + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                .body(data);
        } catch (IOException e) {
            logger.error("Exception while reading the file {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private byte[] readByteRange(String filename, long start, long end) throws IOException {
        Path path = Paths.get(getFilePath(filename));
        byte[] data = Files.readAllBytes(path);
        byte[] result = new byte[(int)(end - start) + 1];
        System.arraycopy(data, (int) start, result, 0, (int)(end - start) + 1);
        return result;
    }

    private String getFilePath(String fileNameWithExtension) {
        return staticFilesPath + fileNameWithExtension;
    }

    private Long getFileSize(String fileName) {
        return Optional.ofNullable(fileName)
            .map(file - > Paths.get(getFilePath(file)))
            .map(this::sizeFromFile)
            .orElse(0 L);
    }

    private Long sizeFromFile(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ioException) {
            logger.error("Error while getting the file size", ioException);
        }
        return 0 L;
    }

    @PostConstruct
    void init() {
        File pwdFile = new File("");
        staticFilesPath = pwdFile.getAbsolutePath() + "/src/main/resources/static/";
    }
}
