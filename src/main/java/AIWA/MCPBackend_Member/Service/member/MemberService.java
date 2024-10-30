package AIWA.MCPBackend_Member.Service.member;


import AIWA.MCPBackend_Member.Dto.MemberDeleteRequestDto;
import AIWA.MCPBackend_Member.Dto.MemberRequestDto;
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


    public Member addOrUpdateKeys(String email,String access_key,String secret_key) {

        Member member = getMemberByEmail(email);
        member.setAccess_key(access_key);
        member.setSecret_key(secret_key);
        s3Service.createTfvarsFile(email,access_key,secret_key);
        return memberRepository.save(member);
    }

    // Access Key와 Secret Key 삭제
    public Member removeKeys(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setAccess_key(null);
        member.setSecret_key(null);

        return memberRepository.save(member);
    }
}