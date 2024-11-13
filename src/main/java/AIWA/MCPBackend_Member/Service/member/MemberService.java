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
    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }


    public Member addOrUpdateAwsKey(String email, String accessKey, String secretKey) {
        Member member = getMemberByEmail(email);

        // AWS 키가 있는지 확인하고, 있으면 업데이트, 없으면 추가
        Optional<AiwaKey> existingAwsKey = member.getAiwaKeys().stream()
                .filter(key -> "AWS".equalsIgnoreCase(key.getCompanyName()))
                .findFirst();

        if (existingAwsKey.isPresent()) {
            existingAwsKey.get().setAccessKey(accessKey);
            existingAwsKey.get().setSecretKey(secretKey);
        } else {
            AiwaKey awsKey = new AiwaKey("AWS", accessKey, secretKey, null, member);
            member.getAiwaKeys().add(awsKey);
        }

        return memberRepository.save(member);
    }

    public Member addOrUpdateGcpKey(String email, String gcpKeyContent) {
        Member member = getMemberByEmail(email);

        // GCP 키가 있는지 확인하고, 있으면 업데이트, 없으면 추가
        Optional<AiwaKey> existingGcpKey = member.getAiwaKeys().stream()
                .filter(key -> "GCP".equalsIgnoreCase(key.getCompanyName()))
                .findFirst();

        if (existingGcpKey.isPresent()) {
            String gcpKeyPath = s3Service.uploadGcpKeyFile(email, gcpKeyContent);
            existingGcpKey.get().setGcpKeyPath(gcpKeyPath);
        } else {
            String gcpKeyPath = s3Service.uploadGcpKeyFile(email, gcpKeyContent);
            AiwaKey gcpKey = new AiwaKey("GCP", null, null, gcpKeyPath, member);
            member.getAiwaKeys().add(gcpKey);
        }

        return memberRepository.save(member);
    }
}