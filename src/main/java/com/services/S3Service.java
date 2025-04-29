package com.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.model.Room;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
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
        // Di chuyển ảnh
        moveFilesInDirectory("images/" + oldAddress, "images/" + newAddress);
        
        // Di chuyển video
        moveFilesInDirectory("videos/" + oldAddress, "videos/" + newAddress);
        
        // Di chuyển web360
        moveFilesInDirectory("web360/" + oldAddress, "web360/" + newAddress);
        
        // Di chuyển model
        moveFilesInDirectory("images/" + oldAddress + "/models", "images/" + newAddress + "/models");
        
        // Cập nhật lại các đường dẫn trong room
        if (room.getImagePaths() != null) {
            List<String> newImagePaths = room.getImagePaths().stream()
                .map(path -> path.replace(oldAddress, newAddress))
                .collect(Collectors.toList());
            room.setImagePaths(newImagePaths);
        }
        
        if (room.getVideoPaths() != null) {
            List<String> newVideoPaths = room.getVideoPaths().stream()
                .map(path -> path.replace(oldAddress, newAddress))
                .collect(Collectors.toList());
            room.setVideoPaths(newVideoPaths);
        }
        
        if (room.getWeb360Paths() != null) {
            List<String> newWeb360Paths = room.getWeb360Paths().stream()
                .map(path -> path.replace(oldAddress, newAddress))
                .collect(Collectors.toList());
            room.setWeb360Paths(newWeb360Paths);
        }
        
        if (room.getModelPath() != null) {
            String newModelPath = room.getModelPath().replace(oldAddress, newAddress);
            room.setModelPath(newModelPath);
        }

        // Xóa các folder cũ
        deleteS3Folder("images/" + oldAddress);
        deleteS3Folder("videos/" + oldAddress);
        deleteS3Folder("web360/" + oldAddress);

        // Xóa folder address cũ
        deleteS3Folder(oldAddress);
    }

    public void deleteFileFromS3(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("/")) return;
        // Lấy key từ url S3
        int idx = fileUrl.indexOf("images/");
        if (idx == -1) return;
        String key = fileUrl.substring(idx);
        s3Client.deleteObject(bucketName, key);
    }

    public void deleteS3Folder(String folderKey) {
        // folderKey ví dụ: images/Ha_Noi
        List<S3ObjectSummary> fileList = s3Client.listObjectsV2(bucketName, folderKey).getObjectSummaries();
        if (fileList.isEmpty()) return;
        List<DeleteObjectsRequest.KeyVersion> keys = fileList.stream()
            .map(obj -> new DeleteObjectsRequest.KeyVersion(obj.getKey()))
            .collect(Collectors.toList());
        DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName).withKeys(keys);
        s3Client.deleteObjects(deleteRequest);
    }

    private void moveFilesInDirectory(String sourceDir, String targetDir) {
        List<S3ObjectSummary> objects = s3Client.listObjectsV2(bucketName, sourceDir).getObjectSummaries();
        for (S3ObjectSummary obj : objects) {
            String sourceKey = obj.getKey();
            String targetKey = sourceKey.replace(sourceDir, targetDir);
            s3Client.copyObject(bucketName, sourceKey, bucketName, targetKey);
            s3Client.deleteObject(bucketName, sourceKey);
        }
    }
} 