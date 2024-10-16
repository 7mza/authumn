package com.authumn.authumn.users

import com.authumn.authumn.privileges.IPrivilegeRepo
import com.authumn.authumn.privileges.Privilege
import com.authumn.authumn.roles.IRoleRepo
import com.authumn.authumn.roles.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomUserDetailsServiceTest {
    @Autowired
    private lateinit var userRepo: IUserRepo

    @Autowired
    private lateinit var roleRepo: IRoleRepo

    @Autowired
    private lateinit var privilegeRepo: IPrivilegeRepo

    @Autowired
    private lateinit var service: UserDetailsService

    private lateinit var privilege: Privilege
    private lateinit var role: Role
    private lateinit var user: User

    @BeforeEach
    fun beforeEach() {
        privilege = privilegeRepo.save(Privilege(id = "", label = "priv1", isDefault = false))
        role = roleRepo.save(Role(id = "", label = "role1", isDefault = false, privileges = listOf(privilege)))
        user = userRepo.save(User(id = "", email = "user1@mail.com", password = "password1", roles = listOf(role)))
    }

    @AfterEach
    fun afterEach() {
        userRepo.deleteAll()
        roleRepo.deleteAll()
        privilegeRepo.deleteAll()
    }

    @Test
    fun loadUserByUsername() {
        val principal = service.loadUserByUsername("user1@mail.com")
        assertThat(principal).isInstanceOf(KustomUser::class.java)
        assertThat((principal as KustomUser).id).isEqualTo(user.id)
        assertThat(principal.username).isEqualTo("user1@mail.com")
        assertThat(principal.password).isEqualTo("password1")
        assertThat(principal.authorities.map { it.authority }).hasSameElementsAs(listOf("role1", "priv1"))
        assertThat(principal.isEnabled).isTrue
        assertThat(principal.isAccountNonLocked).isTrue
        assertThat(principal.isCredentialsNonExpired).isTrue
    }

    @Test
    fun loadUserByUsernameNotFound() {
        val ex =
            assertThrows<UsernameNotFoundException> {
                service.loadUserByUsername("user2@mail.com")
            }
        assertThat(ex.message).isEqualTo("User not found")
    }
}
