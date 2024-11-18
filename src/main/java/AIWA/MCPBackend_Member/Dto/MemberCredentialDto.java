package AIWA.MCPBackend_Member.Dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberCredentialDto {
    private String email;
    private String accessKey;
    private String secretKey;

    public MemberCredentialDto(String email, String accessKey, String secretKey) {
        this.email = email;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    // Getters and Setters
}
