package com.xiaofan.fanmacro.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 主窗口 - AI 生成和代码编辑（Swing 版本）
 */
public class MainWindow extends JFrame {
    
    private JTextArea codeEditor;
    private JTextField macroNameField;
    
    // AI 生成相关组件
    private JTextArea aiRequestField;
    private JButton aiGenerateBtn;
    private JLabel aiStatusLabel;
    private String currentTakeId;
    private Thread pollingThread;
    
    public MainWindow() {
        setTitle("FanMacro AI 代码生成器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        // 代码编辑器
        codeEditor = new JTextArea();
        codeEditor.setFont(new Font("Consolas", Font.PLAIN, 12));
        codeEditor.setBackground(Color.WHITE);
        codeEditor.setTabSize(4);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部工具栏
        JToolBar toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);
        
        // 左侧：AI 生成区域
        JPanel leftPanel = createAISection();
        leftPanel.setPreferredSize(new Dimension(400, 0));
        add(leftPanel, BorderLayout.WEST);
        
        // 中间：代码编辑器
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBorder(new TitledBorder("FanMacro 代码编辑器"));
        
        JScrollPane editorScroll = new JScrollPane(codeEditor);
        editorScroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        editorPanel.add(editorScroll, BorderLayout.CENTER);
        
        add(editorPanel, BorderLayout.CENTER);
    }
    
    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        // 宏名称输入
        toolbar.add(new JLabel("宏名称:"));
        macroNameField = new JTextField(20);
        macroNameField.setToolTipText("输入宏文件名（不含.txt）");
        toolbar.add(macroNameField);
        
        toolbar.addSeparator();
        
        // 按钮
        JButton newBtn = new JButton("新建");
        newBtn.addActionListener(e -> {
            macroNameField.setText("");
            codeEditor.setText("");
        });
        toolbar.add(newBtn);
        
        JButton saveBtn = new JButton("保存宏文件");
        saveBtn.setFont(saveBtn.getFont().deriveFont(Font.BOLD));
        saveBtn.addActionListener(e -> saveMacroFile());
        toolbar.add(saveBtn);
        
        JButton loadBtn = new JButton("加载宏文件");
        loadBtn.addActionListener(e -> loadMacroFile());
        toolbar.add(loadBtn);
        
        JButton exportBtn = new JButton("导出代码");
        exportBtn.addActionListener(e -> exportCode());
        toolbar.add(exportBtn);
        
