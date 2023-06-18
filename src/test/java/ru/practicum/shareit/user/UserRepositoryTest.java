package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void givenNewUser_whenSave_thenSuccess() {
        User user = User.builder().name("name").email("email@mail.com").build();
        User insertedUser = userRepository.save(user);
        assertThat(entityManager.find(User.class, insertedUser.getId())).isEqualTo(user);
    }

    @Test
    public void givenUserCreated_whenUpdate_thenSuccess() {
        User newUser = User.builder().name("name").email("email@mail.com").build();
        entityManager.persist(newUser);
        String newName = "NewName";
        newUser.setName(newName);
        userRepository.save(newUser);
        assertThat(entityManager.find(User.class, newUser.getId()).getName()).isEqualTo(newName);
    }

    @Test
    public void givenUserCreated_whenFindById_thenSuccess() {
        User newUser = User.builder().name("name").email("email@mail.com").build();
        entityManager.persist(newUser);
        Optional<User> retrievedUser = userRepository.findById(newUser.getId());
        assertThat(retrievedUser).contains(newUser);
    }

    @Test
    public void givenUserCreated_whenDelete_thenSuccess() {
        User newUser = User.builder().name("name").email("email@mail.com").build();
        entityManager.persist(newUser);
        userRepository.delete(newUser);
        assertThat(entityManager.find(User.class, newUser.getId())).isNull();
    }

    @Test
    public void givenUserCreated_whenExistByEmail_thenSuccess() {
        User newUser = User.builder().name("name").email("email@mail.com").build();
        entityManager.persist(newUser);
        assertTrue(userRepository.existsByEmail("email@mail.com"));
    }
}
