package AIWA.MCPBackend_Member.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRequestDto {
    private String name;
    private String email;
    private String password;
}