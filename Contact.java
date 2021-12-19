/**
 * Submitted by: Ridamapreet Singh
 * on:Monday 19 April
 */


public class Contact {
    private String personHash;
    private int days;
    private int time;

    public Contact() {
    }

    public Contact(String personHash, int days, int time) {
        super();
        this.personHash = personHash;
        this.days = days;
        this.time = time;
    }

    public String getPersonHash() {
        return personHash;
    }

    public void setPersonHash(String personHash) {
        this.personHash = personHash;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Contact [personHash=" + personHash + ", days=" + days + ", time=" + time + "]";
    }

}
