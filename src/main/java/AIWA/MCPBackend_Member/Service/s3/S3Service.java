package AIWA.MCPBackend_Member.Service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3Client;
    private final String bucketName = "aiwa-terraform";

    // 사용자 디렉토리 생성 (AWS 및 GCP 디렉토리 포함)
    public void createUserAWSDirectory(String userId) {
        String userPrefix = "users/" + userId + "/AWS/";
        uploadAwsInitialFiles(userPrefix);
    }

    public void createUserGCPDirectory(String userId) {
        String userPrefix = "users/" + userId + "/GCP/";
        uploadGcpInitialFiles(userPrefix);
    }

    // AWS 초기 파일 업로드
    private void uploadAwsInitialFiles(String userPrefix) {
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

    // GCP 초기 파일 업로드
    private void uploadGcpInitialFiles(String userPrefix) {
        String mainTfContent = """
        variable "gcp_project_id" {
          description = "GCP Project ID"
          type        = string
        }

        variable "gcp_credentials" {
          description = "GCP Credentials JSON file content"
          type        = string
          sensitive   = true
        }

        provider "google" {
          project     = var.gcp_project_id
          region      = "asia-northeast3"
          credentials = var.gcp_credentials
        }
        """;

        s3Client.putObject(bucketName, userPrefix + "main.tf", mainTfContent);
    }

    // AWS tfvars 파일 생성
    public String createAwsTfvarsFile(String userId, String accessKey, String secretKey) {
        String userPrefix = "users/" + userId + "/AWS/";
        String tfvarsContent = String.format("""
            aws_access_key = "%s"
            aws_secret_key = "%s"
            """, accessKey, secretKey);

        String tfvarsKey = userPrefix + "aws_terraform.tfvars";
        s3Client.putObject(bucketName, tfvarsKey, tfvarsContent);
        return s3Client.getUrl(bucketName, tfvarsKey).toString(); // S3 URL 반환
    }

    // GCP tfvars 파일 생성
    public String createGcpTfvarsFile(String userId, String gcpProjectId) {
        String userPrefix = "users/" + userId + "/GCP/";
        String tfvarsContent = String.format("""
            gcp_project_id = "%s"
            gcp_credentials = "users/%s/GCP/gcp_credentials.json"
            """, gcpProjectId, userId);

        String tfvarsKey = userPrefix + "gcp_terraform.tfvars";
        s3Client.putObject(bucketName, tfvarsKey, tfvarsContent);
        return s3Client.getUrl(bucketName, tfvarsKey).toString(); // S3 URL 반환
    }

    // GCP 자격 증명 파일 업로드
    public String uploadGcpKeyFile(String userId, MultipartFile file) throws IOException {
        String userPrefix = "users/" + userId + "/GCP/";
        String gcpKeyFileKey = userPrefix + "gcp_credentials.json";

        // MultipartFile에서 InputStream을 추출
        InputStream inputStream = file.getInputStream();

        // 파일 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // S3에 파일 업로드
        s3Client.putObject(bucketName, gcpKeyFileKey, inputStream, metadata);

        return s3Client.getUrl(bucketName, gcpKeyFileKey).toString(); // S3 URL 반환
    }

    // GCP 키 파일 삭제
    public void deleteGcpKeyFile(String userId) {
        String gcpKeyFileKey = "users/" + userId + "/GCP/gcp_credentials.json";
        if (s3Client.doesObjectExist(bucketName, gcpKeyFileKey)) {
            s3Client.deleteObject(bucketName, gcpKeyFileKey);
        }
    }

    // AWS tfvars 파일 삭제
    public void deleteAwsTfvarsFile(String userId) {
        String awsTfvarsKey = "users/" + userId + "/AWS/aws_terraform.tfvars";
        if (s3Client.doesObjectExist(bucketName, awsTfvarsKey)) {
            s3Client.deleteObject(bucketName, awsTfvarsKey);
        }
    }

    // GCP tfvars 파일 삭제
    public void deleteGcpTfvarsFile(String userId) {
        String gcpTfvarsKey = "users/" + userId + "/GCP/gcp_terraform.tfvars";
        if (s3Client.doesObjectExist(bucketName, gcpTfvarsKey)) {
            s3Client.deleteObject(bucketName, gcpTfvarsKey);
        }
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
}
