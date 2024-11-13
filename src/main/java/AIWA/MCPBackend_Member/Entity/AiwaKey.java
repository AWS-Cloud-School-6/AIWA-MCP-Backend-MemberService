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
    private Long id;

    @Column(nullable = false)
    private String companyName; // 고객 회사 이름

    @Column(length = 1000)
    private String accessKey; // AWS Access Key

    @Column(length = 1000)
    private String secretKey; // AWS Secret Key

    @Column(length = 2048)
    private String gcpKeyPath; // GCP 키 파일 S3 경로

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public AiwaKey(String companyName, String accessKey, String secretKey, String gcpKeyPath, Member member) {
        this.companyName = companyName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.gcpKeyPath = gcpKeyPath;
        this.member = member;
    }
}