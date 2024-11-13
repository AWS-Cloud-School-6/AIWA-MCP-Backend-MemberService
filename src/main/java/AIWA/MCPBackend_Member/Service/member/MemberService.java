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


    public String addOrUpdateAwsAndGcpKey(String email, String accessKey, String secretKey, String gcpKeyContent) {
        Member member = getMemberByEmail(email);

        // 키가 하나의 회사에 대해 AWS와 GCP를 모두 처리할 수 있도록 합니다.
        Optional<AiwaKey> existingAwsKey = member.getAiwaKeys().stream()
                .filter(key -> "AWS".equalsIgnoreCase(key.getCompanyName()))
                .findFirst();

        if (existingAwsKey.isPresent()) {
            AiwaKey awsKey = existingAwsKey.get();
            awsKey.setAccessKey(accessKey);
            awsKey.setSecretKey(secretKey);
            // GCP 키를 추가할 경우 GCP Key Path를 업데이트
            awsKey.setGcpKeyPath(s3Service.uploadGcpKeyFile(email, gcpKeyContent));
        } else {
            AiwaKey newKey = new AiwaKey("AWS", accessKey, secretKey, s3Service.uploadGcpKeyFile(email, gcpKeyContent), member);
            member.getAiwaKeys().add(newKey);
        }

        member = memberRepository.save(member);

        return "AWS and GCP keys have been successfully added or updated.";
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