        return toolbar;
    }
    
    private void saveMacroFile() {
        String macroName = macroNameField.getText().trim();
        if (macroName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入宏名称", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String code = codeEditor.getText().trim();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "代码为空，无法保存", "警告", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 选择保存目录
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择宏文件保存目录");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        // 尝试使用默认目录
        String userHome = System.getProperty("user.home");
        File defaultDir = new File(userHome, ".minecraft/config/do");
        if (defaultDir.exists()) {
            fileChooser.setCurrentDirectory(defaultDir);
        }
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            try {
                File macroFile = new File(selectedDir, macroName + ".txt");
                Files.write(macroFile.toPath(), code.getBytes("UTF-8"));
                JOptionPane.showMessageDialog(this, "宏文件已保存到:\n" + macroFile.getAbsolutePath(), 
                    "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "保存失败: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadMacroFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("加载宏文件");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("宏文件", "txt"));
        
        String userHome = System.getProperty("user.home");
        File defaultDir = new File(userHome, ".minecraft/config/do");
        if (defaultDir.exists()) {
            fileChooser.setCurrentDirectory(defaultDir);
        }
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");
                String fileName = file.getName().replace(".txt", "");
                macroNameField.setText(fileName);
                codeEditor.setText(content);
                JOptionPane.showMessageDialog(this, "已加载文件", 
                    "提示", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "加载失败: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportCode() {
        String code = codeEditor.getText().trim();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "代码为空，无法导出", "警告", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出代码");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("文本文件", "txt"));
        fileChooser.setSelectedFile(new File("fanmacro.txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                Files.write(file.toPath(), code.getBytes("UTF-8"));
                JOptionPane.showMessageDialog(this, "代码已导出到:\n" + file.getAbsolutePath(), 
                    "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "导出失败: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 创建 AI 生成区域
     */
    private JPanel createAISection() {
        JPanel aiSection = new JPanel(new BorderLayout());
        aiSection.setBorder(new TitledBorder("🤖 AI 智能生成"));
        aiSection.setBackground(new Color(240, 240, 240));
        aiSection.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(new Color(240, 240, 240));
        
        // 需求输入框
        JLabel requestLabel = new JLabel("功能需求描述:");
        requestLabel.setFont(requestLabel.getFont().deriveFont(Font.BOLD));
        
        aiRequestField = new JTextArea(8, 20);
        aiRequestField.setLineWrap(true);
        aiRequestField.setWrapStyleWord(true);
        aiRequestField.setToolTipText("例如：创建一个自动挖矿的宏，挖掘铁矿石");
        JScrollPane requestScroll = new JScrollPane(aiRequestField);
        
        // 生成按钮
        aiGenerateBtn = new JButton("🚀 请求AI生成");
        aiGenerateBtn.setFont(aiGenerateBtn.getFont().deriveFont(Font.BOLD, 14f));
        aiGenerateBtn.setBackground(new Color(76, 175, 80));
        aiGenerateBtn.setForeground(Color.WHITE);
        aiGenerateBtn.addActionListener(e -> requestAIGeneration());
        
        // 状态标签
        aiStatusLabel = new JLabel("等待输入需求...");
        aiStatusLabel.setForeground(new Color(102, 102, 102));
        
        // 说明文字
        JPanel helpBox = createHelpBox();
        
        content.add(requestLabel, BorderLayout.NORTH);
        content.add(requestScroll, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(aiGenerateBtn, BorderLayout.CENTER);
        buttonPanel.add(aiStatusLabel, BorderLayout.SOUTH);
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.add(content, BorderLayout.NORTH);
        mainContent.add(helpBox, BorderLayout.CENTER);
        mainContent.setBackground(new Color(240, 240, 240));
        
        aiSection.add(mainContent, BorderLayout.CENTER);
        
        return aiSection;
    }
    
    /**
     * 创建帮助说明框
     */
    private JPanel createHelpBox() {
        JPanel helpBox = new JPanel();
        helpBox.setLayout(new BoxLayout(helpBox, BoxLayout.Y_AXIS));
        helpBox.setBorder(new TitledBorder("📝 如何写功能描述（小白必看）"));
        helpBox.setBackground(new Color(255, 249, 230));
        
        JLabel helpText1 = new JLabel("1. 描述要做什么：");
        helpText1.setFont(helpText1.getFont().deriveFont(Font.BOLD));
        
        JLabel helpText2 = new JLabel("   • 例如：\"自动挖矿，挖掘铁矿石\"");
        helpText2.setFont(helpText2.getFont().deriveFont(11f));
        helpText2.setForeground(new Color(102, 102, 102));
        
        JLabel helpText3 = new JLabel("   • 例如：\"移动到坐标(0, 85, -3062)\"");
        helpText3.setFont(helpText3.getFont().deriveFont(11f));
        helpText3.setForeground(new Color(102, 102, 102));
        
        JLabel helpText4 = new JLabel("2. 可以包含条件：");
        helpText4.setFont(helpText4.getFont().deriveFont(Font.BOLD));
        
        JLabel helpText5 = new JLabel("   • 例如：\"如果时间到了11000刻，就执行/home命令\"");
        helpText5.setFont(helpText5.getFont().deriveFont(11f));
        helpText5.setForeground(new Color(102, 102, 102));
        
        JLabel helpText6 = new JLabel("3. 注意事项：");
        helpText6.setFont(helpText6.getFont().deriveFont(Font.BOLD));
        
        JLabel helpText7 = new JLabel("   • 描述要清晰具体");
        helpText7.setFont(helpText7.getFont().deriveFont(11f));
        helpText7.setForeground(new Color(102, 102, 102));
        
        JLabel helpText8 = new JLabel("   • 不要写与宏代码无关的内容");
        helpText8.setFont(helpText8.getFont().deriveFont(11f));
        helpText8.setForeground(new Color(102, 102, 102));
        
        JLabel helpText9 = new JLabel("   • 生成后可以手动编辑代码");
        helpText9.setFont(helpText9.getFont().deriveFont(11f));
        helpText9.setForeground(new Color(102, 102, 102));
        
        helpBox.add(helpText1);
        helpBox.add(helpText2);
        helpBox.add(helpText3);
        helpBox.add(Box.createVerticalStrut(5));
        helpBox.add(helpText4);
        helpBox.add(helpText5);
        helpBox.add(Box.createVerticalStrut(5));
        helpBox.add(helpText6);
        helpBox.add(helpText7);
        helpBox.add(helpText8);
        helpBox.add(helpText9);
        
        return helpBox;
    }
    
    /**
     * 请求 AI 生成
     */
    private void requestAIGeneration() {
        String userText = aiRequestField.getText().trim();
        
        // 验证输入
        if (userText.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "请输入功能需求描述！\n\n提示：描述要做什么，例如：\n\"创建一个自动挖矿的宏，挖掘铁矿石\"", 
                "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 禁用按钮，显示状态
        aiGenerateBtn.setEnabled(false);
        aiStatusLabel.setText("正在提交请求...");
        aiStatusLabel.setForeground(new Color(33, 150, 243));
        
        // 提交请求
        AIClient.submitRequest(userText, 
            takeid -> {
                // 成功提交
                currentTakeId = takeid;
                SwingUtilities.invokeLater(() -> {
                    aiStatusLabel.setText("请求已提交，等待AI生成中...");
                    aiStatusLabel.setForeground(new Color(255, 152, 0));
                    startPolling();
                });
            },
            error -> {
                // 提交失败
                SwingUtilities.invokeLater(() -> {
                    aiGenerateBtn.setEnabled(true);
                    aiStatusLabel.setText("请求失败: " + error);
                    aiStatusLabel.setForeground(new Color(244, 67, 54));
                    JOptionPane.showMessageDialog(this, "提交请求失败:\n" + error, 
                        "错误", JOptionPane.ERROR_MESSAGE);
                });
            }
        );
    }
    
    /**
     * 开始轮询查询结果
     */
    private void startPolling() {
        if (pollingThread != null && pollingThread.isAlive()) {
            return;
        }
        
        pollingThread = new Thread(() -> {
            while (currentTakeId != null && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(2000); // 每2秒查询一次
                    
                    if (currentTakeId == null) break;
                    
                    final String takeId = currentTakeId;
                    AIClient.queryResult(takeId,
                        result -> {
                            SwingUtilities.invokeLater(() -> {
                                if (result == null) {
                                    // 还在生成中
                                    aiStatusLabel.setText("AI正在生成中，请稍候...");
                                } else {
                                    // 生成完成
                                    codeEditor.setText(result);
                                    aiStatusLabel.setText("✅ 生成完成！代码已显示在编辑器中");
                                    aiStatusLabel.setForeground(new Color(76, 175, 80));
                                    aiGenerateBtn.setEnabled(true);
                                    currentTakeId = null;
                                }
                            });
                        },
                        error -> {
                            SwingUtilities.invokeLater(() -> {
                                aiGenerateBtn.setEnabled(true);
                                aiStatusLabel.setText("查询失败: " + error);
                                aiStatusLabel.setForeground(new Color(244, 67, 54));
                                currentTakeId = null;
                            });
                        }
                    );
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        
        pollingThread.start();
    }
}
