package puga_tmsk.puga_bot.model;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;

public interface UserDataRepository  extends CrudRepository<UserData, Long> {

    UserData findByUserIdAndDate(long userId, LocalDate date);

    UserData findFirstByUserIdOrderByDateDesc(long userId);
}
