package puga_tmsk.puga_bot.model;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomUserDataRepozitory extends CrudRepository<UserData, Long>{



//        @Query("select e from Employees e where e.salary > :salary")
//        List<UserData> findEmployeesWithMoreThanSalary(@Param("salary") Long salary, Sort sort);
}
