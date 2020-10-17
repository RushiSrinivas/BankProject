package account;

import Exceptions.AccountException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Calendar;
import java.util.Date;

public abstract class Account {

    public static int accNo;
    public static String name;
    public static Date openingDate;
    public static String accType;
    public static String cusType;
    public static double balance;
    public static int withdrawalsToday;

    public int getAccNo() {
        return accNo;
    }

    public String getName() {
        return name;
    }

    public Date getOpeningDate() {
        return openingDate;
    }

    public String getAccType() {
        return accType;
    }

    public String getCusType() {
        return cusType;
    }

    public double getBalance() {
        return balance;
    }

    public int getWithdrawalsToday() {
        return withdrawalsToday;
    }

    static final int minimumBalanceGold = 50000;
    static final int minimumBalanceSilver = 10000;

    static String path = "src/account/Data.xlsx";

    static final int maxWithdrawalsGold = 5;
    static final int maxWithdrawalsSilver = 3;

    public static boolean login(int accNum) {
        try {
            InputStream in = new FileInputStream(path);
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet master = workbook.getSheetAt(0);

            int rowNum = 0;
            while (rowNum < master.getPhysicalNumberOfRows()) {
                Row accRow = master.getRow(rowNum);
                Cell cell = accRow.getCell(0);
                if (cell.getNumericCellValue() == accNum) break;
                rowNum++;
            }
            if (rowNum == master.getPhysicalNumberOfRows()) return false;

            Row accRow = master.getRow(rowNum);

            Cell accNoCell = accRow.getCell(0);
            Cell nameCell = accRow.getCell(1);
            Cell openingDateCell = accRow.getCell(2);
            Cell accTypeCell = accRow.getCell(3);
            Cell cusTypeCell = accRow.getCell(4);
            Cell balanceCell = accRow.getCell(5);
            Cell withdrawalsTodayCell = accRow.getCell(6);

            Account.accNo = (int) accNoCell.getNumericCellValue();
            name = nameCell.getStringCellValue();
            openingDate = openingDateCell.getDateCellValue();
            accType = accTypeCell.getStringCellValue();
            cusType = cusTypeCell.getStringCellValue();
            balance = balanceCell.getNumericCellValue();
            withdrawalsToday = (int) withdrawalsTodayCell.getNumericCellValue();

            in.close();

            OutputStream out = new FileOutputStream(path);
            workbook.write(out);
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void logout() {
        accNo = -1;
        name = null;
        openingDate = null;
        accType = null;
        cusType = null;
        balance = -1;
        withdrawalsToday = -1;
    }

    public static boolean createAccount(String name, String accType, String detail, double initBalance) {
        if (initBalance < 10000) return false;
        try {
            InputStream in = new FileInputStream(path);
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            CreationHelper creationHelper = workbook.getCreationHelper();

            XSSFSheet master = workbook.getSheetAt(0);
            XSSFSheet transactions = workbook.getSheetAt(1);

            int prevAccNo, accNo;
            try {
                Row lastRow = master.getRow(master.getPhysicalNumberOfRows()-1);
                Cell accNoCell = lastRow.getCell(0);
                prevAccNo = (int) accNoCell.getNumericCellValue();
                accNo = prevAccNo+1;
            } catch (NullPointerException e) {
                accNo = 9999;
            }

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy"));

            Row accRow = master.createRow(master.getPhysicalNumberOfRows());

            Cell accNoCell = accRow.createCell(0);
            Cell nameCell = accRow.createCell(1);
            Cell accTypeCell = accRow.createCell(3);
            Cell cusTypeCell = accRow.createCell(4);
            Cell balanceCell = accRow.createCell(5);
            Cell withdrawalsTodayCell = accRow.createCell(6);
            Cell openingDateCell = accRow.createCell(2);
            openingDateCell.setCellStyle(cellStyle);
            if (accType.equals("Savings")) {
                Cell minimumBalanceCell = accRow.createCell(8);
                minimumBalanceCell.setCellValue(initBalance);
            }

            accNoCell.setCellValue(accNo);
            nameCell.setCellValue(name);
            openingDateCell.setCellValue(new Date());
            accTypeCell.setCellValue(accType);
            if (initBalance >= minimumBalanceGold)
                cusTypeCell.setCellValue("Gold");
            else if (initBalance >= minimumBalanceSilver)
                cusTypeCell.setCellValue("Silver");
            balanceCell.setCellValue(initBalance);
            withdrawalsTodayCell.setCellValue(0);

            Row transactionRow = transactions.createRow(transactions.getPhysicalNumberOfRows());

            Cell accNoCell1 = transactionRow.createCell(0);
            Cell transactionDateCell = transactionRow.createCell(1);
            Cell transactionTypeCell = transactionRow.createCell(2);
            Cell transactionAmtCell = transactionRow.createCell(3);
            Cell transactionDetailCell = transactionRow.createCell(4);
            transactionDateCell.setCellStyle(cellStyle);

            accNoCell1.setCellValue(accNo);
            transactionDateCell.setCellValue(new Date());
            transactionTypeCell.setCellValue("CR");
            transactionAmtCell.setCellValue(initBalance);
            transactionDetailCell.setCellValue(detail);

            in.close();

            OutputStream out = new FileOutputStream(path);
            workbook.write(out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void deposit(double amount, String detail) {
        try {
            InputStream in = new FileInputStream(path);
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet master = workbook.getSheetAt(0);
            XSSFSheet transactions = workbook.getSheetAt(1);
            CreationHelper creationHelper = workbook.getCreationHelper();

            int rowNum = 0;
            while (rowNum < master.getPhysicalNumberOfRows()) {
                Row accRow = master.getRow(rowNum);
                Cell cell = accRow.getCell(0);
                if (cell.getNumericCellValue() == accNo) break;
                rowNum++;
            }
            if (rowNum == master.getPhysicalNumberOfRows()) return;

            Row accRow = master.getRow(rowNum);
            Row transactionRow = transactions.createRow(transactions.getPhysicalNumberOfRows());

            Cell cell = accRow.getCell(5);
            if (cell.getNumericCellValue() < minimumBalanceSilver && cell.getNumericCellValue() + amount >= minimumBalanceSilver && cusType.equals("Silver")) {
                Cell issuesCell = transactionRow.createCell(5);
                issuesCell.setCellValue("Balance Restored");
            } else if (cell.getNumericCellValue() < minimumBalanceGold && cell.getNumericCellValue() + amount >= minimumBalanceGold && cusType.equals("Gold")) {
                Cell issuesCell = transactionRow.createCell(5);
                issuesCell.setCellValue("Balance Restored");
            }
            cell.setCellValue(cell.getNumericCellValue()+amount);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy"));
            Cell accNoCell1 = transactionRow.createCell(0);
            Cell transactionDateCell = transactionRow.createCell(1);
            Cell transactionTypeCell = transactionRow.createCell(2);
            Cell transactionAmtCell = transactionRow.createCell(3);
            Cell transactionDetailCell = transactionRow.createCell(4);
            transactionDateCell.setCellStyle(cellStyle);

            accNoCell1.setCellValue(accNo);
            transactionDateCell.setCellValue(new Date());
            transactionTypeCell.setCellValue("CR");
            transactionAmtCell.setCellValue(amount);
            transactionDetailCell.setCellValue(detail);

            in.close();

            OutputStream out = new FileOutputStream(path);
            workbook.write(out);
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[][] generateStatement(Date start, Date end) {
        if (accNo==-1) return null;
        String[][] statement = new String[20][4];
        try {
            InputStream in = new FileInputStream(path);
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet transactions = workbook.getSheetAt(1);

            int i = 0;
            for (Row row: transactions) {

                Cell cell = row.getCell(0);
                Date date = row.getCell(1).getDateCellValue();

                Calendar cal = Calendar.getInstance();

                cal.setTime(start);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                start = cal.getTime();

                cal.setTime(end);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                end = cal.getTime();

                boolean inclusive = cell.getNumericCellValue() == accNo && !date.after(end) && !date.before(start);

                if (inclusive) {
                    Cell typeCell = row.getCell(2);
                    Cell amountCell = row.getCell(3);
                    Cell detailCell = row.getCell(4);

                    statement[i][0] = date.toString();
                    statement[i][1] = typeCell.getStringCellValue();
                    statement[i][2] = Double.toString(amountCell.getNumericCellValue());
                    try {
                        statement[i][3] = detailCell.getStringCellValue();
                    } catch (NullPointerException e) {
                        statement[i][3] = null;
                    }

                    i++;
                }
            }

            in.close();

            return statement;
        } catch (IOException e) {
            return statement;
        }
    }

    public abstract void withdraw(double amount) throws AccountException;
}
