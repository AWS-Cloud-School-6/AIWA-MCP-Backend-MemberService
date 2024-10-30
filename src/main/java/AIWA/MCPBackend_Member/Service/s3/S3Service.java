package AIWA.MCPBackend_Member.Service.s3;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3Client;
    private final String bucketName = "aiwa-terraform";

    public void createUserDirectory(String userId) {
        String userPrefix = "users/" + userId + "/";
        // 초기 main.tf 및 terraform.tfstate 파일을 업로드합니다
        uploadInitialFiles(userPrefix);
    }

    private void uploadInitialFiles(String userPrefix) {
        // 초기 main.tf 파일
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
              access_key = var.aws_access_key  // 변수로 AWS Access Key 제공
              secret_key = var.aws_secret_key  // 변수로 AWS Secret Key 제공
            }
            """;

        s3Client.putObject(bucketName, userPrefix + "main.tf", mainTfContent);

//        // 빈 상태 파일
//        String emptyState = "{}";
//        s3Client.putObject(bucketName, userPrefix + "terraform.tfstate", emptyState);
    }

    public void deleteUserDirectory(String userId) {
        String userPrefix = "users/" + userId + "/";

        // S3에서 해당 디렉터리(prefix)를 기준으로 모든 파일 목록을 가져옴
        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(userPrefix);
        ListObjectsV2Result result;

        // 모든 객체를 반복적으로 가져와 삭제
        do {
            result = s3Client.listObjectsV2(request);
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                s3Client.deleteObject(bucketName, objectSummary.getKey());
            }
            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());  // 계속해서 모든 객체를 삭제할 때까지 반복
    }


    public void createTfvarsFile(String userId, String accessKey, String secretKey) {
        String userPrefix = "users/" + userId + "/";
        String tfvarsContent = String.format("""
            aws_access_key = "%s"
            aws_secret_key = "%s"
            """, accessKey, secretKey);

        s3Client.putObject(bucketName, userPrefix + "terraform.tfvars", tfvarsContent);
    }

}