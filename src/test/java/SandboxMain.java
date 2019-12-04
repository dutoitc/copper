import java.io.UnsupportedEncodingException;

/**
 * Created by xsicdt on 07/11/17.
 */
public class SandboxMain {

    public static String convert(String s) throws UnsupportedEncodingException {
        byte[] bytes = s.getBytes("UTF-8");
        for (int i=0; i<bytes.length; i++) {
            System.out.println((int)bytes[i]);
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
