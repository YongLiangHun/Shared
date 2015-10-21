package solium;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/**
 * Unit tests for App.
 */
public class AppTest
{

    @DataProvider(name = "VestOnlyTest1")
    public Object[][] createVestOnlyData1() {
        String[][] entries = {{"20120102","1000","0.45"}, {"20130102","2000","0.55"}, {"20140630","500","1.20"}};
        Profile emp1 = createProfileData("001A", entries, null);
        String[] dates = {"20140101", "20140701", "20130101", "20100101"};
        BigDecimal[] expected = {new BigDecimal(1450), new BigDecimal(550), BigDecimal.ZERO , new BigDecimal(50)};
        BigDecimal[] marketPrice = {new BigDecimal(1.0), new BigDecimal(0.5)};

        return new Object[][] {
                { emp1, dates[0], marketPrice[0], expected[0]},    //2000*(1-0.55)+1000*(1-0.45)
                { emp1, dates[1], marketPrice[0], expected[0]},    //2000*(1-0.55)+1000*(1-0.45)+500*0 since 1.2>1.0
                { emp1, dates[2], marketPrice[0], expected[1]},     //1000*(1-0.45)
                { emp1, dates[3], marketPrice[0], expected[2]},     //0
                { emp1, dates[0], marketPrice[1], expected[3]}      //2000*(0.5-0.55)+1000*(0.5-0.45)
        };
    }

    @Test(dataProvider = "VestOnlyTest1")
    public void verifyData1(Profile emp, String date, BigDecimal marketPrice, BigDecimal expected) {
        BigDecimal[] results= App.calculate(emp, date, marketPrice);
        Assert.assertEquals(results[0], expected.setScale(2));
        Assert.assertEquals(results[1], BigDecimal.ZERO.setScale(2));
    }

    /**
     *
     * 1st example from the pdf
     * @return
     */
    @DataProvider
    public Object[][] CreateVestOnlyData2() {
        String[][][] entries = {{{"20120101", "1000", "0.45"}, {"20130101", "1500", "0.50"}},
                                {{"20120101","1500","0.45"}, {"20130101","1000","0.50"}},
                                {{"20130101","1000","0.50"}}        };
        Profile emp1 = createProfileData("001B", entries[0], null);
        Profile emp2 = createProfileData("002B", entries[1], null);
        Profile emp3 = createProfileData("003B", entries[2], null);
        String[] dates = {"20140101", "20120615"};
        BigDecimal[] expected = {new BigDecimal(1300), new BigDecimal(1325),  new BigDecimal(500)};
        BigDecimal[] expected2 = {new BigDecimal(550), new BigDecimal(825),  BigDecimal.ZERO};
        BigDecimal marketPrice = new BigDecimal(1.0);

        return new Object[][] {
                { emp1, dates[0], marketPrice, expected[0]},
                { emp2, dates[0], marketPrice, expected[1]},
                { emp3, dates[0], marketPrice, expected[2]},
                { emp1, dates[1], marketPrice, expected2[0]},
                { emp2, dates[1], marketPrice, expected2[1]},
                { emp3, dates[1], marketPrice, expected2[2]}
        };
    }

    @Test(dataProvider = "CreateVestOnlyData2")
    public void verifyData2(Profile emp, String date, BigDecimal marketPrice, BigDecimal expected) {
        //Assert.assertEquals(App.calculateGain(emp, date, marketPrice), expected.setScale(2));
        BigDecimal[] results= App.calculate(emp, date, marketPrice);
        Assert.assertEquals(results[0], expected.setScale(2));
        Assert.assertEquals(results[1], BigDecimal.ZERO.setScale(2));

    }

    private Profile createProfileData(String employeeId, String[][] entries, String[] perfEntry) {
        Profile emp1 = new Profile(employeeId);
        for (String[] parts : entries) {
            Entry e = new Entry(RecordType.VEST, parts[0], Integer.parseInt(parts[1]), new BigDecimal(parts[2]));
            emp1.getEntries().add(e);
        }
        if (perfEntry != null) {
            emp1.setPerfBonus(CreatePerfEntry(perfEntry));
        }
        return emp1;
    }

