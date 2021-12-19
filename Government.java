/**
 * Submitted by: Ridamapreet Singh
 * on:Monday 19 April
 */




import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.util.*;

public class Government {


    private String url;
    private String username;
    private String password;
    private List<covid_test_result> testRecords = new ArrayList<>();
    private Map<String, List<Contact>> globalContactList = new HashMap<>();
    public ArrayList<ArrayList<Integer>> setOfPeople=new ArrayList<>();
    private Set<String> usersEntered=new HashSet<>();
    private Map<Integer,ArrayList<String>> mp=new HashMap<>();
    private ArrayList<String> stringForRecorded=new ArrayList<>();

    Connection connection = null;
    ResultSet result1 = null;
    private boolean resultInsertUsers;
    private boolean InsertIntoCovidTable;
    private boolean IdentifyTest;
    private PreparedStatement stCheckGatheringNew11;

    /**
     * The Government object takes up parameters as the file path and then loads the name of the database
     * the name of the user and the password of the database.
     * @param configFile
     */
    public Government(String configFile) {

        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex)
        {
            System.out.println("Error connecting to jdbc");
        }

        FileInputStream f1 = null;
        try {
            f1 = new FileInputStream(configFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        Properties pr = new Properties();
        try {
            pr.load(f1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        url = pr.getProperty("database");

        username = pr.getProperty("user");
        password = pr.getProperty("password");
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    /**
     * this function records that a test has been conducted, then furthur sends the results to the function where it will be inserted in the db.
     *
     * @param testHash
     * @param date
     * @param result
     */
    public void recordTestResult(String testHash, int date, boolean result)
    {

        covid_test_result obj1 = new covid_test_result(testHash, date, result);
        testRecords.add(obj1);
        sendResultToGovt(testRecords);//send the test report results to govt db.

    }

    /**
     * to check if this person who has called synchronize has been in contact with any one who has tested positive in absolute(+ and -) 14 days.
     * If yes then the function returns true, if no then the function returns false.
     *
     *
     *
     * @param initiator
     * @return
     */
    private boolean check_for_alert(String initiator)
    {

        int flag = 0;//this flag will denote if
        PreparedStatement checkForInfection = null;
        try {
            checkForInfection = connection.prepareStatement("select * from Contacted inner join covid_test_results on Contacted.sourceFkID=covid_test_results.UserID join Users on" +
                    "( Users.UserID=Contacted.contactedFkID or Users.UserID=Contacted.SourceFkID)" +
                    "where Users.deviceHash=?" +
                    "and covid_test_results.result=1 and Contacted.contactedDate>=Date_sub(covid_test_results.date,interval 14 day) and " +
                    "Contacted.contactedDate<=date_add(covid_test_results.date, interval 14 day) and" +
                    "(Users.lastSync is Null or covid_test_results.createdTime>=Users.lastSync) and (covid_test_results.UserID<>(select UserID from Users where deviceHash=?));");

            checkForInfection.setString(1,initiator);
            checkForInfection.setString(2,initiator);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            checkForInfection.setString(1, initiator);


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            ResultSet resultCheckForInfection = checkForInfection.executeQuery();


/**
 * The function when returns true indicates that the person has come in contact with a covid positive person in the 14 days time period, now since we will be returning
 * true for all the cases that have been reported until now, we should not report true for the same cases. To make this possible I set the last sync time of this
 * specific user to the current time, so that the next time this device syncs with database to check if he/she came in contact with someone positive, only new records
 * are reported back, if there are no new records, then we return false.
 */

            if(resultCheckForInfection.next()==true)
            {//if the program enters this loop it means that device synchronizing has met someone who tested positive in the 14 days absolute period from their meeting date

             PreparedStatement updateTheLastSyncTime=connection.prepareStatement("Update Users set lastSync=now() where deviceHash=?");
             updateTheLastSyncTime.setString(1,initiator);

             boolean resultSetForUpdatingLastSyncTime=updateTheLastSyncTime.execute();
                return true;

            }

           return false;



        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        return false;
    }

    /**
     * findGatherings is a method which is used to find number of gatherings at a particular date and then for each pair that came in contact that day
     * the method finds out common set of people, furthur it forms pairs of devices and then checks from the pairs that contacted that day for minimum
     * duration and then performs tha calculation as outlined in the pdf given by professor in order to find the total number of gatherings.
     * @param date
     * @param minSize
     * @param minTime
     * @param density
     * @return
     */



    int findGatherings(int date, int minSize, int minTime, float density )
    {
        int gatheringCount=0;
        Set<ArrayList<Integer>> tempForNew=new HashSet<>();//this will be used to store the pairs that satisfy the condition of meeting that day and for minTime
        Map<Integer,ArrayList<Integer>> mapForCluster=new HashMap();//this map contains key as an individual and the arraylist in fornt of it contains the devices it has met at that day
        Map<ArrayList<Integer>,ArrayList<ArrayList<Integer>>> pairs=new HashMap<>();
        PreparedStatement stCheckGatheringNew = null;
        Set<ArrayList<Integer>> allOfTheMembersAtThatDay=new HashSet<>();//contains all of the pairs that met that day
        try {
            stCheckGatheringNew11 = connection.prepareStatement("select * from Contacted where " +
                    "contactedDate=(DATE_ADD('2021-01-01',INTERVAL" + "\"" + date + "\"" + "DAY))"+";");

            ResultSet r1=stCheckGatheringNew11.executeQuery();
            while(r1.next())
            {
                ArrayList<Integer> temp1=new ArrayList<>();
                temp1.add(r1.getInt("sourceFkID"));
                temp1.add(r1.getInt("contactedFkID"));
                Collections.sort(temp1);
                allOfTheMembersAtThatDay.add(temp1);

            }
          //  System.out.println("New one "+allOfTheMembersAtThatDay);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            stCheckGatheringNew = connection.prepareStatement("select * from Contacted where " +
                    "contactedDate=(DATE_ADD('2021-01-01',INTERVAL" + "\"" + date + "\"" + "DAY)) and contactDuration>="+ minTime+";");
        } catch  (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
          ResultSet CheckGatheringNew = stCheckGatheringNew.executeQuery();

           while(CheckGatheringNew.next())
          {
              ArrayList<Integer> temp=new ArrayList<>();

              temp.add(CheckGatheringNew.getInt("sourceFkID"));
              temp.add(CheckGatheringNew.getInt("contactedFkID"));
              Collections.sort(temp);
              tempForNew.add(temp);//this is only for the cases where we need to know if the pair has appeared in the specific day and at the specific time .
          }
            /**
             * We start now by iterating the pairs that met that day and then we check all the people every pair met, then we perform intersection on them
             */

            Set<Set<Integer>> finalSet=new HashSet<>();
           for(ArrayList<Integer> i1:allOfTheMembersAtThatDay)
           {



               for(Integer i2:i1)//picking each element like A and B and then finding out the people that they have been in contact with that day
               {
                   PreparedStatement getContactsOfThisID = connection.prepareStatement("select contactedFkID from Contacted where contactedDate=(DATE_ADD('2021-01-01',INTERVAL ? DAY)) and sourceFkID=?;");
                   getContactsOfThisID.setInt(1,date);

                   getContactsOfThisID.setInt(2,i2);

                   ResultSet sq1=getContactsOfThisID.executeQuery();
                   //was in

                   while (sq1.next())//feeding the devices that have interacted with one device into the map, we now have all the devices that interacted with each device that day
                   {
                       if(mapForCluster.containsKey(i2)==false)
                       {
                           ArrayList<Integer> tempList=new ArrayList<>();
                           mapForCluster.put(i2,tempList);
                       }
                       if(mapForCluster.get(i2).contains(sq1.getInt("contactedFkID"))==false) {
                           mapForCluster.get(i2).add(sq1.getInt("contactedFkID"));
                          // System.out.println(sq1.getInt("contactedFkID"));
                       }
                       ArrayList<Integer> tp1=new ArrayList<>();
                       tp1.add(i2);
                       tp1.add(sq1.getInt("contactedFkID"));
                       Collections.sort(tp1);


                   }
                    if(mapForCluster.get(i2).contains(i2)==false)
                    {
                        mapForCluster.get(i2).add(i2);
                    }

               }
               TreeSet<Integer> s1=new TreeSet<>();
               TreeSet<Integer> s2=new TreeSet<>();
              //  s1.add(i1.get(1));//to check pair
              //  s2.add(i1.get(0));//to check pair
                for(Integer i:mapForCluster.get(i1.get(0)))
                {
                    s1.add(i);
                }
               for(Integer i:mapForCluster.get(i1.get(1)))
               {
                   s2.add(i);
               }
               s1.retainAll(s2);//intersection of sets of people pair AB met

               if(finalSet.contains(s1)==false) {//here we are checking if the gathering has already been reported. if yes then we dont report it again

                   ArrayList<Integer> s1Duplicate = new ArrayList<>();
                   if (s1.size() >= minSize) {
                       for (Integer in1 : s1) {
                           s1Duplicate.add(in1);
                       }


                       int count = 0;
                       ArrayList<ArrayList<Integer>> arrForCalcOfPairs = new ArrayList<>();
                       for (int i = 0; i < s1Duplicate.size(); i++)//manual formation of pairs
                       {

                           ArrayList<Integer> temp2 = new ArrayList<>();
                           for (int j = i + 1; j < s1Duplicate.size(); j++)
                           {
                               ArrayList<Integer> tempArrList = new ArrayList<>();

                               tempArrList.add(s1Duplicate.get(i));
                               tempArrList.add(s1Duplicate.get(j));
                               Collections.sort(tempArrList);

                               if (tempForNew.contains(tempArrList) == true) {//we check here if the pair we are talking about satisfies the minTime condition also
                                   count++;
                               }
                               tempArrList.clear();
                           }


                       }

                       finalSet.add(s1);

                       int c = count;
                       int n = s1.size();
                       int m = n * (n - 1) / 2;
                       float resultant = ((float) c / (float) m);

                       if (resultant > density) {
                           gatheringCount++;
                       }

                   }
               }


           }
//System.out.println(finalSet);


return gatheringCount;

      }
      catch (Exception e)
      {
          System.out.println(e.getMessage());
      }



      return 0;
    }



    /**
     * This method specificaly handles cases where the device has never been in contact with any other device but has reported positive testHash
     *
     * @param initiator
     *
     */
    public boolean insertForNoContactsButTestPositive(String initiator,String[] s2)
    {
        PreparedStatement checkUserExist= null;
        try {
            checkUserExist = connection.prepareStatement("select UserID from Users where deviceHash=?");
        }  catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        try {
            checkUserExist.setString(1,initiator);
        }  catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        ResultSet r1= null;
        try {
            r1 = checkUserExist.executeQuery();
        }  catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        try {
            if(r1.next()==false)//this means that the device is not present in the users table and needs to be inserted.
            {
                PreparedStatement insertUser=connection.prepareStatement("insert into Users(deviceHash) values(?)");
                insertUser.setString(1,initiator);
                boolean r2=insertUser.execute();

            }
        }  catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        resultIdentification(initiator,s2);
        return check_for_alert(initiator);
    }

    /**
     * This function has been made to handle the cases where the device has not been in contact with any device and has not even reported a positive test hash.
     * @param deviceHash
     * @return
     */

    public boolean insertForNoContactAndTestHash(String deviceHash)
    {//this function creates a new user only when there are o testHash for the device as well as no contacts for that person
        if (connection != null && username != null && password != null){
            PreparedStatement checkforuser = null;
        try {
            checkforuser = connection.prepareStatement("select UserID from Users where deviceHash=?");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            checkforuser.setString(1, deviceHash);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        ResultSet rs1 = null;
        try {
            rs1 = checkforuser.executeQuery();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            if (rs1.next() == false)//this means that this device is not a part of the user tabke and needs to be inserted in the table
            {

                PreparedStatement inserUserOnly = connection.prepareStatement("insert into Users(deviceHash) values(?);");
                inserUserOnly.setString(1, deviceHash);

                boolean r2 = inserUserOnly.execute();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return check_for_alert(deviceHash);
    }

            return false;

    }

    /**
     * This function has been made to send the records of the mobile phone to the database, records such as the devices this device has met, the date and the
     * duration is also feeded to the database from this function.
     * @param initiator
     * @param contactInfo
     * @return
     */
    public boolean mobileContact(String initiator, String contactInfo)
    {
        if(connection==null)
        {
            return false;
        }

        int f=0;
        String[] s1=contactInfo.split("/");//to seperate the contacts reported by the testHashes reported
        if(s1.length>1)//this checks if there has been any testHash that has been reported and then sets the f=1, so that we know that we need to record
        {//this entry in the covid_result database.
            f=1;



        }

        if(contactInfo.length()!=0)
       {

            contactInfo=s1[0].toString();
            String[] arr = contactInfo.split(";");//here all of the contacts will be feeded to arr.

            ArrayList<Contact> contactList = new ArrayList<>();//will store all of the people that came contact with this device.
            for (int i = 0; i < arr.length; i++)
            {
                String[] temp = arr[i].split(",");//I have seperated the date and duration of the meeting with a device by ','
                Contact contact = new Contact();

                contact.setPersonHash(temp[0]);
                contact.setDays(Integer.valueOf(temp[1]));
                contact.setTime(Integer.valueOf(temp[2]));
                contactList.add(contact);
            }

            if (!globalContactList.containsKey(initiator))
            {
                ArrayList<Contact> list = new ArrayList<>();
                globalContactList.put(initiator, list);
            }

            for (Contact obj : contactList)
            {

                globalContactList.get(initiator).add(obj);
            }

            for (Map.Entry<String, List<Contact>> entry : globalContactList.entrySet())
            {
                PreparedStatement stmt1= null;
                try {
                    stmt1 = connection.prepareStatement("select UserID from Users where deviceHash=?");
                    stmt1.setString(1,entry.getKey());
                }  catch (Exception e)
                {
                    System.out.println(e.getMessage());
                }
                try{
                ResultSet RSstmt1= stmt1.executeQuery();
                    if (RSstmt1.next()== false)//means that this is verified that this user does not exist in the db.
                    {
                        PreparedStatement stInsertUsers = null;
                        try {
                            stInsertUsers = connection.prepareStatement("insert into Users(deviceHash) values(" + "\"" + entry.getKey() + "\"" + ");");


                            resultInsertUsers = stInsertUsers.execute();
                        }  catch (Exception e)
                        {
                            System.out.println(e.getMessage());
                        }
                        usersEntered.add(entry.getKey());

                    }

                }
                catch (Exception e){System.out.println(e.getMessage());}


                for (Contact con : entry.getValue())//register the users that have interacted with this device in the central database.
                {

                    PreparedStatement stmt2 = null;
                    try {
                        stmt2 = connection.prepareStatement("select UserID from Users where deviceHash=?");
                    }  catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                    try {
                        stmt2.setString(1, con.getPersonHash());
                    }  catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                    ResultSet RSstmt2 = null;
                    try {
                        RSstmt2 = stmt2.executeQuery();
                    }  catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }

                    try {
                        if (RSstmt2.next() == false) {//users that are in contact with the key of this map will be put now in the table
                            try {
                                PreparedStatement insertToUser = connection.prepareStatement("insert into Users(deviceHash) values(" + "\"" + con.getPersonHash() + "\"" + ");");
                                boolean resultToInsertUsers = insertToUser.execute();

                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }  catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                }


                }
            }

/**
 * Will be using this to insert the contacts who have been in contact with each other, hence record that they have met each other
 */
            for (Map.Entry<String, List<Contact>> entry : globalContactList.entrySet())
            {
                for (Contact con : entry.getValue()) {
                   if (checkIfSameMeeting(entry.getKey(), con.getPersonHash(), con.getDays(), con.getTime()) == false){


                        try {
                            PreparedStatement stInsertContacted = connection.prepareStatement("insert into Contacted (sourceFkID,contactedFkID,contactedDate,contactDuration) " +
                                    "values((select UserID from Users where deviceHash=" + "\"" + entry.getKey() + "\"" + ")," +
                                    "(select UserID from Users where deviceHash=" + "\""
                                    + con.getPersonHash() + "\")" + ",(DATE_ADD('2021-01-01',INTERVAL " + con.getDays() + " DAY))," + con.getTime() + ");");

                            boolean resultInsertContacted = stInsertContacted.execute();
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                }

                }
            }
            globalContactList.clear();//this is only used for one session hence we clear the list
            if (f == 1) {//this flag is being used to make sure that because this user was tested positive it is a
                //gurantee that the testHash for this user is available and hence should be mapped correctly.here s1 is the stirng which will contain the testHashes
                resultIdentification(initiator, s1);
            }

        return check_for_alert(initiator);
    }

    /**
     * This function checks if a meeting involving the same device at the same day has occured, if yes then it updates the duration as the sum of previous and
     * new durations. It then returns true after updating the durations.
     * @param key
     * @param personHash
     * @param days
     * @param time
     * @return
     */

    private boolean checkIfSameMeeting(String key, String personHash, int days, int time) {

        try {
            int f=0;
            PreparedStatement stmntToCheckForDuplicateMeet = connection.prepareStatement("select * from Contacted where SourceFkID=(select UserID from Users where deviceHash=?) and ContactedFkID=(select UserID from Users where deviceHash=?) and contactedDate=(DATE_ADD('2021-01-01',INTERVAL ? DAY));");
            stmntToCheckForDuplicateMeet.setString(1, key);
            stmntToCheckForDuplicateMeet.setString(2, personHash);
            stmntToCheckForDuplicateMeet.setInt(3, days);


            ResultSet rsforstmntToCheckForDuplicateMeet = stmntToCheckForDuplicateMeet.executeQuery();
            //by executing the above query we are asking the db to report if the meeting between the two devices has already taken place at this time and if
            //yes we then get the old time of the meeting and add the new time to it and then update the entry in the database.
            while(rsforstmntToCheckForDuplicateMeet.next())
             {
                 f=1;
                Integer oldDuration = rsforstmntToCheckForDuplicateMeet.getInt("contactDuration");
                Integer meetID = rsforstmntToCheckForDuplicateMeet.getInt("ContactedID");
                oldDuration = oldDuration + time;

                PreparedStatement updateMeetingTime = connection.prepareStatement("update Contacted set contactDuration=? where ContactedID=?");
                updateMeetingTime.setInt(1, oldDuration);
                updateMeetingTime.setInt(2, meetID);

                updateMeetingTime.execute();


            }

                if(f==1)//this flag denotes if the we found an existing entry for the meeting and we have succesfuly updated the entries.
                {return true;}//we return true because we dont want the mobileContact method to insert a duplicate entry for the same meeting.
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }


        return false;//if the meeitng was not found we return false.
    }

    /**
     * this function will identify the person who has been tested positive for covid and then will update that this person has been
     * tested postitive to the government db by populating the UserID field corrosponding to the testHash.
     *
     * @param initiator
     *
     *
     */
    private void resultIdentification(String initiator, String[] arrOftestHash) {

        if (connection != null && username != null && password != null){
            PreparedStatement stIdentifyTest = null;
        try {
            stIdentifyTest = connection.prepareStatement("select UserID from Users where deviceHash=" + "\"" + initiator + "\"" + ";");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        ResultSet I1 = null;
        try {
            I1 = stIdentifyTest.executeQuery();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            I1.next();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        for (int i = 1; i <= arrOftestHash.length - 1; i++) {

            try {
                stIdentifyTest = connection.prepareStatement("update covid_test_results set UserID=" + "\"" + I1.getString("UserID") + "\"" +
                        "where testHash=" + "\"" + arrOftestHash[i] + "\"" + ";");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            try {
                IdentifyTest = stIdentifyTest.execute();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    }

    /**
     * This function is used to insert the tests into the db, which will then be used to make decisions about which device has tested positve and which
     * device has come in contact with a covid positive device
     *
     * @param testRecords
     * @param
     *
     */
    private void sendResultToGovt(List<covid_test_result> testRecords) {

        if(connection!=null&& username!=null&& password!=null){//meaning that there was some error in the config file
        //to-do partition the testerecords into List  of list
        //put one by one and empty the tesrecords.
        int len = 0;
        if (testRecords.size() > 0) {
            String forInsertion = "";
            for (covid_test_result c1 : testRecords) {
                PreparedStatement checkForTestRecords = null;
                try {
                    checkForTestRecords = connection.prepareStatement("select * from covid_test_results where testHash=?");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                try {
                    checkForTestRecords.setString(1, c1.getTestHash());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                ResultSet temp = null;
                try {
                    temp = checkForTestRecords.executeQuery();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                /**
                 * the if statement below is included to check if there already exists a record with same testhash, if so we dont want to create duplicates in the table.
                 */
                try {
                    if (temp.next() == false) {
                        len++;
                        if (len != testRecords.size()) {

                            forInsertion = forInsertion + "(" + "\"" + c1.getTestHash() + "\"" + "," + "(DATE_ADD('2021-01-01',INTERVAL " + c1.getDate() + " DAY))," + c1.getResult() + ",now()),";
                        } else {
                            forInsertion = forInsertion + "(" + "\"" + c1.getTestHash() + "\"" + "," + "(DATE_ADD('2021-01-01',INTERVAL " + c1.getDate() + " DAY))," + c1.getResult() + ",now())";
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            }

            if (forInsertion.length() > 0) {//have created this statement specifically to make sure if there already is a testreport by the teshash do not create duplicates
                PreparedStatement stInsertIntoCovidTable = null;
                try {
                    stInsertIntoCovidTable = connection.prepareStatement("insert into covid_test_results(testHash,date,result,createdTime) values" + forInsertion + ";");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                try {
                    InsertIntoCovidTable = stInsertIntoCovidTable.execute();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            testRecords.clear();
        }
    }
    }
}



