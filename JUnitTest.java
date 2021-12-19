/**
 * Submitted by: Ridamapreet Singh
 * on:Monday 19 April(Latest with 81 and 93 coverage of Government and MobileDevice)
 */



import org.junit.jupiter.api.Test;


import java.sql.PreparedStatement;


import static org.junit.jupiter.api.Assertions.*;

class JUnitTest {
    Government contactTracer = new Government("/users/ridamjaggi/IdeaProjects/Final Project/resources/config.properties");

    MobileDevice DeviceA = new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/deviceA.properties", contactTracer);
    MobileDevice DeviceB = new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/deviceB.properties", contactTracer);
    MobileDevice DeviceC = new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/deviceC.properties", contactTracer);
    MobileDevice DeviceD = new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/deviceD.properties", contactTracer);
    MobileDevice DeviceE = new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/deviceE.properties", contactTracer);
    MobileDevice DeviceF = new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/deviceF.properties", contactTracer);
    MobileDevice Person1=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/device1.properties",contactTracer);
    MobileDevice Person2=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/device2.properties",contactTracer);
    MobileDevice Person3=new MobileDevice("/users/ridamjaggi/IdeaProjects/Final Project/resources/device3.properties",contactTracer);

    /**
     * This method simply deletes all the data in the tables to ensure proper working of the JUnit.
     */


