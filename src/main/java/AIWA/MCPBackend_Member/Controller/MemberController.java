package AIWA.MCPBackend_Member.Controller;

import AIWA.MCPBackend_Member.Dto.AddAwsAndGcpKeyRequestDto;
import AIWA.MCPBackend_Member.Dto.MemberDeleteRequestDto;
import AIWA.MCPBackend_Member.Dto.MemberRequestDto;
import AIWA.MCPBackend_Member.Service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원 등록
    @PostMapping("/register")
    public ResponseEntity<String> registerMember(@RequestBody MemberRequestDto memberRequestDto) {
        try {
            memberService.registerMember(memberRequestDto);
            return ResponseEntity.ok("Member registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 회원 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMember(@RequestBody MemberDeleteRequestDto deleteMemberRequestDto) {
        try {
            memberService.deleteMember(deleteMemberRequestDto);
            return ResponseEntity.ok("Member deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // AWS 및 GCP 키 추가/수정
    @PostMapping("/add-keys")
    public ResponseEntity<String> addOrUpdateAwsAndGcpKey(@RequestBody AddAwsAndGcpKeyRequestDto addAwsAndGcpKeyRequestDto) {
        try {
            String result = memberService.addOrUpdateAwsAndGcpKey(
                    addAwsAndGcpKeyRequestDto.getEmail(),
                    addAwsAndGcpKeyRequestDto.getCompanyName(),
                    addAwsAndGcpKeyRequestDto.getAccessKey(),
                    addAwsAndGcpKeyRequestDto.getSecretKey(),
                    addAwsAndGcpKeyRequestDto.getGcpKeyContent()
            );
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 회원 정보 조회
    @GetMapping("/{email}")
    public ResponseEntity<Object> getMemberByEmail(@PathVariable String email) {
        try {
            return ResponseEntity.ok(memberService.getMemberByEmail(email));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 모든 회원 조회
    @GetMapping("/all")
    public ResponseEntity<Object> getAllMembers() {
        try {
            return ResponseEntity.ok(memberService.getAllMembers());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // AWS 키 삭제
    @DeleteMapping("/remove/aws/{memberId}")
    public ResponseEntity<String> removeAwsKey(@PathVariable Long memberId) {
        try {
            memberService.removeAwsKey(memberId);
            return ResponseEntity.ok("AWS key removed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GCP 키 삭제
    @DeleteMapping("/remove/gcp/{memberId}")
    public ResponseEntity<String> removeGcpKey(@PathVariable Long memberId) {
        try {
            memberService.removeGcpKey(memberId);
            return ResponseEntity.ok("GCP key removed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
