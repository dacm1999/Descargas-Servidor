package tasks;

import controlador.ControladorServidor;
import gui.VistaServidor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HiloCliente extends Thread{


    private Socket socketCliente;
    private List<String> listaDescargas;
    private VistaServidor vista;

    private ObjectOutputStream salida;
    private ObjectInputStream entrada;

    public HiloCliente(Socket socketCliente, List<String> listaDescargas, VistaServidor vista) {
        this.socketCliente = socketCliente;
        this.listaDescargas = listaDescargas;
        this.vista = vista;
    }

    @Override
    public void run() {
        try {
            this.salida = new ObjectOutputStream(this.socketCliente.getOutputStream());
            this.entrada = new ObjectInputStream(this.socketCliente.getInputStream());



            // Agrega el nombre del cliente a la lista de clientes y actualiza la vista
            String ip = String.valueOf(this.socketCliente.getInetAddress());
            refrescarClientesEDT();

            // Maneja las solicitudes del cliente
            opciones();

        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            this.refrescarClientesEDT();
            // Cierra los flujos y el socket del cliente
            try {
                if (this.entrada != null) {
                    this.entrada.close();
                }
                if (this.salida != null) {
                    this.salida.close();
                }
                if (this.socketCliente != null) {
                    this.socketCliente.close();
                }

            } catch (IOException e) {
                System.err.println("Error al cerrar los flujos y el socket del cliente: " + e.getMessage());
            }
        }
        refrescarClientesEDT();
    }

    private void opciones() {
        while(true){
            try {
                String seleccion = entrada.readUTF();

                switch (seleccion) {
                    case  "subir": {
                        listaObjetos();
                    }

                    case "descargar": {

                    }

                    case "": {

                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void listaObjetos() {
        try {
            salida.writeObject(listaDescargas);
            salida.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ficheroSubido(){

        try {
            long fileSize = this.entrada.readLong();
            String nombre = (String)this.entrada.readObject();
            if (fileSize > 0L) {
                int i = 0;
                String nombreTemporal = nombre;

                FileOutputStream escritorFichero = new FileOutputStream(nombreTemporal);
                long totalEscrito = 0L;
                byte[] buffer = new byte[1024];
                int bytesLeidos;
                while (fileSize > totalEscrito && (bytesLeidos = this.entrada.read(buffer)) > 0) {
                    escritorFichero.write(buffer, "".length(), bytesLeidos);
                    totalEscrito += bytesLeidos;
                }
                escritorFichero.close();
                listaDescargas.add(nombreTemporal);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void descarga()  {
        try{
            String nombre = (String)this.entrada.readObject();

            File fichero = new File(nombre);
            long fileSize = fichero.length();
            long totalEscrito = 0L;
            this.salida.writeLong(fileSize);
            this.salida.flush();
            FileInputStream lectorFichero = new FileInputStream(fichero);
            byte[] buffer = new byte[429 + 853 - 264 + 6];

            int bytesLeidos;
            while(fileSize > 0L && (bytesLeidos = lectorFichero.read(buffer)) > 0) {
                this.salida.write(buffer, "".length(), bytesLeidos);
                this.salida.flush();
                totalEscrito += (long)bytesLeidos;
                "".length();
                if (0 >= 1) {
                    throw null;
                }
            }
            lectorFichero.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void refrescarClientesEDT() {
        ControladorServidor control = null;

        control.modelo.clear();
        for(String descarga : listaDescargas){
//            control.modelo.addElement(descarga());
        }
    }
}
