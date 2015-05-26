import javax.swing.*;
import javax.swing.event.*;
import java.awt.Color;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

        MainFrame frame = new MainFrame();
        frame.setVisible(true);
    }

}
