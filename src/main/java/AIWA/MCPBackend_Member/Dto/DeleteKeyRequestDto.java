package AIWA.MCPBackend_Member.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteKeyRequestDto {
    private Long memberId;
    private String companyName; // AWS 또는 GCP
}