    private Entry CreatePerfEntry( String[] perfEntry) {
        Entry pe = new Entry(RecordType.PERF, perfEntry[0], 0, new BigDecimal(perfEntry[1]));
        return pe;
    }

    private void addSaleEntries(Profile profile, String[][] entries){
        for (String[] parts : entries) {
            Entry e = new Entry(RecordType.SALE, parts[0], Integer.parseInt(parts[1]), new BigDecimal(parts[2]));
            profile.getSaleEntries().add(e);
        }
    }

    @DataProvider(name = "VestPerfTest1")
    public Object[][] createVestPerfData1() {
        String[][] entries = {{"20120102","1000","0.45"}, {"20130102","2000","0.55"}, {"20140630","500","0.70"}};
        String[][] perfEntry = {{"20140101","1.5"}, {"20120701","2"}};
        Profile emp1 = createProfileData("001A", entries, null);
        String[] dates = {"20140101", "20140701", "20130101", "20100101"};
        BigDecimal[] expectedOn20140101 = {new BigDecimal(2175), new BigDecimal(2325),new BigDecimal(550), BigDecimal.ZERO , new BigDecimal(75)};
        BigDecimal[] expectedOn20120701 = {new BigDecimal(2000), new BigDecimal(2150),new BigDecimal(1100), BigDecimal.ZERO , new BigDecimal(100)};
        BigDecimal[] marketPrice = {new BigDecimal(1.0), new BigDecimal(0.5)};

        return new Object[][] {
                { emp1, CreatePerfEntry(perfEntry[0]), dates[0], marketPrice[0], expectedOn20140101[0]},    //(2000*(1-0.55)+1000*(1-0.45))*1.5
                { emp1, CreatePerfEntry(perfEntry[0]), dates[1], marketPrice[0], expectedOn20140101[1]},    //2000*(1-0.55)*1.5+1000*(1-0.45)*1.5+500*(1-0.7)
                { emp1, CreatePerfEntry(perfEntry[0]), dates[2], marketPrice[0], expectedOn20140101[2]},     //1000*(1-0.45)
                { emp1, CreatePerfEntry(perfEntry[0]), dates[3], marketPrice[0], expectedOn20140101[3]},     //0
                { emp1, CreatePerfEntry(perfEntry[0]), dates[0], marketPrice[1], expectedOn20140101[4]},      //2000*(0.5-0.55)+1000*(0.5-0.45)*1.5

                { emp1, CreatePerfEntry(perfEntry[1]), dates[0], marketPrice[0], expectedOn20120701[0]},    //2000*(1-0.55)+1000*(1-0.45)*2
                { emp1, CreatePerfEntry(perfEntry[1]), dates[1], marketPrice[0], expectedOn20120701[1]},    //2000*(1-0.55)+1000*(1-0.45)*2+500*(1-0.7)
                { emp1, CreatePerfEntry(perfEntry[1]), dates[2], marketPrice[0], expectedOn20120701[2]},     //1000*(1-0.45)*2
                { emp1, CreatePerfEntry(perfEntry[1]), dates[3], marketPrice[0], expectedOn20120701[3]},     //0
                { emp1, CreatePerfEntry(perfEntry[1]), dates[0], marketPrice[1], expectedOn20120701[4]},      //2000*(0.5-0.55)+1000*(0.5-0.45)*2

        };
    }
    @Test(dataProvider = "VestPerfTest1")
    public void verifyVestPerfData1(Profile emp, Entry perfEntry, String date, BigDecimal marketPrice, BigDecimal expected) {

        if (perfEntry != null) emp.setPerfBonus(perfEntry);
        BigDecimal[] results= App.calculate(emp, date, marketPrice);
        Assert.assertEquals(results[0], expected.setScale(2));
        Assert.assertEquals(results[1], BigDecimal.ZERO.setScale(2));

    }