    @Test
    void cleanUpTheTables()
    {
        try {
            PreparedStatement cleanCovid_test_results=contactTracer.connection.prepareStatement("delete from covid_test_results;");

            boolean ResSetCovid_test_results=cleanCovid_test_results.execute();

            PreparedStatement cleanContacted=contactTracer.connection.prepareStatement("delete from Contacted;");

            boolean ResSetContacted=cleanContacted.execute();

            PreparedStatement cleanUsers=contactTracer.connection.prepareStatement("delete from Users;");

            boolean ResSetUsers=cleanUsers.execute();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }

    /**
     * This method is used to check if the contacts that have already met and the next time we record the meeting between same contacts at the same day,
     * the durations of these contacts gets added
     */
    @Test
    void recordContactsOnSameDayWithDifferentDurations()
    {


        Person1.recordContact(Person2.deviceHash,100,10);
        Person2.recordContact(Person1.deviceHash,100,10);

        Person2.synchronizeData();
        Person1.synchronizeData();
        Person1.recordContact(Person2.deviceHash,100,10);
        Person2.recordContact(Person1.deviceHash,100,10);// to check if the person one and person 2 have total duration of meeting as 20, you may refer to db
        Person2.synchronizeData();
        Person1.synchronizeData();

    }

    /**
     * The function is to check the working of the synchronizeData method in the class MonileDevice.
     */



    @Test
    void Testsynchronize()
    {

        DeviceA.synchronizeData();//covers cases where the device has had no contact but still wants to synchronize.
        DeviceA.recordContact(DeviceB.deviceHash,30,30);
        DeviceA.recordContact(DeviceC.deviceHash,35,35);

        DeviceB.recordContact(DeviceA.deviceHash,30,30);

        DeviceC.recordContact(DeviceA.deviceHash,35,35);

        DeviceC.positiveTest("Covid1");
        contactTracer.recordTestResult("Covid1",22,true);

        DeviceB.positiveTest("Covid2");
        contactTracer.recordTestResult("Covid2",44,true);

        assertEquals(false, DeviceB.synchronizeData());
        assertEquals(false, DeviceC.synchronizeData());


        assertEquals(true, DeviceA.synchronizeData());//since this is the first time that this device is synchronizing after being in contact with covid+ve person, we would report true because it came
        //in contact with B and C on day 30 and day 35.
        assertEquals(false, DeviceA.synchronizeData());//after we have reported true once, we wont report true until we find a new contact.

        DeviceC.positiveTest("CovidC");
        contactTracer.recordTestResult("CovidC",22,true);//here we report DeviceC as covid+ve once again so that the next time A syncs, we return true


        assertEquals(false, DeviceC.synchronizeData());
        assertEquals(true, DeviceA.synchronizeData());
        assertEquals(false, DeviceA.synchronizeData());
        Person3.positiveTest("TestForPEerson3");
        contactTracer.recordTestResult("TestForPEerson3",34,true);
        assertEquals(false,Person3.synchronizeData());//case where  a device has no meetings but wants to record a positive testHash.


    }

    /**
     * This test method checks if the cases reported beyond the 14 day period are reported false properly  and if the cases within 14 days are reported true properly.
     */

    @Test
    void TestFor14DayLimit()
    {
        Person3.recordContact(Person2.deviceHash,130,30);
        Person2.recordContact(Person3.deviceHash,130,30);

        Person3.recordContact(Person1.deviceHash,158,158);
        Person1.recordContact(Person3.deviceHash,158,158);

        Person3.positiveTest("TestforPerson3");
        contactTracer.recordTestResult("TestforPerson3",144,true);

        assertEquals(false,Person3.synchronizeData());
        assertEquals(true,Person2.synchronizeData());
        assertEquals(true,Person1.synchronizeData());
        assertEquals(false,Person2.synchronizeData());
        assertEquals(false,Person1.synchronizeData());

        Person3.recordContact(Person2.deviceHash,199,30);
        Person2.recordContact(Person3.deviceHash,199,30);

        Person3.recordContact(Person1.deviceHash,229,228);
        Person1.recordContact(Person3.deviceHash,229,228);



        Person3.positiveTest("TestforPerson3Beyond14Days");
        contactTracer.recordTestResult("TestforPerson3Beyond14Days",214,true);

        assertEquals(false,Person3.synchronizeData());//since 3 has tested positive at day 214 which is 15 days from both the meetings that took place.
        assertEquals(false,Person2.synchronizeData());//2 will return false
        assertEquals(false,Person1.synchronizeData());//1 will return false.


    }


    /**
     * This method makes entries of a day and then checs if the gatherings are reported as expected value.
     */

    @Test
    void TestfindGatheings()
    {
        DeviceA.recordContact(DeviceD.deviceHash,15,12);
        DeviceA.recordContact(DeviceF.deviceHash,15,13);
        DeviceA.recordContact(DeviceB.deviceHash,15,13);
        DeviceA.recordContact(DeviceC.deviceHash,15,11);


        DeviceD.recordContact(DeviceA.deviceHash,15,12);
        DeviceD.recordContact(DeviceB.deviceHash,15,10);
        DeviceD.recordContact(DeviceC.deviceHash,15,11);
        DeviceD.recordContact(DeviceF.deviceHash,15,13);
        DeviceD.recordContact(DeviceE.deviceHash,15,12);

        DeviceB.recordContact(DeviceC.deviceHash,15,12);
        DeviceB.recordContact(DeviceD.deviceHash,15,10);
        DeviceB.recordContact(DeviceF.deviceHash,15,13);
        DeviceB.recordContact(DeviceA.deviceHash,15,13);

        DeviceC.recordContact(DeviceD.deviceHash,15,11);
        DeviceC.recordContact(DeviceF.deviceHash,15,14);
        DeviceC.recordContact(DeviceA.deviceHash,15,11);
        DeviceC.recordContact(DeviceB.deviceHash,15,12);

        DeviceF.recordContact(DeviceA.deviceHash,15,13);
        DeviceF.recordContact(DeviceB.deviceHash,15,13);
        DeviceF.recordContact(DeviceD.deviceHash,15,13);
        DeviceF.recordContact(DeviceE.deviceHash,15,11);
        DeviceF.recordContact(DeviceC.deviceHash,15,14);

        DeviceE.recordContact(DeviceF.deviceHash,15,11);
        DeviceE.recordContact(DeviceD.deviceHash,15,12);


        DeviceA.synchronizeData();
        DeviceB.synchronizeData();
        DeviceC.synchronizeData();
        DeviceD.synchronizeData();
        DeviceE.synchronizeData();
        DeviceF.synchronizeData();

        /*System.out.println(contactTracer.findGatherings(15,3,11,0.4f));
        System.out.println(contactTracer.findGatherings(15,4,11,0.4f));
        System.out.println(contactTracer.findGatherings(15,6,11,0.4f));*/

        assertEquals(3,contactTracer.findGatherings(15,3,11,0.4f));
        assertEquals(2,contactTracer.findGatherings(15,4,11,0.4f));
        assertEquals(1,contactTracer.findGatherings(15,6,11,0.4f));


    }

    /**
     * This method is invoked to cover some of the error conditions.
     */


    @Test
    void ErrorConditions()
    {

        System.out.println("Erros invoked for error conditions are displayed as follows:");
        Government ErrorObjectforGov=new Government("");
        MobileDevice ErrorObjforMob=new MobileDevice("",ErrorObjectforGov);

        ErrorObjectforGov.recordTestResult("",3,true);
        ErrorObjforMob.synchronizeData();
        ErrorObjectforGov.findGatherings(3,33,3,1);


    }




}