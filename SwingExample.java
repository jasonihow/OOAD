import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SwingExample {
    public static void main(String[] args) {
        // 創建 JFrame（視窗）
        JFrame frame = new JFrame("Swing Example");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        // 創建 JLabel（標籤）
        JLabel label = new JLabel("Hello, Swing!");

        // 創建 JButton（按鈕）
        JButton button = new JButton("Click Me");

        // 按鈕的事件監聽
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                label.setText("Button Clicked!");
            }
        });

        // 將元件加到視窗
        frame.add(label);
        frame.add(button);

        // 顯示視窗
        frame.setVisible(true);
    }
}
