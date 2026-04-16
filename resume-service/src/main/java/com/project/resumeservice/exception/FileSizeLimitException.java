package com.project.resumeservice.exception;

public class FileSizeLimitException extends RuntimeException {
    public FileSizeLimitException(long actual, long limit) {
        super(String.format(
                "File size %d bytes exceeds the maximum allowed %d bytes",
                actual, limit));
    }
}
