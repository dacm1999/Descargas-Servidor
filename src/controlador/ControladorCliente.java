package controlador;

import gui.VistaCliente;
import tasks.Transferencia;
import tasks.WorkerDescarga;
import tasks.WorkerSubida;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ControladorCliente implements ActionListener {

    public VistaCliente vistaCliente;
    public List<String> listaDescargas;
    List<String> listaDes;
    public Transferencia t;
    private Socket cliente;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private DefaultListModel modelo;
    private boolean estado;
    private String ficheroSeleccionado;
    private File fichero;
    private int opcion;


    public ControladorCliente(VistaCliente vistaCliente) {
        this.vistaCliente = vistaCliente;
        vincularListener(this);

        listaDescargas = new ArrayList<>();
        ficheroSeleccionado = "";
//        modelo = new DefaultListModel();
//        vistaCliente.listaGUI.setModel(modelo);
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
                Thread hilo = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                                opcion = 1;
                                salida.writeInt(opcion);
                                descargar();
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, "Conexion perdida", "Error de conexion", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                hilo.start();
                break;
            }
            case "Refrescar Archivos Disponibles": {
                Thread hilo = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            opcion = 0;
                            salida.writeInt(opcion);
                            salida.flush();
                            listaDes = (List<String>) entrada.readObject();

                            listaFicheros(listaDes);

                            for (String fichero : listaDes) {
                                System.out.println(fichero);
                            }
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, "Conexion perdida", "Error de conexion", JOptionPane.ERROR_MESSAGE);
                            System.exit(0);
                        } catch (ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
                hilo.start();
                break;
            }
            case "Subir": {
                try {
                    opcion = 2;
                    salida.writeInt(opcion);
                    subirFichero();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
        }
    }

    private void descargar() {
        if (vistaCliente.listaGUI.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(null, "Seleccione un fichero.", "Error de descarga", JOptionPane.ERROR_MESSAGE);
        } else {
            ficheroSeleccionado = (String) vistaCliente.listaGUI.getSelectedValue();
            fichero = new File(ficheroSeleccionado);
            System.out.println("Fichero seleccionado " + ficheroSeleccionado + " " + getClass());
            WorkerDescarga descarga = new WorkerDescarga(entrada, salida, vistaCliente, ficheroSeleccionado, fichero);
            descarga.execute();

        }
    }

    private void subirFichero() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona el archivo a subir");
        int seleccion = fileChooser.showOpenDialog(null);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            fichero = fileChooser.getSelectedFile();
            WorkerSubida workerEnvio = new WorkerSubida(salida, vistaCliente, fichero);
            workerEnvio.execute();
        }
    }

    private void listaFicheros(List<String> lista) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Estoy en el EDT: " + SwingUtilities.isEventDispatchThread());

            try {
                DefaultListModel modelo = new DefaultListModel<>();
                for (String fichero : lista) {
                    modelo.addElement(fichero);
                }
                vistaCliente.listaGUI.setModel(modelo);
            } catch (NullPointerException ex) {
                System.out.println("ERROR: " + ex.getMessage());
                throw new RuntimeException(ex);
            } finally {
                System.out.println("Ficheros subidos: ");
                lista.forEach(System.out::println);
            }
        });
    }

    public void conectar() {
        try {
            String ip = vistaCliente.txfIp.getText();
            int port = 12345;
            cliente = new Socket(ip, port);
            System.out.println("Cliente iniciado " + cliente.getInetAddress());
            salida = new ObjectOutputStream(cliente.getOutputStream());
            entrada = new ObjectInputStream(cliente.getInputStream());
            System.out.println("Se ha creado el flujo");
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
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
