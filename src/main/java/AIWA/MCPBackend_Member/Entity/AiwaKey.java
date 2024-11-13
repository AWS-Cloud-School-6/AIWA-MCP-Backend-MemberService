package AIWA.MCPBackend_Member.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AiwaKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aiwa_key_id")
    private Long id;

    @Column(nullable = false)
    private String companyName;

    private String accessKey;
    private String secretKey;
    private String gcpKeyPath;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public AiwaKey(String companyName, String accessKey, String secretKey, String gcpKeyPath, Member member) {
        this.companyName = companyName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.gcpKeyPath = gcpKeyPath;
        this.member = member;
    }
}
