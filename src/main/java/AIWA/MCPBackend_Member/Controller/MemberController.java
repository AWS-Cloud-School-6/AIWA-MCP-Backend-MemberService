package AIWA.MCPBackend_Member.Controller;


import AIWA.MCPBackend_Member.Dto.*;
import AIWA.MCPBackend_Member.Entity.Member;
import AIWA.MCPBackend_Member.Service.member.MemberService;
import AIWA.MCPBackend_Member.Service.response.ResponseService;
import AIWA.MCPBackend_Member.response.CommonResult;
import AIWA.MCPBackend_Member.response.ListResult;
import AIWA.MCPBackend_Member.response.SingleResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member/api/members")
public class MemberController {
    private final MemberService memberService;
    private final ResponseService responseService;

    // 회원 등록
    @PostMapping("/register")
    public SingleResult<MemberResponseDto> registerMember(@RequestBody MemberRequestDto memberRequestDto) {
        Member savedMember = memberService.registerMember(memberRequestDto);
        MemberResponseDto memberResponseDto = MemberResponseDto.toDto(savedMember);
        return responseService.getSingleResult(memberResponseDto);
    }

    // 회원 삭제
    @DeleteMapping("/delete")
    public CommonResult deleteMember(@RequestBody MemberDeleteRequestDto deleteMemberRequestDto) {
        // 회원 삭제 서비스 호출
        memberService.deleteMember(deleteMemberRequestDto);

        // 성공 응답 반환 (ResponseService를 통해)
        return responseService.getSuccessResult();
    }


    // 특정 회원 조회
    @GetMapping("/email") // PathVariable로 이메일을 전달
    public SingleResult<MemberCredentialDTO> getMember(@RequestParam String email) {
        Member findMember = memberService.getMemberByEmail(email);  // Optional을 반환하지 않는다고 가정
//        System.out.println(email);
//        System.out.println(findMember.getAccess_key());
//        System.out.println(findMember.getSecret_key());

        if (findMember != null) {
            // Member 정보를 MemberCredentialDTO로 변환
            MemberCredentialDTO memberCredentialDTO = new MemberCredentialDTO(
                    findMember.getEmail(),
                    findMember.getAccess_key(),
                    findMember.getSecret_key()
            );
            System.out.println(memberCredentialDTO);
            return responseService.getSingleResult(memberCredentialDTO);
        } else {
            return (SingleResult<MemberCredentialDTO>) responseService.getFailResult();
        }
    }

    @GetMapping("/all")
    public ListResult<MemberResponseDto> getAllMembers() {
        List<Member> members = memberService.getAllMembers();
        List<MemberResponseDto> memberResponseDtoList = members.stream().map(MemberResponseDto::toDto).collect(Collectors.toList());
        return responseService.getListResult(memberResponseDtoList);
    }

    @PostMapping("/update-credentials")
    public CommonResult updateCredentials(@RequestBody MemberCredentialDTO memberCredentialDTO) {
        System.out.println(memberCredentialDTO.getAccessKey());
        System.out.println(memberCredentialDTO.getSecretKey());
        memberService.addOrUpdateKeys(memberCredentialDTO.getEmail(),memberCredentialDTO.getAccessKey(), memberCredentialDTO.getSecretKey());
        return responseService.getSuccessResult();
    }

}