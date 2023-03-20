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
import java.util.Iterator;
import java.util.List;

public class ControladorCliente implements ActionListener {

    public VistaCliente vistaCliente;
    public ArrayList<Transferencia> listaDescargas;
    public Transferencia t;
    private Socket cliente;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private DefaultListModel modelo;
    private boolean estado;
    private String nombre;
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
                    Thread hilo = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                int seleccion = 0;
                                salida.writeInt(seleccion);
                                salida.flush();
                                List <String >listaDes = (List<String>) entrada.readObject();

                                listaFicheros(listaDes);

                                for(String  fichero : listaDes){
                                    System.out.println(fichero);
                                }
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            } catch (ClassNotFoundException ex) {
                                throw new RuntimeException(ex);
                            }



                        }
                    });
                break;
            }
            case "Subir": {
                subirFichero();
                break;
            }
        }
    }

    private void descargar() {
        WorkerDescarga descarga = new WorkerDescarga(entrada,salida,vistaCliente,nombre,fichero);
        descarga.execute();
    }

    private void subirFichero() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona el archivo a enviar");
        int seleccion = fileChooser.showOpenDialog(null);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            fichero = fileChooser.getSelectedFile();
            WorkerSubida workerEnvio = new WorkerSubida(salida, vistaCliente, fichero);
            workerEnvio.execute();
        }
    }

    private void listaFicheros(List<String> lista) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Se borra la lista de descargas en la vista.
                modelo.clear();

                // Se itera a través de la lista de descargas.
                Iterator iterator = lista.iterator();
                while (iterator.hasNext()) {
                    // Se obtiene la próxima descarga.
                    String descarga = (String) iterator.next();
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
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try{

                            String ip = vistaCliente.txfIp.getText();
                            int port = 12345;
                            cliente = new Socket(ip, port);
                            System.out.println("Cliente iniciado " + cliente.getInetAddress());
                            salida = new ObjectOutputStream(cliente.getOutputStream());
                            entrada = new ObjectInputStream(cliente.getInputStream());
                            System.out.println("Se ha creado el flujo");


                            vistaCliente.btnSubirFichero.setEnabled(true);
                            vistaCliente.btnConectar.setText("Conectado");
                            vistaCliente.btnDescargarFichero.setEnabled(true);
                            vistaCliente.btnRefrescar.setEnabled(true);
                            vistaCliente.btnConectar.setEnabled(false);
                        } catch (UnknownHostException e) {
                            JOptionPane.showMessageDialog(null, "No se pudo establecer la conexión con el servidor.", "Error de conexión", JOptionPane.ERROR_MESSAGE);
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "No se pudo establecer la conexión con el servidor.", "Error de conexión", JOptionPane.ERROR_MESSAGE);
                        }

                    }
                });
            }
        });
        hilo.start();
    }

}
