package solium;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Yong.L on 8/31/2015.
 * basic input validation
 */
public class InputValidateUtil {
    private static final int VEST_SALE_ROW_INPUT_SIZE = 5;
    private static final int PERF_ROW_INPUT_SIZE = 4;
    public static void validateRecordLine(String[] parts) throws Exception {

        switch (parts[0]) {
            case "VEST":
            case "SALE":
                validateVestSaleRec(parts);
                break;
            case "PERF":
                validatePerfRec(parts);
                break;
            default:
                throw new Exception(makeExceptionMsg(parts));
        }
    }

    private static void validateVestSaleRec(String[] parts) throws Exception {
        if (parts.length != VEST_SALE_ROW_INPUT_SIZE) throw new Exception(makeExceptionMsg(parts));
        checkDate(parts[2]);
    }

    private static void validatePerfRec(String[] parts) throws Exception {
        if (parts.length != PERF_ROW_INPUT_SIZE) throw new Exception(makeExceptionMsg(parts));
        checkDate(parts[2]);
    }
    private static void checkDate(String date) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        formatter.setLenient(false);
        if (date.length() == 8) {
            try {
                formatter.parse(date);
            } catch (ParseException e) {
                //e.printStackTrace();
                throw e;
            }
        } else {
            throw new Exception();
        }
    }
    private static String makeExceptionMsg(String[] parts) {
        return "Invalid input: " + Arrays.toString(parts);
    }
}
