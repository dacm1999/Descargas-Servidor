package controlador;

import gui.VistaServidor;
import tasks.HiloCliente;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ControladorServidor implements ActionListener {

    public VistaServidor vistaServidor;
    private ServerSocket serverSocket;
    private Socket cliente;
    private boolean estado;
    public DefaultListModel modelo;
    private List <String> listaDescargas;

    /**
     * Constructor de la clase ControladorServidor
     * @param vistaServidor
     */
    public ControladorServidor(VistaServidor vistaServidor) {
        this.vistaServidor = vistaServidor;
        vincularListeners(this);

        listaDescargas = new ArrayList();
        estado = true;
//
//        modelo = new DefaultListModel();
//        vistaServidor.listaGUI.setModel(modelo);
//        listaDescargas = vistaServidor.listaGUI.getSelectedValuesList();
    }


    private void vincularListeners(ActionListener listener) {
        vistaServidor.btnIniciar.addActionListener(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String comando = e.getActionCommand();

        switch (comando) {
            case "Iniciar Servidor": {
                inicio();
                break;
            }
        }
    }

    /**
     * Inicia el servidor creando un socket en el puerto 12345 y aceptando conexiones entrantes.
     * También inicia un hilo que se encarga de manejar las conexiones entrantes y crea un objeto
     * Instancia la clase HiloCliente para cada cliente que se conecte aceptada.
     */
    public void inicio() {
        try {

            serverSocket = new ServerSocket(12345);
//            listaDescargas = (List<String>) vistaServidor.lista;

            vistaServidor.btnIniciar.setText("Servidor Iniciado");
            System.out.println("SERVIDOR INICIADO");
            estado = true;
            Thread hilo = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (estado) {
                        try {
                            cliente = serverSocket.accept();
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    vistaServidor.txtClientes.append(cliente.getInetAddress().getHostAddress() + " conectado\n");
                                    System.out.println("Cliente conectado: " + cliente.getInetAddress());

                                }
                            });
                            HiloCliente cliente1 = new HiloCliente(cliente,listaDescargas,vistaServidor);
                            cliente1.start();
                        } catch (IOException e) {
                            System.out.println("ERROR: " + e.getMessage());
                        }
                    }
                }
            });
            hilo.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
