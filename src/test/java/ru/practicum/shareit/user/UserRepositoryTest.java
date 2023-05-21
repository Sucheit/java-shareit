package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.user.model.UserEntity;

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
        UserEntity userEntity = UserEntity.builder().name("name").email("email@mail.com").build();
        UserEntity insertedUserEntity = userRepository.save(userEntity);
        assertThat(entityManager.find(UserEntity.class, insertedUserEntity.getId()) ).isEqualTo(userEntity);
    }

    @Test
    public void givenUserCreated_whenUpdate_thenSuccess() {
        UserEntity newUser = UserEntity.builder().name("name").email("email@mail.com").build();
        entityManager.persist(newUser);
        String newName = "NewName";
        newUser.setName(newName);
        userRepository.save(newUser);
        assertThat(entityManager.find(UserEntity.class, newUser.getId()).getName()).isEqualTo(newName);
    }

    @Test
    public void givenUserCreated_whenFindById_thenSuccess() {
        UserEntity newUser = UserEntity.builder().name("name").email("email@mail.com").build();
        entityManager.persist(newUser);
        Optional<UserEntity> retrievedUser = userRepository.findById(newUser.getId());
        assertThat(retrievedUser).contains(newUser);
    }

    @Test
    public void givenUserCreated_whenDelete_thenSuccess() {
        UserEntity newUser = UserEntity.builder().name("name").email("email@mail.com").build();
        entityManager.persist(newUser);
        userRepository.delete(newUser);
        assertThat(entityManager.find(UserEntity.class, newUser.getId())).isNull();
    }

    @Test
    public void givenUserCreated_whenExistByEmail_thenSuccess() {
        UserEntity newUser = UserEntity.builder().name("name").email("email@mail.com").build();
        entityManager.persist(newUser);
        assertTrue(userRepository.existsByEmail("email@mail.com"));
    }
}
