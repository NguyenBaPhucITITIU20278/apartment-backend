package com.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.model.Room;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String addressFolder, String type) {
        try {
            String fileName = file.getOriginalFilename();
            String key = "images/" + addressFolder + "/" + type + "/" + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            s3Client.putObject(new PutObjectRequest(
                    bucketName,
                    key,
                    file.getInputStream(),
                    metadata
            ));

            return s3Client.getUrl(bucketName, key).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    public void moveRoomFilesToNewAddress(String oldAddress, String newAddress, Room room) {
        String[] types = {"images", "models", "web360", "video"};
        for (String type : types) {
            List<String> paths = null;
            if (type.equals("images")) paths = room.getImagePaths();
            else if (type.equals("web360")) paths = room.getWeb360Paths();
            else if (type.equals("models") && room.getModelPath() != null) paths = List.of(room.getModelPath());
            else if (type.equals("video") && room.getVideoPath() != null) paths = List.of(room.getVideoPath());
            if (paths == null) continue;

            for (int i = 0; i < paths.size(); i++) {
                String oldUrl = paths.get(i);
                // Lấy key từ url S3
                String oldKey = oldUrl.substring(oldUrl.indexOf("images/")); // hoặc models/, web360/, video/
                String newKey = oldKey.replace(oldAddress, newAddress);

                // Copy object sang key mới
                s3Client.copyObject(bucketName, oldKey, bucketName, newKey);
                // Xóa object cũ
                s3Client.deleteObject(new DeleteObjectRequest(bucketName, oldKey));

                // Update url mới trong room
                String newUrl = oldUrl.replace(oldAddress, newAddress);
                paths.set(i, newUrl);
            }

            // Gán lại vào room
            if (type.equals("images")) room.setImagePaths(paths);
            else if (type.equals("web360")) room.setWeb360Paths(paths);
            else if (type.equals("models")) room.setModelPath(paths.get(0));
            else if (type.equals("video")) room.setVideoPath(paths.get(0));
        }
    }

    public void deleteFileFromS3(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("/")) return;
        // Lấy key từ url S3
        int idx = fileUrl.indexOf("images/");
        if (idx == -1) return;
        String key = fileUrl.substring(idx);
        s3Client.deleteObject(bucketName, key);
    }
} 