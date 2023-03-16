package puga_tmsk.puga_bot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Calendar;

@Data
@Entity(name = "user_data")
public class UserData {

    @Id
//    @GeneratedValue
    private long id;
    private long userId;
//    private Timestamp date;
    private LocalDate date;
    private long messageCount;
    private boolean isPidor;
}
