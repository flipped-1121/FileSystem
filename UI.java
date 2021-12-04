import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;

/**
 * @Author: Kang
 * @Version 1.0
 * @Package: PACKAGE_NAME
 * @CreateTime: 2021/12/1 22:30
 * @Software: IntelliJ IDEA
 */

public class UI extends JFrame {
    // 初始化
    private final JTree tree;
    private final tableModel model = new tableModel();
    private final JTable fileTable;
    private final JPopupMenu myMenu = new JPopupMenu();

    private final File rootFile;
    private FileWriter readMeWrite;

    private final ArrayList<Block> blocks = new ArrayList<Block>();

    private final JTextField searchLine = new JTextField();


    // 删除文件夹
    public static void deleteDirectory(String filePath){
        File file = new File(filePath);
        if(!file.exists()){
            return;
        }
        if(file.isFile()){
            file.delete();
        }else if(file.isDirectory()){
            File[] files = file.listFiles();
            assert files != null;
            for (File myfile : files) {
                deleteDirectory(filePath + File.separator + myfile.getName());
            }
            file.delete();
        }
    }


    // 获取本地目录
    public double getSpace(File file){
        double space = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            reader.readLine();
            space = Double.parseDouble(reader.readLine());
            if (space > 1024){
                space = 0.0;
            }
            reader.close();
        } catch (Exception ignored){};
        return space;
    }


    // 搜索文件
    public boolean searchFile(String fileName, File parent){
        File [] files = parent.listFiles();
        assert files != null;
        for (File myFile:files){
            if (myFile.getName().equals(fileName)){
                try {
                    if(Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(myFile);
                        return true;
                    }
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, myFile.getPath() + "啊哦，无法进行", "失败",
                            JOptionPane.ERROR_MESSAGE);
                    return true;
                }
            }
            if (myFile.isDirectory() && myFile.canRead()){
                if(searchFile(fileName, myFile)){
                    return true;
                }
            }
        }
        return false;
    }


    // 用户界面UI
    public UI() throws IOException {
        setTitle("文件系统 by Kang");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 初始化根目录
        String path = File.listRoots()[0].getPath();
        String rootPath = new String();
        JFileChooser chooser = new JFileChooser(path);
        chooser.setDialogTitle("选择根目录");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setPreferredSize(new Dimension(800, 600));
        int result = chooser.showOpenDialog(this);
        if (result == chooser.APPROVE_OPTION){
            System.out.println(chooser.getSelectedFile().getAbsolutePath());
            rootPath = chooser.getSelectedFile().getPath();
        }

        // 创建工作区
        rootFile = new File(rootPath + File.separator + "myFileSystem");
        File readMe = new File(rootPath + File.separator + "myFileSystem" + File.separator + "ReadMe.txt");

        boolean flag = true;

        // 初始化 JTree
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(new myFiles(rootFile, 0, 10240));
        if (!rootFile.exists()) {
            flag = false;
            try {
                rootFile.mkdir();
                readMe.createNewFile();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "此路径不支持创建文件夹!", "错误", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }

        Block block1 = new Block(1, new File(rootFile.getPath() + File.separator + "1"), flag);
        blocks.add(block1);
        Block block2 = new Block(2, new File(rootFile.getPath() + File.separator + "2"), flag);
        blocks.add(block2);
        Block block3 = new Block(3, new File(rootFile.getPath() + File.separator + "3"), flag);
        blocks.add(block3);

        root.add(new DefaultMutableTreeNode(new myFiles(block1.getBlockFile(), 1, 1024.0)));
        model.addRow(new myFiles(block1.getBlockFile(), 1, 1024.0));
        ((DefaultMutableTreeNode)root.getChildAt(0)).add(new DefaultMutableTreeNode("temp"));

        root.add(new DefaultMutableTreeNode(new myFiles(block2.getBlockFile(), 2, 1024.0)));
        model.addRow(new myFiles(block2.getBlockFile(), 2, 1024.0));
        ((DefaultMutableTreeNode)root.getChildAt(1)).add(new DefaultMutableTreeNode("temp"));

        root.add(new DefaultMutableTreeNode(new myFiles(block3.getBlockFile(), 3, 1024.0)));
        model.addRow(new myFiles(block3.getBlockFile(), 3, 1024.0));
        ((DefaultMutableTreeNode)root.getChildAt(2)).add(new DefaultMutableTreeNode("temp"));

        root.add(new DefaultMutableTreeNode(new myFiles(readMe, 0, 0)));
        model.addRow(new myFiles(readMe, 0, 0));

        // 初始化文件信息表
        fileTable = new JTable(model);
        fileTable.getTableHeader().setFont(new Font(Font.DIALOG, Font.BOLD,24));
        fileTable.setSelectionBackground(Color.ORANGE);

        fileTable.updateUI();

        final DefaultTreeModel treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.setEditable(false);
        tree.putClientProperty("Jtree.lineStyle", "Angled");
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode parent = null;
                TreePath parentPath = e.getPath();
                if (parentPath == null){
                    parent = root;
                }else{
                    parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                }
                int blockName = ((myFiles)parent.getUserObject()).getBlockName();
                Block currentBlock = blocks.get(blockName - 1);
                if (parentPath == null){
                    parent = root;
                }else{
                    parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                }

                model.removeRows(0, model.getRowCount());
                File rootFile = new File(((myFiles)parent.getUserObject()).getFilePath());
                if (parent.getChildCount() > 0) {
                    File[] childFiles = rootFile.listFiles();

                    assert childFiles != null;
                    for (File file : childFiles) {
                        model.addRow(new myFiles(file, blockName, getSpace(file)));
                    }
                }
                else{
                    model.addRow(new myFiles(rootFile, blockName, getSpace(rootFile)));
                }
                fileTable.updateUI();

            }
        });
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode parent = null;
                TreePath parentPath = event.getPath();
                if (parentPath == null){
                    parent = root;
                }else{
                    parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                }

                int blockName = ((myFiles)parent.getUserObject()).getBlockName();

                File rootFile = new File(((myFiles)parent.getUserObject()).getFilePath());
                File [] childFiles = rootFile.listFiles();

                model.removeRows(0, model.getRowCount());
                for (File myFile : childFiles){
                    DefaultMutableTreeNode node = null;
                    node = new DefaultMutableTreeNode(new myFiles(myFile, blockName, getSpace(myFile)));
                    if (myFile.isDirectory() && myFile.canRead()) {
                        node.add(new DefaultMutableTreeNode("temp"));
                    }

                    treeModel.insertNodeInto(node, parent,parent.getChildCount());
                    model.addRow(new myFiles(myFile, blockName, getSpace(myFile)));
                }
                if (parent.getChildAt(0).toString().equals("temp") && parent.getChildCount() != 1)
                    treeModel.removeNodeFromParent((MutableTreeNode) parent.getChildAt(0));
                fileTable.updateUI();
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode parent = null;
                TreePath parentPath = event.getPath();
                if (parentPath == null){
                    parent = root;
                }else{
                    parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                }
                if (parent.getChildCount() > 0) {
                    int count = parent.getChildCount();
                    for (int i = count - 1; i >= 0; i--){
                        treeModel.removeNodeFromParent((MutableTreeNode) parent.getChildAt(i));
                    }
                    treeModel.insertNodeInto(new DefaultMutableTreeNode("temp"), parent,parent.getChildCount());
                }
                model.removeRows(0, model.getRowCount());
                fileTable.updateUI();
            }
        });
        JScrollPane treePane = new JScrollPane(tree);
        treePane.setPreferredSize(new Dimension(250, 400));
        add(treePane, BorderLayout.WEST);

        JScrollPane tablePane = new JScrollPane(fileTable);
        add(tablePane, BorderLayout.CENTER);
        
        // 双击打开文件
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1){
                    String fileName = ((String) model.getValueAt(fileTable.getSelectedRow(), 0));
                    String filePath = ((String) model.getValueAt(fileTable.getSelectedRow(), 1));
                    try {
                        if(Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.open(new File(filePath));
                        }
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Sorry, some thing wrong!", "Fail to open",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    JOptionPane.showMessageDialog(null, "文件名: " + fileName + "\n 文件路径: " + filePath, "内容",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        // 初始化菜单
        final JPopupMenu myMenu = new JPopupMenu();
        myMenu.setPreferredSize(new Dimension(200, 200));
        
        // 创建文件
        JMenuItem createFileItem = new JMenuItem("新建文件");
        createFileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                myFiles temp = (myFiles)node.getUserObject();
                int blockName = temp.getBlockName();
                Block currentBlock = blocks.get(blockName - 1);

                String inputValue;
                double capacity;

                JOptionPane inputPane = new JOptionPane();
                inputPane.setPreferredSize(new Dimension(600, 600));
                inputPane.setInputValue(JOptionPane.showInputDialog("文件名:"));
                if (inputPane.getInputValue() == null) {
                    return;
                }
                inputValue = inputPane.getInputValue().toString();
                inputPane.setInputValue(JOptionPane.showInputDialog("文件大小(KB):"));
                if (inputPane.getInputValue() == null) {
                    return;
                }
                capacity = Double.parseDouble(inputPane.getInputValue().toString());

                File newFile = new File(temp.getFilePath() + File.separator + inputValue);
                if (!newFile.exists() && inputValue != null){
                    try {
                        if (currentBlock.createFile(newFile, capacity)) {
                            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new myFiles(newFile, blockName, capacity));
                            model.removeRows(0, model.getRowCount());
                            model.addRow(new myFiles(newFile, blockName, capacity));
                            fileTable.updateUI();
                            JOptionPane.showMessageDialog(null, "创建成功，请刷新！！!", "✅成功", JOptionPane.PLAIN_MESSAGE);
                        }
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "创建失败！！！", "❌失败", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        myMenu.add(createFileItem);

        // 创建文件夹
        JMenuItem createDirItem = new JMenuItem("新建目录");
        createDirItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                myFiles temp = (myFiles)node.getUserObject();
                int blockName = temp.getBlockName();
                Block currentBlock = blocks.get(blockName - 1);
                String inputValue = JOptionPane.showInputDialog("目录名称:");
                if (inputValue == null) {
                    return;
                }
                File newDir = new File(temp.getFilePath() + File.separator + inputValue);
                if (newDir.exists())
                    deleteDirectory(newDir.getPath());
                try{
                    newDir.mkdir();
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new myFiles(newDir, blockName, 0));
                    newNode.add(new DefaultMutableTreeNode("temp"));
                    model.removeRows(0, model.getRowCount());
                    model.addRow(new myFiles(newDir, blockName, 0));
                    fileTable.updateUI();
                    JOptionPane.showMessageDialog(null, "创建成功，请刷新！！!", "✅成功", JOptionPane.PLAIN_MESSAGE);
                }catch (Exception E){
                    JOptionPane.showMessageDialog(null, "创建失败！！！", "❌失败", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        myMenu.add(createDirItem);

        // 删除
        JMenuItem deleteItem = new JMenuItem("删除");
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                myFiles temp = (myFiles)node.getUserObject();
                int blockName = temp.getBlockName();
                Block currentBlock = blocks.get(blockName - 1);
                int choose = JOptionPane.showConfirmDialog(null, "是否确认删除?", "⚠确认", JOptionPane.YES_NO_OPTION);
                if (choose == 0){
                    if (currentBlock.deleteFile(temp.getMyFile(), temp.getSpace())){
                        try {
                            currentBlock.rewriteBitMap();
                            currentBlock.rewriteRecoverWriter();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        JOptionPane.showMessageDialog(null, "删除成功，请刷新！！!", "✅成功", JOptionPane.PLAIN_MESSAGE);
                    }else{
                        JOptionPane.showMessageDialog(null, "删除失败！！！", "❌失败", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        myMenu.add(deleteItem);

        // 重命名
        JMenuItem renameItem = new JMenuItem("重命名");
        renameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                myFiles temp = (myFiles)node.getUserObject();
                int blockName = temp.getBlockName();
                Block currentBlock = blocks.get(blockName - 1);

                String inputValue = null;
                JOptionPane inputPane = new JOptionPane();
                inputPane.setInputValue(JOptionPane.showInputDialog("新文件名:"));
                if (inputPane.getInputValue() == null) {
                    return;
                }
                inputValue = inputPane.getInputValue().toString();
                try {
                    currentBlock.renameFile(temp.getMyFile(), inputValue, temp.getSpace());
                    JOptionPane.showMessageDialog(null, "重命名成功，请刷新！！!", "✅成功", JOptionPane.PLAIN_MESSAGE);
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, "重命名失败！！！", "❌失败", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        myMenu.add(renameItem);

        // 初始化搜索
        JPanel searchPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JLabel searchLabel = new JLabel("搜索一下");
        searchPane.add(searchLabel);
        searchLine.setPreferredSize(new Dimension(500, 50));
        searchPane.add(searchLine);
        JButton searchButton = new JButton("GO");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = searchLine.getText();
                if(!searchFile(fileName, rootFile)){
                    JOptionPane.showMessageDialog(null, "啊哦，没有找到~~~", "失败!", JOptionPane.WARNING_MESSAGE);
                }
                searchLine.setText("");
            }
        });
        searchPane.add(searchButton);
        add(searchPane, BorderLayout.NORTH);

        // 监听文件树
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON3){
                    myMenu.show(e.getComponent(), e.getX(), e.getY());

                }
            }
        });

        setSize(1200, 600);
        setVisible(true);
    }

    // 启动文件系统 主函数
    public static void main(String[] args) throws IOException {
        new UI();
    }
}
