import com.chrionline.client.service.AuthService;
public class RegisterProbe {
  public static void main(String[] args) throws Exception {
    try {
      String result = new AuthService().register(
        "Test",
        "Probe",
        "probe" + System.currentTimeMillis() + "@example.com",
        "Aa!23456",
        "0600000000",
        "Tetouan, Tanger-Tetouan-Al Hoceima - Centre",
        "1999-10-05"
      );
      System.out.println("OK=" + result);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
