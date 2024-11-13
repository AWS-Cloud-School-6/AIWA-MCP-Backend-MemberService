package AIWA.MCPBackend_Member.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class AiwaKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;

    @Column(length = 1000)
    private String accessKey; // AWS access key

    @Column(length = 1000)
    private String secretKey; // AWS secret key

    @Column(length = 2048) // S3 경로를 저장
    private String gcpKeyPath;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}