package data;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WritableMutableCharSequenceTest {

    @Test
    public void testBasic() {
        WritableMutableCharSequence writableMutableCharSequence = new WritableMutableCharSequence(1024);
        writableMutableCharSequence.copy("abcde1");
        assertEquals("abcde1", writableMutableCharSequence.toString());

        writableMutableCharSequence.copy("abcde1334");
        assertEquals("abcde1334", writableMutableCharSequence.toString());

        writableMutableCharSequence.copy("abcde1");
        writableMutableCharSequence.concat("concat");
        assertEquals("abcde1concat", writableMutableCharSequence.toString());

        String s = "This is a AUD/USDgoos";
        WritableMutableCharSequence write = new WritableMutableCharSequence(20);
        int start = s.indexOf("AUD");
        int end = s.indexOf("goos");
        String string = write.append(s, start, end).toString();
        assertEquals("AUD/USD", string);
    }

    @Test
    public void testEqualAndHashCode() {
        WritableMutableCharSequence abc = new WritableMutableCharSequence(20);
        abc.copy("abc");

        WritableMutableCharSequence abc2 = new WritableMutableCharSequence(20);
        abc2.copy("abc");

        assertEquals(abc2.hashCode(), abc.hashCode());
        assertTrue(abc2.equals(abc));

        WritableMutableCharSequence other = new WritableMutableCharSequence(20);
        other.copy("aaxxdfsadfbc");

        Map<WritableMutableCharSequence, String> map = new HashMap<>();

        map.put(abc, "ABC");
        map.put(other, "next");

        assertEquals("ABC", map.get(abc2));
        assertEquals("next", map.get(other));
    }


}