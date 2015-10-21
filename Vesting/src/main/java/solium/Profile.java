package solium;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * Created by Yong.L on 8/30/2015.
 */
public class Profile {

    private String employeeId;
    private boolean bonusAwarded = false;
    private LinkedList<Entry> entries = new LinkedList<>();
    private LinkedList<Entry> saleEntries = new LinkedList<>();
    private Entry perfBonus = null;

    public Profile(String employeeId) {
        this.employeeId = employeeId;
    }

    public LinkedList<Entry> getEntries() {
        return entries;
    }
    public LinkedList<Entry> getSaleEntries() {
        return saleEntries;
    }

//    public void setEntries(LinkedList<Entry> entries) {
//        this.entries = entries;
//    }

    public void setPerfBonus(Entry perfBonus) {
        this.perfBonus = perfBonus;
        bonusAwarded = true;
    }
    public Entry getPerfBonus() {
        return perfBonus;
    }
    public String getEmployeeId() {
        return employeeId;
    }
    public boolean isBonusAwarded() {
        return bonusAwarded;
    }

}

class Entry implements Comparable<Entry>{
    private RecordType type;
    private String date;
    private int amount;
    private BigDecimal price;

    Entry(RecordType type, String date, int amount, BigDecimal price) {
        this.type = type;
        this.date = date;
        this.amount = amount;
        this.price = price;  // VEST-grant price, PERF-multiplier, SALE-sale price
    }

    public String getDate() {
        return date;
    }
    public BigDecimal getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public boolean occuredBeforeOrOn(String date) {
        return (this.getDate().compareTo(date) <= 0);
    }

    /**
     * use date string natural order for comparison
     */
    @Override
    public int compareTo(Entry entry2) {
         return this.getDate().compareTo(entry2.getDate()) ;
    }
}
