package animeflooding;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class AnimeFlooding {
  static int subDirNum = 0;
  static int avanzamento = 0;
  
  public static void main(String[] args) throws NoSuchAlgorithmException {
              TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };
      SSLContext sc = SSLContext.getInstance("SSL");;
      try {
          sc = SSLContext.getInstance("SSL");
      } catch (NoSuchAlgorithmException ex) {
          Logger.getLogger(AnimeFlooding.class.getName()).log(Level.SEVERE, null, ex);
      }
      try {
          sc.init(null, trustAllCerts, new java.security.SecureRandom());
      } catch (KeyManagementException ex) {
          Logger.getLogger(AnimeFlooding.class.getName()).log(Level.SEVERE, null, ex);
      }
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    Gui gui = new Gui();
  }
  
  public static void scorrereSottocartelle(String cartellaPath, int n, boolean nsfw, int value) throws Exception {
    File cartella = new File(cartellaPath);
    if (cartella.exists() && cartella.isDirectory()) {
      File[] files = cartella.listFiles();
      if (files != null) {
        try {
          for (int i = 0; i < n; i++) {
            String waifu = getWaifu(nsfw, value);
            URL url = new URL(waifu);
            InputStream inputStream = url.openStream();
            Path destination = Paths.get(cartellaPath, new String[] { waifu.substring(23, waifu.length()) });
            Files.copy(inputStream, destination, new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
            Gui.progressBar.setValue(Gui.progressBar.getValue() + avanzamento);
            System.out.println("Aggiorno la barra");
            Gui.progressBar.update(Gui.progressBar.getGraphics());
          } 
        } catch (MalformedURLException e) {
          e.printStackTrace();
        } 
        for (File file : files) {
          if (file.isDirectory()) {
            System.out.println("Sottocartella: " + file.getAbsolutePath());
            scorrereSottocartelle(file.getAbsolutePath(), n, nsfw, value);
          } 
        } 
      } 
    } else {
      System.out.println("La cartella specificata non esiste o non una cartella.");
    } 
  }
  
  public static String getWaifu(boolean nsfw, int value) throws NoSuchAlgorithmException {
    String type = "waifu";
    String mode = "sfw";
    String waifu = "";
    try {
      if (nsfw) {
        double n = Math.random();
        if (n < value / 100.0D)
          mode = "nsfw"; 
      } 

      String apiUrl = "https://api.waifu.pics/" + mode + "/" + type;
      URL url = new URL(apiUrl);
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("GET");
      int responseCode = connection.getResponseCode();
      if (responseCode == 200) {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        try {
          StringBuilder response = new StringBuilder();
          String inputLine;
          while ((inputLine = in.readLine()) != null)
            response.append(inputLine); 
          System.out.println("API Response: " + response.toString());
          String jsonString = response.toString();
          String regex = "\"url\":\"(https://[^\"]+)\"";
          Pattern pattern = Pattern.compile(regex);
          Matcher matcher = pattern.matcher(jsonString);
          if (matcher.find()) {
            waifu = matcher.group(1);
            System.out.println("URL estratto: " + waifu);
          } else {
            System.out.println("Nessun URL trovato nella stringa.");
          } 
          in.close();
        } catch (Throwable throwable) {
          try {
            in.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
      } else {
        System.out.println("Error: Unable to fetch data from the API. Response Code: " + responseCode);
      } 
      connection.disconnect();
    } catch (Exception ex) {
          Logger.getLogger(AnimeFlooding.class.getName()).log(Level.SEVERE, null, ex);
      }
    return waifu;
  }
  
  public static int contaSottocartelle(String cartellaPath) {
    int n = 0;
    File cartella = new File(cartellaPath);
    if (cartella.exists() && cartella.isDirectory()) {
      File[] files = cartella.listFiles();
      if (files != null)
        for (File file : files) {
          if (file.isDirectory()) {
            System.out.println("Sottocartella: " + file.getAbsolutePath());
            n++;
            n += contaSottocartelle(file.getAbsolutePath());
          } 
        }  
    } else {
      System.out.println("La cartella specificata non esiste o non una cartella.");
    } 
    return n;
  }
}