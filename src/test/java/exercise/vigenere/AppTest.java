package exercise.vigenere;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AppTest {

    @Test
    public void testSanity() {
        System.out.println("JUnit works");
        App app = new App();
        
        assertEquals(">2:IH,//:/", app.encrypt("encrypt", "top secret"));
        assertEquals("top secret", app.decrypt("encrypt", ">2:IH,//:/"));
                
    }
}
