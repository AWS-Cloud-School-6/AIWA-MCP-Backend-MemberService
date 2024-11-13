package AIWA.MCPBackend_Member.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAwsAndGcpKeyRequestDto {
    private String email;
    private String accessKey;
    private String secretKey;
    private String gcpKeyContent;
}
