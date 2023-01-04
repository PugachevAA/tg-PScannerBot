package puga_tmsk.puga_bot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@Entity(name = "users")
public class User {

    @Id
    private long userId;
    private String userName;
    private String firstName;
    private String lastName;
    private Timestamp registerTime;
    private long pidorCount;
    private boolean pidorNow;

    @Override
    public String toString() {
        return  "Имя='" + firstName + '\n' +
                "Фамилия='" + lastName + '\n' +
                "Логин='" + userName + '\n' +
                "Дата регистрации=" + registerTime + '\n' +
                "Был пидором " + registerTime + " раз";
    }

}
