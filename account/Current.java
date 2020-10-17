package account;

import Exceptions.AccountException;
import Exceptions.BelowZeroBalanceException;
import Exceptions.OverdraftException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Current extends Account{

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

            if (total < -3000)
                throw new BelowZeroBalanceException();
            else
                balanceCell.setCellValue(total);

            Cell withdrawalsTodayCell = accRow.getCell(6);
            Cell lastWithdrawalCell;

            try {
                lastWithdrawalCell = accRow.getCell(7);
                lastWithdrawalCell.setCellStyle(cellStyle);
                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                if (fmt.format(lastWithdrawalCell.getDateCellValue()).equals(fmt.format(new Date()))) {
                    withdrawalsTodayCell.setCellValue(withdrawalsTodayCell.getNumericCellValue()+1);
                    withdrawalsToday++;
                } else {
                    withdrawalsTodayCell.setCellValue(1);
                    lastWithdrawalCell.setCellValue(new Date());
                    withdrawalsToday = 1;
                }
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

            if (total < 0)
                issuesCell.setCellValue("Overdraft Limit");

            in.close();

            OutputStream out = new FileOutputStream(path);
            workbook.write(out);
            out.flush();
            out.close();

            if (total < 0)
                throw new OverdraftException();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
