package puga_tmsk.puga_bot.model;

import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface UserRepository extends CrudRepository<User, Long> {
    ArrayList<User> findAll();
}
