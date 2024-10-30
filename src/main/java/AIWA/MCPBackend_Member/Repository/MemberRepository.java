package AIWA.MCPBackend_Member.Repository;

import AIWA.MCPBackend_Member.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
}
