package uk.ac.rhul.cs3821.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles; // e.g. ["USER"], ["ADMIN"]

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