    @DataProvider(name = "VestPerfSaleTest1")
    public Object[][] createVestPerfSaleData1() {
        String[][] entries = {{"20120102","1000","0.45"}, {"20130102","2000","0.55"}, {"20140630","500","0.70"}, {"20141230","2000","0.3"}};
        String[][] saleEntries = {{"20130702","2500","1.00"}, {"20140902","500","1.10"}};
        String[][] perfEntry = { {"20130401","2"}, {"20140701","1.5"} };
        Profile emp1 = createProfileData("001A", entries, null);
        addSaleEntries(emp1, saleEntries);

        String[] dates = {"20100101", "20130101", "20130601", "20140101", "20140701", "20150101"};
        BigDecimal[] marketPrice = {new BigDecimal(1.0), new BigDecimal(0.5)};

        BigDecimal[][] expectedForPerf1 = {{BigDecimal.ZERO, BigDecimal.ZERO}, {new BigDecimal(550), BigDecimal.ZERO},
                                             {new BigDecimal(2900), BigDecimal.ZERO}, {new BigDecimal(450), new BigDecimal(2450)},
                                             {new BigDecimal(600), new BigDecimal(2450)}, {new BigDecimal(1550), new BigDecimal(3000)}};

        BigDecimal[][] expectedForPerf2 = {{BigDecimal.ZERO, BigDecimal.ZERO}, {new BigDecimal(550), BigDecimal.ZERO},
                {new BigDecimal(1450), BigDecimal.ZERO}, {new BigDecimal(225), new BigDecimal(1225)},
                {new BigDecimal(562.5), new BigDecimal(1225)}, {new BigDecimal(1625), new BigDecimal(1637.5)}};

        BigDecimal[][] expectedForPerf1Under = {{BigDecimal.ZERO, BigDecimal.ZERO}, {new BigDecimal(50), BigDecimal.ZERO},
                {new BigDecimal(100), BigDecimal.ZERO}, {BigDecimal.ZERO, new BigDecimal(2450)},
                {BigDecimal.ZERO, new BigDecimal(2450)}, {new BigDecimal(400), new BigDecimal(3000)}};

        String[][] entries2 = {{"20120102","1000","0.45"}, {"20130102","2000","0.55"}, {"20140630","500","0.70"}};
        Profile emp2 = createProfileData("001B", entries2, null);
        String[][] saleEntries2 = {{"20120402","1000","1.00"}, {"20130602","1000","2.00"}, {"20130802","1000","2.50"}};
        addSaleEntries(emp2, saleEntries2);
        String[] dates2 = {"20120301", "20120901", "20130402", "20130602", "20130901", "20150101"};
        BigDecimal[][] expectedForPerf1Emp1 = {{new BigDecimal(550), BigDecimal.ZERO}, {BigDecimal.ZERO, new BigDecimal(550)},
                {new BigDecimal(1800), new BigDecimal(550)}, {new BigDecimal(900), new BigDecimal(3450)},
                {BigDecimal.ZERO, new BigDecimal(7350)}, {new BigDecimal(150), new BigDecimal(7350)}};

        return new Object[][] {
                { emp1, CreatePerfEntry(perfEntry[0]), dates[0], marketPrice[0], expectedForPerf1[0]},    //before anything happens
                { emp1, CreatePerfEntry(perfEntry[0]), dates[1], marketPrice[0], expectedForPerf1[1]},    //1000*(1-0.45);no sale
                { emp1, CreatePerfEntry(perfEntry[0]), dates[2], marketPrice[0], expectedForPerf1[2]},     //1000*(1-0.45)*2 + 2000*(1-0.55)*2;no sale
                { emp1, CreatePerfEntry(perfEntry[0]), dates[3], marketPrice[0], expectedForPerf1[3]},     //(2000-1500)*(1-.55)*2;sale:1000*(1-0.45)*2+1500*(1-0.55)*2
                { emp1, CreatePerfEntry(perfEntry[0]), dates[4], marketPrice[0], expectedForPerf1[4]},      //450+500*(1-0.7);no new sale
                { emp1, CreatePerfEntry(perfEntry[0]), dates[5], marketPrice[0], expectedForPerf1[5]},      //500*(1-0.7)+2000*(1-0.3);2450+(1.1-0.55)**500*2

                { emp1, CreatePerfEntry(perfEntry[1]), dates[0], marketPrice[0], expectedForPerf2[0]},    //before anything happens
                { emp1, CreatePerfEntry(perfEntry[1]), dates[1], marketPrice[0], expectedForPerf2[1]},    //1000*(1-0.45);no sale
                { emp1, CreatePerfEntry(perfEntry[1]), dates[2], marketPrice[0], expectedForPerf2[2]},     //1000*(1-0.45) + 2000*(1-0.55);no sale
                { emp1, CreatePerfEntry(perfEntry[1]), dates[3], marketPrice[0], expectedForPerf2[3]},     //(2000-1500)*(1-.55);sale:1000*(1-0.45)+1500*(1-0.55)
                { emp1, CreatePerfEntry(perfEntry[1]), dates[4], marketPrice[0], expectedForPerf2[4]},      //225*1.5+500*(1-0.7)*1.5;no new sale, perf in
                { emp1, CreatePerfEntry(perfEntry[1]), dates[5], marketPrice[0], expectedForPerf2[5]},      //500*(1-0.7)*1.5+2000*(1-0.3);1225+(1.1-0.55)**500*1.5

                //under-water below
                { emp1, CreatePerfEntry(perfEntry[0]), dates[0], marketPrice[1], expectedForPerf1Under[0]},    //before anything happens
                { emp1, CreatePerfEntry(perfEntry[0]), dates[1], marketPrice[1], expectedForPerf1Under[1]},    //1000*(0.5-0.45);no sale
                { emp1, CreatePerfEntry(perfEntry[0]), dates[2], marketPrice[1], expectedForPerf1Under[2]},     //1000*(0.5-0.45)*2 + 2000*(0.5-0.55)*2;no sale
                { emp1, CreatePerfEntry(perfEntry[0]), dates[3], marketPrice[1], expectedForPerf1Under[3]},     //(2000-1500)*(0.5-.55)*2;sale:1000*(1-0.45)*2+1500*(1-0.55)*2
                { emp1, CreatePerfEntry(perfEntry[0]), dates[4], marketPrice[1], expectedForPerf1Under[4]},      //0+500*(0.5-0.7);no new sale
                { emp1, CreatePerfEntry(perfEntry[0]), dates[5], marketPrice[1], expectedForPerf1Under[5]},      //500*(0.5-0.7)+2000*(0.5-0.3); 2450+(1.1-0.55)**500*2

                //emp2
                { emp2, CreatePerfEntry(perfEntry[0]), dates2[0], marketPrice[0], expectedForPerf1Emp1[0]},    //1000*(1-0.45);no sale
                { emp2, CreatePerfEntry(perfEntry[0]), dates2[1], marketPrice[0], expectedForPerf1Emp1[1]},    //0 ; sale=1000*(1-0.45)
                { emp2, CreatePerfEntry(perfEntry[0]), dates2[2], marketPrice[0], expectedForPerf1Emp1[2]},     //2000*(1-0.55)*2;no new sale
                { emp2, CreatePerfEntry(perfEntry[0]), dates2[3], marketPrice[0], expectedForPerf1Emp1[3]},     //(2000-1000)*(1-.55)*2;sale:1000*(2-0.55)*2+550
                { emp2, CreatePerfEntry(perfEntry[0]), dates2[4], marketPrice[0], expectedForPerf1Emp1[4]},      //0;3450+1000*(2.50-0.55)*2
                { emp2, CreatePerfEntry(perfEntry[0]), dates2[5], marketPrice[0], expectedForPerf1Emp1[5]},      //500*(1-0.7);no new sale

        };
    }
    @Test(dataProvider = "VestPerfSaleTest1")
    public void verifyVestPerfSaleData1(Profile emp, Entry perfEntry, String date, BigDecimal marketPrice, BigDecimal[] expected) {

        if (perfEntry != null) emp.setPerfBonus(perfEntry);

        BigDecimal[] results= App.calculate(emp, date, marketPrice);
        Assert.assertEquals(results[0], expected[0].setScale(2));
        Assert.assertEquals(results[1], expected[1].setScale(2));

    }

}
