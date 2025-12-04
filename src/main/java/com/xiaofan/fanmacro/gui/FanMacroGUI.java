package com.xiaofan.fanmacro.gui;

import javax.swing.*;
import java.awt.*;

/**
 * FanMacro 积木式编程 GUI 主程序（Swing 版本）
 */
public class FanMacroGUI {
    
    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 在 EDT 中创建和显示 GUI
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }
}
