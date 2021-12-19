/**
 * Submitted by: Ridamapreet Singh
 * on:Monday 19 April
 */


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Array;
import java.sql.SQLException;
import java.util.*;

public class MobileDevice {

    private String testHash = "";
    public String deviceHash;
    private Map<String, String> contacted;
    private Map<String, ArrayList<String>> storeMeetings;
    private boolean testReport;
    Government gov1;

    /**
     * Constructor MobileDevice take input as the path to the configFile that needs to be read and then reads the file.
     * It further sends the address and the name of the device to the encryption function where these details about
     * the device will be encrypted and fed into the variable deviceHash, deviceHash of each device will be used
     * while making contacts with other devices.
     * @param configFile
     * @param contactTracer
     */
    public MobileDevice(String configFile, Government contactTracer)
    {

        FileInputStream f1 = null;
        try {
            f1 = new FileInputStream(configFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
        Properties pr = new Properties();
        try {
            pr.load(f1);
            String individual1=pr.getProperty("address");
            individual1+=pr.getProperty("name");
            individual1=encryptDeviceUser(individual1);
            this.deviceHash=individual1;

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        this.contacted = new HashMap<>();
        this.gov1 = contactTracer;
        this.storeMeetings=new HashMap<>();
    }

    /**
     * Method performs encryption on the device name and address and then returns a hexadecimal string which
     * will be the encrypted identity of the device.
     * @param deviceInfo
     * @return
     */

    public  String encryptDeviceUser(String deviceInfo)
    {
        if(deviceInfo.length()<1){return null;}
        try {
            deviceInfo.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        MessageDigest messDig = null;
        try {
            messDig = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        messDig.update(deviceInfo.getBytes());
        byte[] digest = messDig.digest();
        StringBuffer hexStr = new StringBuffer();
        for (int i = 0;i<digest.length;i++)
        {
            String hex = Integer.toHexString(0xFF & digest[i]);
            if (hex.length() == 1)
            {
                hexStr.append('0');
            }
            hexStr.append(hex);
        }

        return hexStr.toString();
    }


    /**
     * The mrthod recordContact records all the people you have met on the date and the duration of that meeting into a map, this map is later used while packing
     * up information for the government class.
     *
     * NOTE-It is important to record devices correctly for this whole project to work properly. Improper recording of devices will result in ambiguities for example:
     * A.recordContact(B.deviceHash,40,40);
     * does not mean that B automaticaly registers that it met A on day 40 for 40 minutes, one must record each contact properly, to solve this you should make B recordCpntact with A
     * B.recordContact(A.deviceHash,40,40);
     *
     *
     * @param individual
     * @param date
     * @param duration
     */


    public void recordContact(String individual, int date, int duration)
    {
        if(individual!=null && date>=0 && duration>0)
        {
            /**
             * Format documenting
             *
             * contact meeting date and the duration are seperated by ','.
             */

            String individualString = individual + "," + date + "," + duration;
            if (storeMeetings.get(individual) != null)
            {
                storeMeetings.get(individual).add(individualString);
            } else
                {
                ArrayList<String> tempRecords = new ArrayList<>();
                tempRecords.add(individualString);
                storeMeetings.put(individual, tempRecords);
            }
            contacted.put(individual, individualString);

        }
    }

    /**
     * This method simply equates the testHash of the covid test to the variable testHash.
     * @param testHash
     */
    public void positiveTest(String testHash)
    {
        /**
         * Format documenting
         *
         * every testHash has been seperated by '/'.
         */

        this.testHash = this.testHash+"/"+testHash;
    }

    /**
     * Method synchronizeData will simply pack all the data and then send to the mobileContact method of the government using the object.
     *
     * Within this method we send data in 2 situations
     * 1. The device owner has tested positive for Covid-19.
     * 2. The device owner has not tested positive for Covid-19.
     *
     * The string is packd un the folllowing format:
     * Contacts that came in contact replaced by ';' and testHashes seperated by /''
     *
     * Based on the type of data we are sending, it is decided weather the person should be matched with the test report in the government database.
     *
     * @return
     *
     */

    public boolean synchronizeData()
    {



        StringBuilder contactInfo = new StringBuilder();
//We iterate this map, in case there are more than one meetings for each contacts.
        for (Map.Entry<String, ArrayList<String>> obj : storeMeetings.entrySet())
        {



                for(String tmpStr: obj.getValue())
                {

                    /**
                     * Format documenting
                     *
                     * everymeeting is seperated by ';'
                     */

                    contactInfo.append(tmpStr+";");

                }


        }


        if(!contactInfo.isEmpty()){
        contactInfo.deleteCharAt(contactInfo.length() - 1);}

        boolean stayAlert = false;//this variable gets information from the mobileContact method weather this device was around some one who tested positive
        if(contactInfo.length()==0)//this is for cases where contact has not been made with any of the devices.
        {//keep testing, this IF is only for when there are no users and no testHashes
            if(testHash=="")
            {

                return gov1.insertForNoContactAndTestHash(deviceHash);//equate it to stayalert

            }
            else
                {//keep testing this else is only for when there are no contacts but there are some testHashes
                    String[] arrForSendingToTheFunc=testHash.split("/");
                    return gov1.insertForNoContactsButTestPositive(deviceHash,arrForSendingToTheFunc);//equate it to stayalert

                }
        }
        else if (testHash == "")
        {
            stayAlert = gov1.mobileContact(deviceHash, contactInfo.toString());
        } else
            {

            String c1=contactInfo.toString();
            c1=c1+testHash;
            stayAlert = gov1.mobileContact(deviceHash, c1);
        }
        contacted.clear();
        storeMeetings.clear();
        testHash="";
        return stayAlert;
    }

}
