package gui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import controlador.ControladorCliente;
import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author danie
 * @version 1.0
 */
public class VistaCliente {
    public JButton btnDescargarFichero;
    public JButton btnConectar;
    public JButton btnRefrescar;
    public JButton btnSubirFichero;
    public JTextField txfIp;
    public JLabel lblEstado;
    public JList listaGUI;
    public JTextField txfCliente;
    public JProgressBar barraProgreso;
    public JButton btnCancelar;
    public JButton btnPausar;
    private JPanel pnlPrincipal;
    private JLabel lbl1;
    private JPanel pnl2;

    private VistaCliente() {
        JFrame frame = new JFrame("Cliente");
        frame.setContentPane(pnlPrincipal);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 500);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     *Inicio del programa cliente
     * @param args
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(VistaCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        new ControladorCliente(new VistaCliente());
    }
}
