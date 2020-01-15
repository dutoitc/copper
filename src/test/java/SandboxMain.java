import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Created by xsicdt on 07/11/17.
 */
public class SandboxMain {

    public static String convert(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        for (final byte aByte : bytes) {
            System.out.println((int) aByte);
        }

        return "";
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String s="éà";
        System.out.println(convert(s));

        /*System.out.println(new String(s.getBytes("ISO-8859-1"), "UTF-8"));
        for (int i=0; i<s.length(); i++) {
            System.out.println((int)s.charAt(i)+" " + s.charAt(i));
        }*/
    }

}
