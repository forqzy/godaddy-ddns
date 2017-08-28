package com.istep;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by forqzy@gmail.com on 2017/8/28.
 * <p>
 * <p>
 * Reference:
 * Author£ºJellyCai
 * Link£ºhttp://www.jianshu.com/p/0842b888d94a
 */


public class Godaddy_ddns {
    private final String USER_AGENT = "Mozilla/5.0";
    private static boolean keepRunning =true;

    public static void main(String[] args) throws Exception {
        String ipaddress = "";
        String domain = "yourdomain.com";
        String subdomain ="@";
        String key ="yourkey"; //https://developer.godaddy.com/keys/.
        String secret = "yoursecret"; //https://developer.godaddy.com/keys/.
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        if(args.length==0){
            Properties p=new Properties();
            p.load(new FileInputStream("./godaddy_ddns.prop"));

            domain = p.getProperty("domain").trim();
            subdomain = p.getProperty("subdomain").trim();
            key = p.getProperty("key").trim();
            secret = p.getProperty("secret").trim();
        }

        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                keepRunning = false;
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Godaddy_ddns http = new Godaddy_ddns();
        do {
            try {
                String myIP = http.sendGet();
                if (myIP != null && !myIP.equalsIgnoreCase(ipaddress)) {
                    Date today = Calendar.getInstance().getTime();
                    String reportDate = df.format(today);


                    System.out.println(String.format("[%s]Found IP change start change ip %s to %s",reportDate,ipaddress,myIP));
                    if(http.sendPost(domain,subdomain,myIP,key,secret)) {
                        ipaddress = myIP;
                    }

                }
            } catch (Exception exp) {
                exp.printStackTrace();
            }

            try{
                Thread.sleep(10000);
            }catch (Exception ignore){

            }
        } while (keepRunning);
    }


    private String sendGet() throws Exception {
        String url = "http://ipv4.icanhazip.com/";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
//        con.setRequestProperty("Connection", "close");


        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;

        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        con.disconnect();


        if (responseCode == 200) {
            System.out.println("My IP address is:"+response.toString());
            return response.toString().trim();
        } else {
            System.out.println("Not able to get the public ipaddress from "+url);
            System.out.println("Response Code : " + responseCode);
            System.out.println(response.toString());
            return null;
        }
    }

    private boolean sendPost(String domain,String subdomain,String ip,String key,String secret) throws Exception {
        String url = String.format("https://api.godaddy.com/v1/domains/%s/records/A/%s",domain,subdomain);
        String json ="[{" +
                "\"data\":\""+ip+"\","+
                "\"ttl\":600,"+
                "\"name\":\"@\","+
                "\"type\":\"A\""+
                "}]";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();


        con.setRequestMethod("PUT");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type","application/json");
        con.setRequestProperty("Accept","application/json");
        con.setRequestProperty("Authorization", String.format("sso-key %s:%s",key,secret));
//        con.setRequestProperty("Connection", "close");
        con.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(json);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();

        System.out.println("\nSending 'PUT' request to URL : " + url);
        System.out.println("Post parameters : " + json);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        con.disconnect();

        System.out.println(response.toString());

        if(responseCode ==200){
            System.out.println(String.format("Change dns %s.%s to %s:\r\n"+response.toString(),subdomain,domain,ip));

            return true;
        }else {
            System.out.println("Not able to update the godaddy dns ");
            System.out.println("Response Code : " + responseCode);
            return false;
        }
    }

}
