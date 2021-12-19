/**
 * Submitted by: Ridamapreet Singh
 * on:Monday 19 April
 */


import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;

public class Main {

    //refference "https://www.geeksforgeeks.org/generate-random-string-of-given-size-in-java/"


    public static void main(String[] args)
    {
        try {
            Government contactTracer=new Government("/users/ridamjaggi/IdeaProjects/Final Project/resources/config.properties");
MobileDevice Person1=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/device1.properties",contactTracer);
MobileDevice Person2=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/device2.properties",contactTracer);
MobileDevice Person3=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/device3.properties",contactTracer);
MobileDevice Person4=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/device4.properties",contactTracer);
MobileDevice Person5=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/device5.properties",contactTracer);
MobileDevice Person6=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/device6.properties",contactTracer);
MobileDevice Person7=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/device7.properties",contactTracer);
MobileDevice Person8=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/device8.properties",contactTracer);
MobileDevice DeviceA=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/deviceA.properties",contactTracer);
MobileDevice DeviceB=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/deviceB.properties",contactTracer);
MobileDevice DeviceC=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/deviceC.properties",contactTracer);
MobileDevice DeviceD=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/deviceD.properties",contactTracer);
MobileDevice DeviceE=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/deviceE.properties",contactTracer);
MobileDevice DeviceF=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/deviceF.properties",contactTracer);




            DeviceA.recordContact(DeviceB.deviceHash,130,2);
            DeviceB.recordContact(DeviceA.deviceHash,130,2);


            DeviceA.recordContact(DeviceC.deviceHash,133,3);
            DeviceC.recordContact(DeviceA.deviceHash,133,3);

            DeviceC.recordContact(DeviceD.deviceHash,135,3);
            DeviceD.recordContact(DeviceC.deviceHash,135,3);



            DeviceB.positiveTest("TestB");
            contactTracer.recordTestResult("TestB",143,true);



            contactTracer.recordTestResult("TestC",145,true);
            DeviceC.positiveTest("TestC");

            System.out.println(DeviceB.synchronizeData());
            System.out.println(DeviceC.synchronizeData());
            System.out.println(DeviceA.synchronizeData());
            System.out.println(DeviceA.synchronizeData());
            System.out.println(DeviceD.synchronizeData());

            DeviceC.positiveTest("TestC2");
            contactTracer.recordTestResult("TestC2",149,true);


            System.out.println(DeviceC.synchronizeData());
            System.out.println(DeviceA.synchronizeData());
            System.out.println(DeviceD.synchronizeData());
            System.out.println(DeviceD.synchronizeData());


DeviceE.positiveTest("check1");
contactTracer.recordTestResult("check1",30,true);
DeviceE.positiveTest("check2");
contactTracer.recordTestResult("check2",30,true);
DeviceE.synchronizeData();
DeviceE.positiveTest("check3");
contactTracer.recordTestResult("check3",56,true);
DeviceE.synchronizeData();


contactTracer.connection.close();

        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }


    }
}
