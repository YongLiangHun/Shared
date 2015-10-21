package solium;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Solium Stock Option Vesting App
 *
 */
public class App 
{
    protected static Map<String, Profile> records = new HashMap<>();
    protected static String targetDate;
    protected static BigDecimal marketPrice;
    protected static DecimalFormat df = new DecimalFormat();

    public static void main( String[] args ) {
        int linesTotal=0;
        setDfFormat();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in)))
        {
            String sCurrentLine;
            //read record count N
            if (( sCurrentLine = br.readLine()) != null) {
                linesTotal = Integer.parseInt(sCurrentLine);
            }
            //read records
            if (linesTotal > 0) {
                for (int i = 1; i <= linesTotal; i++) {
                    if ((sCurrentLine = br.readLine()) != null) {
                        String[] parts = sCurrentLine.trim().split("\\s*,\\s*");
                        InputValidateUtil.validateRecordLine(parts);
                        insertProfile(parts);
                    }
                }
                //read target date and market price
                if ((sCurrentLine = br.readLine()) != null) {
                    String[] parts = sCurrentLine.split(",");
                    targetDate = parts[0];
                    marketPrice = new BigDecimal(parts[1]);
                }

                List<String> keys = new ArrayList<>(records.keySet());
                Collections.sort(keys);
                for (String key : keys) {
                    Profile p = records.get(key);
                    BigDecimal[] results= calculate(p, targetDate, marketPrice);
                    System.out.println(p.getEmployeeId() + "," + df.format(results[0]) + "," + df.format(results[1]));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * insert a record(row) into the Hashmap keeping track of input
     * @param parts : a line's input data from stdin, which was separated by ','
     */
    protected static void insertProfile(String[] parts) {
        if (!records.containsKey(parts[1])) {
            records.put(parts[1], new Profile(parts[1]));
        }
        switch (parts[0]) {
            case "VEST":
                records.get(parts[1]).getEntries().add( new Entry(RecordType.VEST, parts[2], Integer.parseInt(parts[3]), new BigDecimal(parts[4])) );
                break;
            case "PERF":
                records.get(parts[1]).setPerfBonus(new Entry(RecordType.PERF, parts[2], 0, new BigDecimal(parts[3])));
                break;
            case "SALE":
                records.get(parts[1]).getSaleEntries().add( new Entry(RecordType.SALE, parts[2], Integer.parseInt(parts[3]), new BigDecimal(parts[4])) );
                break;
            default:
        }

    }

    /**
     * In valid scenarios, SALE record(s) must be pre-dated by some VEST record(s)
     * @param profile An employee's profile which consists of id and all VEST/PERF/SALE records.
     * @param targetDate date for query, defined on the last line of input
     * @param marketPrice market price of the stock on targetDate
     */
    @SuppressWarnings("unchecked")
    protected static BigDecimal[] calculate(Profile profile, String targetDate, BigDecimal marketPrice) {
        BigDecimal totalGainAvail = BigDecimal.ZERO;
        BigDecimal gainRealized = BigDecimal.ZERO;

        Collections.sort(profile.getEntries());
        Collections.sort(profile.getSaleEntries());
        Queue<Entry> vestQueue = (Queue<Entry>) profile.getEntries().clone();
        Queue<Entry> saleQueue = (Queue<Entry>) profile.getSaleEntries().clone();
        Queue<Entry> tempVestQueue = new LinkedList<>();

        boolean notReachedTargetDate = ( saleQueue.peek() != null && saleQueue.peek().occuredBeforeOrOn(targetDate));
        Entry currentSale=null;
        Entry currentVest=null;
        while (notReachedTargetDate && (currentSale = saleQueue.poll()) != null) {
            int remain = 0;
            int totalUnitAmount = 0;
            if (tempVestQueue.peek() != null) {
                currentVest = tempVestQueue.poll();     //check if there's a leftover VEST from last SALE calculation
            }
            //if currentVest is not null, there's a leftover VEST from last run, poll on right side of LOGIC-OR would be skipped;
            // otherwise, poll the queue to get a new one
            while (currentVest != null || (currentVest = vestQueue.poll()) != null) {

                if (totalUnitAmount < currentSale.getAmount()) {

                    if ((totalUnitAmount + currentVest.getAmount()) >= currentSale.getAmount()) {
                        remain = currentSale.getAmount() - totalUnitAmount;
                        totalUnitAmount += currentVest.getAmount();
                        gainRealized = gainRealized.add(calculateSaleGain(currentVest, currentSale, profile.getPerfBonus(), remain));
                        if (totalUnitAmount > currentSale.getAmount()) {
                            Entry leftoverVest = new Entry(RecordType.VEST, currentVest.getDate(), totalUnitAmount - currentSale.getAmount(), currentVest.getPrice());
                            tempVestQueue.add(leftoverVest);
                        } else {
                            currentVest = null; //force to poll from queue
                        }

                        break;
                    } else {
                        //this VEST is less than the SALE's amount, would be fully consumed
                        gainRealized = gainRealized.add(calculateSaleGain(currentVest, currentSale, profile.getPerfBonus(), 0));
                        totalUnitAmount += currentVest.getAmount();
                        currentVest = null; //force to poll from queue
                        continue;
                    }

                }

            }
            notReachedTargetDate = ( saleQueue.peek() != null && saleQueue.peek().occuredBeforeOrOn(targetDate));

        }
        //After exhausting all in-range SALE records, go on to calculate Total Gain Available from the rest of the in-range VEST recs
        if ((currentVest = tempVestQueue.poll()) != null) {
            totalGainAvail = totalGainAvail.add(calculateAvailVestGain(currentVest, profile.getPerfBonus(), targetDate, marketPrice));
        }
        notReachedTargetDate = ( vestQueue.peek() != null && vestQueue.peek().occuredBeforeOrOn(targetDate));
        while ( notReachedTargetDate &&(currentVest = vestQueue.poll()) != null ) {
            totalGainAvail = totalGainAvail.add(calculateAvailVestGain(currentVest, profile.getPerfBonus(), targetDate, marketPrice));
            notReachedTargetDate = ( vestQueue.peek() != null && vestQueue.peek().occuredBeforeOrOn(targetDate));
        }

        totalGainAvail = totalGainAvail.setScale(2, BigDecimal.ROUND_HALF_UP);
        gainRealized = gainRealized.setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal[] results = {totalGainAvail, gainRealized};
        return results;
    }

    /**
     * calculate the gain realized of a VEST/SALE combo, with PERF if in-effect
     * @param vestEntry a VEST entry covering the SALE defined below
     * @param saleEntry the SALE entry realized
     * @param perfEntry PERF entry from owner's profile
     * @param lastrun if this is the last VEST needed for covering this SALE,this is the actual unit amount consumed.
     * @return Gain realized from this VEST/SALE combo
     */
    private static BigDecimal calculateSaleGain(Entry vestEntry, Entry saleEntry, Entry perfEntry, int lastrun) {
        //BigDecimal sum = BigDecimal.ZERO;
        BigDecimal unitAmount = (lastrun > 0) ? new BigDecimal(lastrun) : new BigDecimal(vestEntry.getAmount());
        BigDecimal base = (saleEntry.getPrice().subtract(vestEntry.getPrice()));
        if (perfEntry != null && vestEntry.occuredBeforeOrOn(perfEntry.getDate()) && perfEntry.occuredBeforeOrOn(saleEntry.getDate())){
            base = base.multiply(perfEntry.getPrice());
        }

        return (unitAmount.multiply(base));
    }

    /**
     * calculate the on-paper gain of VEST record with PERF if in-effect
     * @param vestEntry a VEST entry that has NOT been realized
     * @param perfEntry PERF entry from owner's profile
     * @param targetDate date for query, defined on the last line of input
     * @param marketPrice market price of the stock on targetDate
     * @return "on-paper" gain of this VEST on targetDate
     */
    private static BigDecimal calculateAvailVestGain(Entry vestEntry, Entry perfEntry, String targetDate, BigDecimal marketPrice) {
        BigDecimal base = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        if (vestEntry.occuredBeforeOrOn(targetDate)) {
            if (marketPrice.compareTo(vestEntry.getPrice()) > 0) {
                base = base.add((marketPrice.subtract(vestEntry.getPrice())).multiply(new BigDecimal(vestEntry.getAmount())));
                if (perfEntry != null && perfEntry.occuredBeforeOrOn(targetDate)
                        && vestEntry.occuredBeforeOrOn(perfEntry.getDate())) {
                    base = base.multiply(perfEntry.getPrice());
                }
            }
            total = total.add(base) ;

        }
        return total;
    }

    /**
     * set BigDecimal's output format
     */
    protected static void setDfFormat() {
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        df.setGroupingUsed(false);

    }
}
