package AIWA.MCPBackend_Member.Controller;

import AIWA.MCPBackend_Member.Dto.*;
import AIWA.MCPBackend_Member.Entity.AiwaKey;
import AIWA.MCPBackend_Member.Entity.Member;
import AIWA.MCPBackend_Member.Service.member.MemberService;
import AIWA.MCPBackend_Member.Service.response.ResponseService;
import AIWA.MCPBackend_Member.response.CommonResult;
import AIWA.MCPBackend_Member.response.ListResult;
import AIWA.MCPBackend_Member.response.SingleResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
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

    @GetMapping("/{email}/{companyName}")
    public SingleResult<MemberResponseDto> getMemberAndKeyByEmailAndCompany(
            @PathVariable String email,
            @PathVariable String companyName) {

        // 서비스에서 멤버 조회
        Member member = memberService.getMemberByEmail(email);

        if (member == null) {
            throw new IllegalArgumentException("Member not found");
        }

        // AiwaKey 조회
        Optional<AiwaKey> optionalKey = member.getAiwaKeys().stream()
                .filter(key -> companyName.equalsIgnoreCase(key.getCompanyName()))
                .findFirst();

        if (optionalKey.isPresent()) {
            AiwaKey aiwaKey = optionalKey.get();

            // MemberResponseDto 생성
            MemberResponseDto memberResponseDto = MemberResponseDto.toDto(member);

            // AiwaKeyResponseDto 추가 (해당 회사 키만 추가)
            List<AiwaKeyResponseDto> filteredKeys = List.of(AiwaKeyResponseDto.toDto(aiwaKey));
            memberResponseDto.setAiwaKeys(filteredKeys);

            return responseService.getSingleResult(memberResponseDto);
        } else {
            throw new IllegalArgumentException("Key for company '" + companyName + "' not found");
        }
    }




    // AWS 및 GCP 키 추가/업데이트
    @PostMapping("/add-aws-gcp-key")
    public SingleResult<String> addAwsAndGcpKey(
            @RequestParam String email,
            @RequestParam String companyName,
            @RequestParam String accessKey,
            @RequestParam String secretKey,
            @RequestParam String projectId,
            @RequestParam(required = false) MultipartFile gcpKeyFile) throws IOException {

        // GCP 자격 증명 파일을 포함하여 AWS 및 GCP 키 처리
        String result = memberService.addOrUpdateAwsAndGcpKey(
                email,
                companyName,
                accessKey,
                secretKey,
                projectId,
                gcpKeyFile
        );
        return responseService.getSingleResult(result);
    }

    // AWS 키 삭제
    @DeleteMapping("/delete-aws-key")
    public CommonResult deleteAwsKey(@RequestBody DeleteKeyRequestDto deleteKeyRequestDto) {
        memberService.removeAwsKey(deleteKeyRequestDto.getMemberId());
        return responseService.getSuccessResult();
    }

    // GCP 키 삭제
    @DeleteMapping("/delete-gcp-key")
    public CommonResult deleteGcpKey(@RequestBody DeleteKeyRequestDto deleteKeyRequestDto) {
        memberService.removeGcpKey(deleteKeyRequestDto.getMemberId());
        return responseService.getSuccessResult();
    }
}
