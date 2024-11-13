package AIWA.MCPBackend_Member.Service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3Client;
    private final String bucketName = "aiwa-terraform";

    // 사용자 디렉토리 생성
    public void createUserDirectory(String userId) {
        String userPrefix = "users/" + userId + "/";
        uploadInitialFiles(userPrefix);
    }

    private void uploadInitialFiles(String userPrefix) {
        String mainTfContent = """
            variable "aws_access_key" {
              description = "AWS Access Key"
              type        = string
            }

            variable "aws_secret_key" {
              description = "AWS Secret Key"
              type        = string
            }

            provider "aws" {
              region     = "ap-northeast-2"
              access_key = var.aws_access_key
              secret_key = var.aws_secret_key
            }
            """;

        s3Client.putObject(bucketName, userPrefix + "main.tf", mainTfContent);
    }

    // AWS tfvars 파일 생성
    public String createAwsTfvarsFile(String userId, String accessKey, String secretKey) {
        String userPrefix = "users/" + userId + "/";
        String tfvarsContent = String.format("""
            aws_access_key = "%s"
            aws_secret_key = "%s"
            """, accessKey, secretKey);

        String tfvarsKey = userPrefix + "aws_terraform.tfvars";
        s3Client.putObject(bucketName, tfvarsKey, tfvarsContent);
        return s3Client.getUrl(bucketName, tfvarsKey).toString(); // S3 URL 반환
    }

    // GCP 자격 증명 파일 업로드
    public String uploadGcpKeyFile(String userId, String gcpKeyContent) {
        String userPrefix = "users/" + userId + "/";
        String gcpKeyFileKey = userPrefix + "gcp_credentials.json";
        byte[] gcpKeyBytes = gcpKeyContent.getBytes(StandardCharsets.UTF_8);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(gcpKeyBytes.length);
        s3Client.putObject(bucketName, gcpKeyFileKey, new ByteArrayInputStream(gcpKeyBytes), metadata);

        return s3Client.getUrl(bucketName, gcpKeyFileKey).toString(); // S3 URL 반환
    }

    // 사용자 디렉토리 삭제
    public void deleteUserDirectory(String userId) {
        String userPrefix = "users/" + userId + "/";
        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(userPrefix);
        ListObjectsV2Result result;

        do {
            result = s3Client.listObjectsV2(request);
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                s3Client.deleteObject(bucketName, objectSummary.getKey());
            }
            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
    }

    // GCP 키 파일 삭제
    public void deleteGcpKeyFile(String userId) {
        String gcpKeyFileKey = "users/" + userId + "/gcp_credentials.json";
        if (s3Client.doesObjectExist(bucketName, gcpKeyFileKey)) {
            s3Client.deleteObject(bucketName, gcpKeyFileKey);
        }
    }

    // AWS tfvars 파일 삭제
    public void deleteAwsTfvarsFile(String userId) {
        String awsTfvarsKey = "users/" + userId + "/aws_terraform.tfvars";
        if (s3Client.doesObjectExist(bucketName, awsTfvarsKey)) {
            s3Client.deleteObject(bucketName, awsTfvarsKey);
        }
    }
}