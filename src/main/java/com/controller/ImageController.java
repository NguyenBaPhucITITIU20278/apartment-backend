package com.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ImageController {

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/api/images")
    public List<String> listImages() {
        File folder = new File(uploadPath);
        return Arrays.stream(folder.listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
    }
}