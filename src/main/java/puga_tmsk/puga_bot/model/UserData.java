package puga_tmsk.puga_bot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Date;
import java.util.Calendar;

@Data
@Entity(name = "user_data")
public class UserData {

    @Id
    private long id;
    private long userId;
    private Calendar date;
    private long messageCount;
    private boolean isPidor;
}
