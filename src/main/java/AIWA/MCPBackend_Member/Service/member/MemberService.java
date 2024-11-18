package AIWA.MCPBackend_Member.Service.member;


import AIWA.MCPBackend_Member.Dto.MemberDeleteRequestDto;
import AIWA.MCPBackend_Member.Dto.MemberRequestDto;
import AIWA.MCPBackend_Member.Entity.AiwaKey;
import AIWA.MCPBackend_Member.Entity.Member;
import AIWA.MCPBackend_Member.Repository.MemberRepository;
import AIWA.MCPBackend_Member.Service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    public Member registerMember(MemberRequestDto memberRequestDto) {
        if (memberRepository.findByEmail(memberRequestDto.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }
        s3Service.createUserDirectory(memberRequestDto.getEmail());
        Member regiMember=new Member(memberRequestDto.getName(), memberRequestDto.getPassword(), memberRequestDto.getEmail());

        return memberRepository.save(regiMember);
    }

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


    public String addOrUpdateAwsAndGcpKey(String email, String companyName, String accessKey, String secretKey, String gcpKeyContent) {
        // 회원 조회
        Member member = getMemberByEmail(email);
        if (member == null) {
            throw new RuntimeException("Member not found with Email: " + email);
        }

        // AWS 키 처리: "AWS" 또는 다른 회사 키를 처리 가능하도록 수정
        Optional<AiwaKey> existingKey = member.getAiwaKeys().stream()
                .filter(key -> companyName.equalsIgnoreCase(key.getCompanyName()))
                .findFirst();

        if (existingKey.isPresent()) {
            // 기존 키 업데이트
            AiwaKey key = existingKey.get();
            key.setAccessKey(accessKey);
            key.setSecretKey(secretKey);

            // GCP 키 내용이 제공된 경우에만 GCP 키 경로 업데이트
            if (gcpKeyContent != null && !gcpKeyContent.isEmpty()) {
                String gcpKeyPath = s3Service.uploadGcpKeyFile(email, gcpKeyContent);
                key.setGcpKeyPath(gcpKeyPath);
            }
        } else {
            // 새로운 키 추가
            String gcpKeyPath = gcpKeyContent != null && !gcpKeyContent.isEmpty()
                    ? s3Service.uploadGcpKeyFile(email, gcpKeyContent)
                    : null;

            AiwaKey newKey = new AiwaKey(companyName, accessKey, secretKey, gcpKeyPath, member);
            member.getAiwaKeys().add(newKey);
        }

        // 회원 정보 저장
        memberRepository.save(member);

        return String.format("%s keys have been successfully added or updated.", companyName);
    }




    public Member removeAwsKey(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.getAiwaKeys().removeIf(key -> "AWS".equalsIgnoreCase(key.getCompanyName()));

        return memberRepository.save(member);
    }


    public Member removeGcpKey(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // GCP 키를 삭제하고 S3에서 GCP 키 파일도 삭제
        member.getAiwaKeys().removeIf(key -> "GCP".equalsIgnoreCase(key.getCompanyName()));
        s3Service.deleteGcpKeyFile(member.getEmail());

        return memberRepository.save(member);
    }


}