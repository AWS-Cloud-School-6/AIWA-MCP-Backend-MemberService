package AIWA.MCPBackend_Member.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAwsKeyRequestDto {
    private String email;
    private String accessKey;
    private String secretKey;
    private String companyName;
}