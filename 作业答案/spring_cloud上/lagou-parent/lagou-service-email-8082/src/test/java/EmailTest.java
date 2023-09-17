import com.lagou.edu.EmailApplication8082;
import com.lagou.edu.service.EmailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = {EmailApplication8082.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class EmailTest {

    @Autowired
    EmailService emailService;


    @Test
    public void testSendEmail() {
      emailService.sendSimpleMail("517309804@qq.com","AuthCode","8888888");
    }
}
