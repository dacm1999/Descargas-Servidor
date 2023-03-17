package controlador;

import gui.VistaCliente;
import tasks.Transferencia;
import tasks.WorkerEnvio;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class ControladorCliente implements ActionListener {

    public VistaCliente vistaCliente;
    public ArrayList<Transferencia> listaDescargas;
    public Transferencia t;
    private Socket cliente;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private DefaultListModel modelo;
    private boolean estado;
    private File fichero;


    public ControladorCliente(VistaCliente vistaCliente) {
        this.vistaCliente = vistaCliente;
        vincularListener(this);

        listaDescargas = new ArrayList<>();
        modelo = new DefaultListModel();
        vistaCliente.listaGUI.setModel(modelo);
    }

    private void vincularListener(ActionListener listener) {

        vistaCliente.btnDescargarFichero.addActionListener(listener);
        vistaCliente.btnConectar.addActionListener(listener);
        vistaCliente.btnRefrescar.addActionListener(listener);
        vistaCliente.btnSubirFichero.addActionListener(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String comando = e.getActionCommand();
        switch (comando) {
            case "Conectar": {
                conectar();
                break;
            }
            case "Descargar": {
                descargar();
                break;
            }
            case "Refrescar Archivos Disponibles": {
                //refrescar();
                break;
            }
            case "Subir": {
                subirFichero();
                break;
            }
        }
    }

    private void descargar() {

    }

    private void subirFichero() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona el archivo a enviar");
        int seleccion = fileChooser.showOpenDialog(null);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            fichero = fileChooser.getSelectedFile();
            WorkerEnvio workerEnvio = new WorkerEnvio(salida, vistaCliente, fichero);
            workerEnvio.execute();
        }
    }

    private void refrescar() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Se borra la lista de descargas en la vista.
                modelo.clear();

                // Se itera a través de la lista de descargas.
                Iterator var1 = listaDescargas.iterator();
                while (var1.hasNext()) {
                    // Se obtiene la próxima descarga.
                    String descarga = (String) var1.next();

                    // Se agrega la descarga a la lista en la vista.
                    modelo.addElement(descarga);
                }
            }
        });
    }

    public void conectar() {
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String ip = vistaCliente.txfIp.getText();
                    int port = 12345;
                    System.out.println("ESTOY EN EL HILO CONTROLADOR");
                    cliente = new Socket(ip, port);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            vistaCliente.btnSubirFichero.setEnabled(true);
                            vistaCliente.btnConectar.setText("Conectado");
                            vistaCliente.btnDescargarFichero.setEnabled(true);
                            vistaCliente.btnRefrescar.setEnabled(true);
                            vistaCliente.btnConectar.setEnabled(false);
                        }
                    });
                    salida = new ObjectOutputStream(cliente.getOutputStream());
                    entrada = new ObjectInputStream(cliente.getInputStream());



                } catch (UnknownHostException e) {
                    JOptionPane.showMessageDialog(null, "No se pudo establecer la conexión con el servidor.", "Error de conexión", JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Conexion perdida.", "Error de conexión", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        hilo.start();
    }

}
