package AIWA.MCPBackend_Member.Dto;

import lombok.Getter;

@Getter
public class CompanyDto {
    private String userId;
    private String company;
    private String accessKey;
    private String secretKey;

    public CompanyDto(String userId, String company, String accessKey, String secretKey) {
        this.userId = userId;
        this.company = company;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }
}
