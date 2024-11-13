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
        // 회원 등록
        Member savedMember = memberService.registerMember(memberRequestDto);
        MemberResponseDto memberResponseDto = MemberResponseDto.toDto(savedMember);
        return responseService.getSingleResult(memberResponseDto);
    }

    // 회원 삭제
    @DeleteMapping("/delete")
    public CommonResult deleteMember(@RequestBody MemberDeleteRequestDto deleteMemberRequestDto) {
        // 회원 삭제
        memberService.deleteMember(deleteMemberRequestDto);
        return responseService.getSuccessResult();
    }

    // 모든 회원 조회
    @GetMapping("/all")
    public ListResult<MemberResponseDto> getAllMembers() {
        // 모든 회원 조회
        List<Member> members = memberService.getAllMembers();
        List<MemberResponseDto> memberResponseDtoList = members.stream()
                .map(MemberResponseDto::toDto)
                .collect(Collectors.toList());
        return responseService.getListResult(memberResponseDtoList);
    }


    @GetMapping("/email")
    public SingleResult<MemberResponseDto> getMemberByEmail(@RequestParam String email) {
        // 특정 회원 조회
        Member member = memberService.getMemberByEmail(email);
        if (member == null) {
            throw new RuntimeException("Member not found with Email: " + email); // 예외 처리
        }
        MemberResponseDto memberResponseDto = MemberResponseDto.toDto(member);
        return responseService.getSingleResult(memberResponseDto);
    }




    // AWS 키 추가/업데이트
    @PostMapping("/add-aws-gcp-key")
    public SingleResult<String> addAwsAndGcpKey(@RequestBody AddAwsAndGcpKeyRequestDto requestDto) {
        String result = memberService.addOrUpdateAwsAndGcpKey(
                requestDto.getEmail(),
                requestDto.getAccessKey(),
                requestDto.getSecretKey(),
                requestDto.getGcpKeyContent()
        );
        return responseService.getSingleResult(result);
    }

    // AWS 키 삭제
    @DeleteMapping("/delete-aws-key")
    public CommonResult deleteAwsKey(@RequestBody DeleteKeyRequestDto deleteKeyRequestDto) {
        // AWS 키 삭제
        memberService.removeAwsKey(deleteKeyRequestDto.getMemberId());
        return responseService.getSuccessResult();
    }

    // GCP 키 삭제
    @DeleteMapping("/delete-gcp-key")
    public CommonResult deleteGcpKey(@RequestBody DeleteKeyRequestDto deleteKeyRequestDto) {
        // GCP 키 삭제
        memberService.removeGcpKey(deleteKeyRequestDto.getMemberId());
        return responseService.getSuccessResult();
    }
}