package AIWA.MCPBackend_Member.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddGcpKeyRequestDto {
    private String email;
    private String gcpKeyContent;
    private String companyName;
}