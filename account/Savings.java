package account;

import Exceptions.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class Savings extends Account implements Acct{

    static final int withdrawalLimitGold = 100000;
    static final int withdrawalLimitSilver = 50000;

    @Override
    public void withdraw(double amount) throws AccountException {
        if (accNo==-1) return;
        try {
            InputStream in = new FileInputStream(path);
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet master = workbook.getSheetAt(0);
            XSSFSheet transactions = workbook.getSheetAt(1);
            CreationHelper creationHelper = workbook.getCreationHelper();

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy"));

            int rowNum = 0;
            while (rowNum < master.getPhysicalNumberOfRows()) {
                Row accRow = master.getRow(rowNum);
                Cell cell = accRow.getCell(0);
                if (cell.getNumericCellValue() == accNo) break;
                rowNum++;
            }
            if (rowNum == master.getPhysicalNumberOfRows()) return;

            Row accRow = master.getRow(rowNum);
            Cell balanceCell = accRow.getCell(5);
            double total = balanceCell.getNumericCellValue()-amount;

            if (total < 0)
                throw new BelowZeroBalanceException();
            else
                balanceCell.setCellValue(total);

            if (withdrawalsToday == maxWithdrawalsSilver && cusType.equals("Silver"))
                throw new MaximumWithdrawalsException();
            else if (withdrawalsToday == maxWithdrawalsGold && cusType.equals("Gold"))
                throw new MaximumWithdrawalsException();

            if (cusType.equals("Gold") && amount > withdrawalLimitGold)
                throw new WithdrawalLimitException();
            else if (cusType.equals("Silver") && amount > withdrawalLimitSilver)
                throw new WithdrawalLimitException();

            Cell withdrawalsTodayCell = accRow.getCell(6);
            Cell lastWithdrawalCell = accRow.getCell(7);
            Cell minimumBalanceCell = accRow.getCell(8);

            try {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                if (fmt.format(lastWithdrawalCell.getDateCellValue()).equals(fmt.format(new Date()))) {
                    withdrawalsTodayCell.setCellValue(withdrawalsTodayCell.getNumericCellValue()+1);
                    withdrawalsToday++;
                } else {
                    withdrawalsTodayCell.setCellValue(1);
                    lastWithdrawalCell.setCellValue(new Date());
                    withdrawalsToday = 1;
                }
                lastWithdrawalCell.setCellStyle(cellStyle);
            } catch (NullPointerException e) {
                lastWithdrawalCell = accRow.createCell(7);
                withdrawalsTodayCell.setCellValue(1);
                lastWithdrawalCell.setCellStyle(cellStyle);
                lastWithdrawalCell.setCellValue(new Date());
                withdrawalsToday = 1;
            }

            Row transactionRow = transactions.createRow(transactions.getPhysicalNumberOfRows());
            Cell accNoCell1 = transactionRow.createCell(0);
            Cell transactionDateCell = transactionRow.createCell(1);
            Cell transactionTypeCell = transactionRow.createCell(2);
            Cell transactionAmtCell = transactionRow.createCell(3);
            Cell issuesCell = transactionRow.createCell(5);
            transactionDateCell.setCellStyle(cellStyle);

            accNoCell1.setCellValue(accNo);
            transactionDateCell.setCellValue(new Date());
            transactionTypeCell.setCellValue("DR");
            transactionAmtCell.setCellValue(amount);

            LocalDate localDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int date = localDate.getDayOfMonth();

            if (date >= 10) {
                if (balanceCell.getNumericCellValue() < minimumBalanceCell.getNumericCellValue()) {
                    minimumBalanceCell.setCellValue(balanceCell.getNumericCellValue());
                }
            }

            if (total < minimumBalanceGold && cusType.equals("Gold"))
                issuesCell.setCellValue("Less Than Minimum Balance");
            else if (total < minimumBalanceSilver && cusType.equals("Silver"))
                issuesCell.setCellValue("Less Than Minimum Balance");

            in.close();

            OutputStream out = new FileOutputStream(path);
            workbook.write(out);
            out.flush();
            out.close();

            if (total < minimumBalanceGold && cusType.equals("Gold"))
                throw new BelowMinimumBalanceException(total, "Gold");
            else if (total < minimumBalanceSilver && cusType.equals("Silver"))
                throw new BelowMinimumBalanceException(total, "Silver");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void calculateInterest() {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);

        if (today != daysInMonth) return;

        try {
            InputStream in = new FileInputStream(path);
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet master = workbook.getSheetAt(0);
            XSSFSheet transactions = workbook.getSheetAt(1);

            CreationHelper creationHelper = workbook.getCreationHelper();
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy"));

            int rowNum = 0;
            while (rowNum < master.getPhysicalNumberOfRows()) {
                Row accRow = master.getRow(rowNum);
                Cell cell = accRow.getCell(0);
                if (cell.getNumericCellValue() == accNo) break;
                rowNum++;
            }
            if (rowNum == master.getPhysicalNumberOfRows()) return;

            Row accRow = master.getRow(rowNum);
            Cell minimumBalanceCell = accRow.getCell(8);

            Cell balanceCell = accRow.getCell(5);

            if (cusType.equals("Silver")) {

                double interest = minimumBalanceCell.getNumericCellValue() * (savingsIntRateSilver/100);

                balanceCell.setCellValue(balanceCell.getNumericCellValue()+interest);
                if (minimumBalanceCell.getNumericCellValue() >= 50000) {
                    Cell accTypeCell = accRow.getCell(3);
                    accTypeCell.setCellValue("Gold");
                }

                Row transactionRow = transactions.createRow(transactions.getPhysicalNumberOfRows());

                Cell accNoCell = transactionRow.createCell(0);
                Cell dateCell = transactionRow.createCell(1);
                Cell transactionTypeCell = transactionRow.createCell(2);
                Cell amtCell = transactionRow.createCell(3);
                Cell transactionDetailCell = transactionRow.createCell(4);
                dateCell.setCellStyle(cellStyle);

                accNoCell.setCellValue(accNo);
                dateCell.setCellValue(new Date());
                transactionTypeCell.setCellValue("CR");
                amtCell.setCellValue(interest);
                transactionDetailCell.setCellValue("Interest Accrual");

            } else {
                Date less;
                Date restore = new Date();
                int daysLessThanMax = 0;
                for (Row row: transactions) {
                    Cell issuesCell = row.getCell(5);
                    try {
                        if (issuesCell.getStringCellValue().equals("Less Than Minimum Balance")) {
                            Cell dateCell = row.getCell(1);
                            less = dateCell.getDateCellValue();
                            if (isSameMonth(less)) {
                                for (int i = row.getRowNum()+1; i < transactions.getPhysicalNumberOfRows(); i++) {
                                    Row row1 = transactions.getRow(i);
                                    try {
                                        Cell issuesCell1 = row1.getCell(5);
                                        if (issuesCell1.getStringCellValue().equals("Balance Restored")) {
                                            Cell dateCell1 = row1.getCell(1);
                                            restore = dateCell1.getDateCellValue();
                                        }

                                        LocalDate localDate = less.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                        int dateLess = localDate.getDayOfMonth();

                                        LocalDate localDate1 = restore.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                        int dateMore = localDate1.getDayOfMonth();

                                        daysLessThanMax += dateMore-dateLess;
                                    } catch (NullPointerException e) {

                                    }
                                }
                            }
                        }
                    } catch (NullPointerException e) {

                    }
                }

                double interestRate = (daysLessThanMax >= 3)? savingsIntRateSilver: savingsIntRateGold;
                double interest = minimumBalanceCell.getNumericCellValue() * (interestRate/100);

                balanceCell.setCellValue(balanceCell.getNumericCellValue()+interest);
                minimumBalanceCell.setCellValue(balanceCell.getNumericCellValue());

                Row transactionRow = transactions.createRow(transactions.getPhysicalNumberOfRows());

                Cell accNoCell = transactionRow.createCell(0);
                Cell dateCell = transactionRow.createCell(1);
                Cell transactionTypeCell = transactionRow.createCell(2);
                Cell amtCell = transactionRow.createCell(3);
                Cell transactionDetailCell = transactionRow.createCell(4);
                dateCell.setCellStyle(cellStyle);

                accNoCell.setCellValue(accNo);
                dateCell.setCellValue(new Date());
                transactionTypeCell.setCellValue("CR");
                amtCell.setCellValue(interest);
                transactionDetailCell.setCellValue("Interest Accrual");
            }

            in.close();

            OutputStream out = new FileOutputStream(path);
            workbook.write(out);
            out.flush();
            out.close();

        } catch (IOException e) {

        }
    }

    private static boolean isSameMonth(@NotNull Date date) {
        LocalDate localDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int month = localDate.getMonthValue();
        int year = localDate.getYear();

        LocalDate localDate1 = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int month1 = localDate1.getMonthValue();
        int year1 = localDate.getYear();

        return month == month1 && year == year1;
    }
}
