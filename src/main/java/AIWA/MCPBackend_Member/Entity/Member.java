package AIWA.MCPBackend_Member.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 1000)
    private String access_key;
    @Column(length = 1000)
    private String secret_key;

    public Member(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
    }

}
