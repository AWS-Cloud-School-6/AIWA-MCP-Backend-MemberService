package AIWA.MCPBackend_Member.Dto;

import AIWA.MCPBackend_Member.Entity.AiwaKey;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiwaKeyResponseDto {

    private String companyName;   // 회사 이름 (AWS, GCP 등)
    private String accessKey;     // Access Key (AWS의 경우만 있을 수 있음)
    private String secretKey;     // Secret Key (AWS의 경우만 있을 수 있음)
    private String gcpKeyPath;    // GCP Key Path (GCP의 경우만 있을 수 있음)
    private String awsTfvarsUrl;
    private String gcpTfvarsUrl;

    // Entity -> DTO 변환 메서드
    public static AiwaKeyResponseDto toDto(AiwaKey aiwaKey) {
        AiwaKeyResponseDto dto = new AiwaKeyResponseDto();
        dto.setCompanyName(aiwaKey.getCompanyName());
        dto.setAccessKey(aiwaKey.getAccessKey());
        dto.setSecretKey(aiwaKey.getSecretKey());
        dto.setGcpKeyPath(aiwaKey.getGcpKeyPath());
        dto.setAwsTfvarsUrl(aiwaKey.getAwsTfvarsUrl());
        dto.setGcpTfvarsUrl(aiwaKey.getGcpTfvarsUrl());
        return dto;
    }
}
