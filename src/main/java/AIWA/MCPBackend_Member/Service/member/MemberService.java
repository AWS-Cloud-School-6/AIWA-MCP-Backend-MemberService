package AIWA.MCPBackend_Member.Service.member;

import AIWA.MCPBackend_Member.Dto.MemberDeleteRequestDto;
import AIWA.MCPBackend_Member.Dto.MemberRequestDto;
import AIWA.MCPBackend_Member.Entity.AiwaKey;
import AIWA.MCPBackend_Member.Entity.Member;
import AIWA.MCPBackend_Member.Repository.MemberRepository;
import AIWA.MCPBackend_Member.Service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    // 회원 등록
    public Member registerMember(MemberRequestDto memberRequestDto) {
        if (memberRepository.findByEmail(memberRequestDto.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }
        // 사용자 디렉터리 생성 (AWS 및 GCP 디렉토리 포함)
        s3Service.createUserAWSDirectory(memberRequestDto.getEmail());
        s3Service.createUserGCPDirectory(memberRequestDto.getEmail());

        // 회원 생성
        Member regiMember = new Member(memberRequestDto.getName(), memberRequestDto.getPassword(), memberRequestDto.getEmail());
        return memberRepository.save(regiMember);
    }

    // 회원 삭제
    public void deleteMember(MemberDeleteRequestDto deleteMemberRequestDto) {
        String email = deleteMemberRequestDto.getEmail();

        // 회원 정보 조회
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new RuntimeException("Member not found");
        }

        // S3에 저장된 사용자 디렉터리 삭제
        s3Service.deleteUserDirectory(email);

        // 회원 삭제
        memberRepository.delete(member);
    }

    // 특정 회원 조회
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // AWS 및 GCP 키 추가/수정
    public String addOrUpdateAwsAndGcpKey(String email, String companyName, String accessKey, String secretKey, String projectId, MultipartFile gcpKeyFile) throws IOException {
        // 회원 조회
        Member member = getMemberByEmail(email);
        if (member == null) {
            throw new RuntimeException("Member not found with Email: " + email);
        }

        // AWS 및 GCP 키 업데이트 또는 추가
        AiwaKey aiwaKey = findOrCreateAiwaKey(member, companyName);
        aiwaKey.setAccessKey(accessKey);
        aiwaKey.setSecretKey(secretKey);

        // AWS tfvars 파일 생성 및 URL 반환
        String awsTfvarsUrl = s3Service.createAwsTfvarsFile(email, accessKey, secretKey);
        aiwaKey.setAwsTfvarsUrl(awsTfvarsUrl);

        // GCP 키 처리 (GCP 키가 제공된 경우에만 업데이트)
        if (gcpKeyFile != null && !gcpKeyFile.isEmpty()) {
            // GCP 자격 증명 파일 업로드
            String gcpKeyPath = s3Service.uploadGcpKeyFile(email, gcpKeyFile);
            aiwaKey.setGcpKeyPath(gcpKeyPath);

            // GCP tfvars 파일 생성 및 URL 반환
            String gcpTfvarsUrl = s3Service.createGcpTfvarsFile(email,projectId,gcpKeyPath); // GCP tfvars 생성
            aiwaKey.setGcpTfvarsUrl(gcpTfvarsUrl);
        }

        // 회원 정보 저장
        memberRepository.save(member);

        return String.format("%s keys have been successfully added or updated.", companyName);
    }

    // AiwaKey를 찾아서 없으면 새로 생성
    private AiwaKey findOrCreateAiwaKey(Member member, String companyName) {
        // 기존 키가 있으면 업데이트, 없으면 새로 생성
        return member.getAiwaKeys().stream()
                .filter(key -> companyName.equalsIgnoreCase(key.getCompanyName()))
                .findFirst()
                .orElseGet(() -> {
                    AiwaKey newKey = new AiwaKey(companyName, null, null, null, member);
                    member.getAiwaKeys().add(newKey);
                    return newKey;
                });
    }

    // AWS 키 삭제
    public Member removeAwsKey(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.getAiwaKeys().removeIf(key -> "AWS".equalsIgnoreCase(key.getCompanyName()));

        return memberRepository.save(member);
    }

    // GCP 키 삭제
    public Member removeGcpKey(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // GCP 키를 삭제하고 S3에서 GCP 키 파일도 삭제
        member.getAiwaKeys().removeIf(key -> "GCP".equalsIgnoreCase(key.getCompanyName()));
        s3Service.deleteGcpKeyFile(member.getEmail());

        // GCP tfvars 파일 삭제
        s3Service.deleteGcpTfvarsFile(member.getEmail());

        return memberRepository.save(member);
    }
}
