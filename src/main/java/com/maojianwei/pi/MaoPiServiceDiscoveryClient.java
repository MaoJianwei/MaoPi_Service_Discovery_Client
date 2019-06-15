package com.maojianwei.pi;

import com.alibaba.edas.acm.ConfigService;
import com.alibaba.edas.acm.exception.ConfigException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Hello world!
 */
public class MaoPiServiceDiscoveryClient {

    //    private static final Logger logger = LoggerFactory.getLogger(MaoPiServiceDiscoveryClient.class);
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String hostname = null;
    private static int count = 0;


    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put("endpoint", "acm.aliyun.com");
        properties.put("namespace", "$namespace");
        properties.put("accessKey", "$accessKey");
        properties.put("secretKey", "$secretKey");
        // 如果是加密配置，则添加下面两行进行自动解密
        //properties.put("openKMSFilter", true);
        //properties.put("regionId", "$regionId");
//        logger.info("properties:\n{}", properties);


        ConfigService.init(properties);

        boolean hostnameReady = false;
        while (true) {
            try {
                hostname = InetAddress.getLocalHost().getHostName();
                hostnameReady = true;
                break;
            } catch (UnknownHostException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    break;
                }
            }
        }
//        logger.info("hostname: {}", hostname);

        if (hostnameReady) {
            while (true) {
                String info = getLocalServiceDiscoveryInfo();
                try {
                    ConfigService.publishConfig(hostname, "Service_Discovery", info);
//                    if () {
////                        logger.info("publish OK");
//                    } else {
////                        logger.warn("publish Fail");
//                    }
                    Thread.sleep(1000);
                } catch (ConfigException e) {
//                    logger.warn("ConfigException");
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private static String getLocalServiceDiscoveryInfo() {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("NodeName=%s\n", hostname));
        sb.append(String.format("Count=%d\n", ++count));

        try {
            Enumeration<NetworkInterface> intfs = NetworkInterface.getNetworkInterfaces();
            while (intfs.hasMoreElements()) {

                Enumeration<InetAddress> addrs = intfs.nextElement().getInetAddresses();
                while (addrs.hasMoreElements()) {
                    sb.append(String.format("IP=%s\n", addrs.nextElement().getHostAddress()));
                }
            }
        } catch (SocketException e) {
            sb.append("IP=Fail\n");
        }

//        for (InetAddress addr : InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())) {
//            sb.append(String.format("IP=%s\n", addr.getHostAddress()));
//        }

        sb.append(String.format("SysTime=%s\n", SDF.format(new Date())));

        return sb.toString();
    }
}
