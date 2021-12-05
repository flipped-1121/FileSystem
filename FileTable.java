import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

/**
 * @Author: Kang
 * @Version 1.1
 * @Package: PACKAGE_NAME
 * @CreateTime: 2021/12/1 22:13
 * @Software: IntelliJ IDEA
 */

public class FileTable extends AbstractTableModel {
    private Vector<Vector<String>> content = null;
    private final String[] title_name = { "文件名", "文件路径", "类型", "文件大小/KB", "最后修改时间"};

    public FileTable(){
        content = new Vector<>();
    }

    public void addRow(MyFiles myFile){
        Vector<String> v = new Vector<>();
        DecimalFormat format=new DecimalFormat("#0.00");
        v.add(0, myFile.getFileName());
        v.add(1, myFile.getFilePath());
        if (myFile.getMyFile().isFile()){
            v.add(2, "文件");
            v.add(3, format.format(myFile.getSpace()));
        }else {
            v.add(2, "目录");
            v.add(3, "-");
        }
        long time = myFile.getMyFile().lastModified();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String ctime = dateFormat.format(new Date(time));
        v.add(4, ctime);
        content.add(v);
    }

    public void removeRow(String name) {
        for (int i = 0; i < content.size(); i++){
            if ((content.get(i)).get(0).equals(name)){
                content.remove(i);
                break;
            }
        }
    }

    public void removeRows(int row, int count){
        for (int i = 0; i < count; i++){
            if (content.size() > row){
                content.remove(row);
            }
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int colIndex){
        (content.get(rowIndex)).remove(colIndex);
        (content.get(rowIndex)).add(colIndex, (String) value);
        this.fireTableCellUpdated(rowIndex, colIndex);
    }

    public String getColumnName(int col) {
        return title_name[col];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex){
        return false;
    }

    @Override
    public int getRowCount() {
        return content.size();
    }

    @Override
    public int getColumnCount() {
        return title_name.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return (content.get(rowIndex)).get(columnIndex);
    }
}
