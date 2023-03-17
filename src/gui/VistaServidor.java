package gui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import controlador.ControladorServidor;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VistaServidor {
    public JButton btnIniciar;
    public JTextArea txtClientes;
    public JList lista;
    private JPanel pnlPrincipal;
    private JLabel lbl1;
    private JLabel lbl2;
    private JScrollPane scrollpnl;


    private VistaServidor() {
        JFrame frame = new JFrame("Servidor");
        frame.setContentPane(pnlPrincipal);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Definir tamano inicial
        frame.setSize(new Dimension(450,450));
        //centrar ventana inicial
        frame.setLocationRelativeTo(null);
        //modelo = new DefaultListModel<Transferencia>();
        //lista.setModel(modelo);
        //lista = new ArrayList<>();

        frame.setVisible(true);
    }
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(VistaServidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        new ControladorServidor(new VistaServidor());
    }

}
