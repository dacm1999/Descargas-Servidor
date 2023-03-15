package controlador;

import gui.VistaCliente;
import tasks.Transferencia;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ControladorCliente implements ActionListener {

    public VistaCliente vistaCliente;
    public  ArrayList<Transferencia> listaDescargas;
    public Transferencia t;
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private DefaultListModel modelo;

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
                ficheros();
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
    }

    private void ficheros() {
    }

    public void conectar(){
        System.out.println("HOLA");
    }


}
