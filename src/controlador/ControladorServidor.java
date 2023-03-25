package controlador;

import gui.VistaServidor;
import tasks.HiloCliente;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private List<String> listaDescargas;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private int contadorClientes;
    private String nombreCliente;

    /**
     * Constructor de la clase ControladorServidor
     *
     * @param vistaServidor
     */
    public ControladorServidor(VistaServidor vistaServidor) {
        this.vistaServidor = vistaServidor;
        vincularListeners(this);

        nombreCliente = "";
        listaDescargas = new ArrayList();
        estado = true;
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
            vistaServidor.btnIniciar.setText("Servidor Iniciado");
            System.out.println("SERVIDOR INICIADO");
            estado = true;
            Thread hilo = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (estado) {
                        try {
                            cliente = serverSocket.accept();
                            contadorClientes++;
                            nombreCliente =  "Cliente " + contadorClientes;
//                            salida = new ObjectOutputStream(cliente.getOutputStream());
//                            salida.writeUTF(nombreCliente);
//                            salida.flush();
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    vistaServidor.txtClientes.append(cliente.getInetAddress().getHostAddress() + " " + nombreCliente + " conectado\n");
                                    System.out.println("Cliente conectado: " + cliente.getInetAddress() + " " + nombreCliente);

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
