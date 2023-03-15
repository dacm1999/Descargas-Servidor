package gui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import controlador.ControladorCliente;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VistaCliente {
    public JButton btnDescargarFichero;
    public JButton btnConectar;
    public JButton btnRefrescar;
    public JButton btnSubirFichero;
    public JList listaGUI;
    private JPanel pnlPrincipal;
    private JTextField txfIp;
    private JTextField txfCliente;
    private JLabel lbl1;
    private JProgressBar barraProgreso;


    private VistaCliente() {
        JFrame frame = new JFrame("Cliente");
        frame.setContentPane(pnlPrincipal);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 450);
        frame.setLocationRelativeTo(null);
        //listaGUI.setModel(modelo);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(VistaCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        new ControladorCliente(new VistaCliente());
    }
}
