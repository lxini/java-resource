import org.junit.Test;

import java.time.ZonedDateTime;

public class MyTest {
    @Test
    public void testTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        System.out.println(zonedDateTime);
    }
}
