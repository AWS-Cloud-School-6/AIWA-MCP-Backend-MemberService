package AIWA.MCPBackend_Member.Dto;

import AIWA.MCPBackend_Member.Entity.Member;
import AIWA.MCPBackend_Member.Entity.AiwaKey;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class MemberResponseDto {

    private Long id;        // 회원 ID
    private String name;    // 회원 이름
    private String email;   // 회원 이메일

    @JsonIgnore // 비밀번호는 반환하지 않음
    private String password; // 회원 비밀번호

    private List<AiwaKeyResponseDto> aiwaKeys; // 회원이 관리하는 회사들의 키 정보 리스트

    // Entity -> DTO 변환 메서드
    public static MemberResponseDto toDto(Member member) {
        MemberResponseDto memberResponseDto = new MemberResponseDto();
        memberResponseDto.setId(member.getId());
        memberResponseDto.setName(member.getName());
        memberResponseDto.setEmail(member.getEmail());
        memberResponseDto.setPassword(member.getPassword()); // 비밀번호는 필요 없다면 제외 가능

        // AiwaKey 리스트를 AiwaKeyResponseDto 리스트로 변환
        List<AiwaKeyResponseDto> aiwaKeyResponseDtoList = member.getAiwaKeys().stream()
                .map(AiwaKeyResponseDto::toDto)
                .collect(Collectors.toList());
        memberResponseDto.setAiwaKeys(aiwaKeyResponseDtoList);

        return memberResponseDto;
    }
}
