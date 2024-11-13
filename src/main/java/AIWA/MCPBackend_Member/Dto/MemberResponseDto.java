package AIWA.MCPBackend_Member.Dto;

import AIWA.MCPBackend_Member.Entity.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberResponseDto {
    private String name;
    private String email;

    public static MemberResponseDto toDto(Member member) {
        MemberResponseDto dto = new MemberResponseDto();
        dto.setName(member.getName());
        dto.setEmail(member.getEmail());
        return dto;
    }
}