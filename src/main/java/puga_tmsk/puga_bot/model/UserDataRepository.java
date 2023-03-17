package puga_tmsk.puga_bot.model;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.ArrayList;

public interface UserDataRepository  extends CrudRepository<UserData, Long> {

    UserData findByUserIdAndDate(long userId, LocalDate date);

    ArrayList<UserData> findAllByDate(LocalDate date);

    UserData findFirstByUserIdOrderByDateDesc(long userId);

    UserData findFirstByDateOrderByIdDesc(LocalDate date);

    UserData findFirstByOrderByIdDesc();

    int countAllByUserIdAndIsPidor(long userId, boolean isPidor);

}
