/**
 * Submitted by: Ridamapreet Singh
 * on:Monday 19 April
 */


public class covid_test_result {

    private String testHash;
    private int date;
    private Boolean result;



    public covid_test_result(String testHash, int date,Boolean result )
    {
        this.testHash = testHash;
        this.result = result;
        this.date = date;
    }

    public String getTestHash() {
        return testHash;
    }

    public void setTestHash(String testHash) {
        this.testHash = testHash;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }


    @Override
    public String toString() {
        return "covid_test_result{" +
                "testHash='" + testHash + '\'' +
                ", result=" + result +
                ", date=" + date +
                '}';
    }
}
