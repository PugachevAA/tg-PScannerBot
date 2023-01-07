package puga_tmsk.puga_bot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

@Data
@Entity(name = "user_data")
public class UserData {

    @Id
    private long id;
    @Id
    private long userId;
    private Timestamp date;
    private long messageCount;
    private boolean isPidor;
}
