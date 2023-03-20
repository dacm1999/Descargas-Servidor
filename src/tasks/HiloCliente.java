package tasks;

import controlador.ControladorServidor;
import gui.VistaServidor;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;

public class HiloCliente extends Thread {


    private Socket socketCliente;
    private List<String> listaDescargas;
    private VistaServidor vista;

    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private ControladorServidor controladorServidor;

    /**
     * Constructor de la clase HiloCliente
     * @param socketCliente
     * @param listaDescargas
     * @param vista
     */
    public HiloCliente(Socket socketCliente, List<String> listaDescargas, VistaServidor vista) {
        this.socketCliente = socketCliente;
        this.listaDescargas = listaDescargas;
        this.vista = vista;
    }

    /**
     */
    @Override
    public void run() {
        try {
            this.salida = new ObjectOutputStream(this.socketCliente.getOutputStream());
            this.entrada = new ObjectInputStream(this.socketCliente.getInputStream());

            // Agrega el nombre del cliente a la lista de clientes y actualiza la vista
            String ip = String.valueOf(this.socketCliente.getInetAddress());

            // Maneja las solicitudes del cliente
            opciones();
            refrescarDescargas();

        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            refrescarDescargas();
        }
    }

    private void opciones() {
        while (true) {
            try {
                int seleccion = entrada.readInt();
                System.out.println("opcion seleccionada " + seleccion);
                switch (seleccion) {
                    case 0: {
                        System.out.println("Refrescar button " + " seleccion " + seleccion);
                        enviarListaFicheros();
                        break;
                    }

                    case 1: {

                    }
                    case 2: {
                        System.out.println("Descargar button " + " seleccion" + seleccion);
                        descargas();
                        break;
                    }

                    case 3: {
                        ficheroSubido();
                        refrescarDescargas();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Envio la lista de ficheros
     */
    private void enviarListaFicheros() {
        try {
            salida.writeObject(listaDescargas);
            salida.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ficheroSubido() {

        try {
            long tamanoFichero = this.entrada.readLong();
            String nombreFichero = (String) this.entrada.readObject();
            System.out.println("Nombre del fichero " + nombreFichero + "Tamaño del fichero a recibir: " + tamanoFichero);

            if (tamanoFichero > 0L) {
                int i = 1;
//                String nombreTemporal = nombreFichero;
//                while (listaDescargas.contains(nombreTemporal)) {
//                    nombreTemporal = i + "_" + nombreFichero;
//                    listaDescargas.add(nombreTemporal);
//                    ++i;
//                }

                System.out.println("Subiendo archivo: " + nombreFichero + ", tamaño: " + tamanoFichero + getClass());
                FileOutputStream ficheroSalida = new FileOutputStream(nombreFichero);
                long totalWrited = 0L;
                byte[] buffer = new byte[1024];
                int bytesLeidos;

                while (tamanoFichero > totalWrited && (bytesLeidos = entrada.read(buffer)) > 0) {
                    ficheroSalida.write(buffer, 0, bytesLeidos);
                    totalWrited += (long) bytesLeidos;
                }
                ficheroSalida.close();
                listaDescargas.add(nombreFichero);

                for (String nombre : listaDescargas) {
                    System.out.println("ficheros agregados: " + nombre);
//            long tamanoFichero = entrada.readLong();
//            System.out.println("Tamaño del fichero a recibir: " + tamanoFichero);
//
//            String nombreFichero = (String) entrada.readObject();
//            System.out.println("Nombre del fichero a recibir: " + nombreFichero);
//

//            FileOutputStream ficheroSalida = new FileOutputStream(nombreFichero);
//            byte[] buffer = new byte[1024];
//            int bytesLeidos;
//            long totalBytesLeidos = 0L;
//
//
//            while ((bytesLeidos = entrada.read(buffer)) > 0) {
//                ficheroSalida.write(buffer, 0, bytesLeidos);
//                totalBytesLeidos += (long) bytesLeidos;
//            }
//            System.out.println("Total leido " + totalBytesLeidos + " del fichero " + nombreFichero);
//
//            ficheroSalida.close();
//            listaDescargas.add(nombreFichero);
//
//            for(String nombre : listaDescargas){
//                System.out.println("Nombre de ficheros: " + nombre);
//            }

                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }


    private void descargas() {
        try {
            String nombreFichero = (String) this.entrada.readObject();
            File fichero = new File(nombreFichero);
            long fileSize = fichero.length();
            System.out.println("Nombre del fichero " + nombreFichero + " tamaño del fichero " + fichero);
            long totalEscrito = 0L;

            //Lo lee la clase WorkerDescarga
            this.salida.writeLong(fileSize);
            this.salida.flush();
            FileInputStream fileInputStream = new FileInputStream(fichero);
            byte[] buffer = new byte[1024];
            int bytesLeidos;

            while (fileSize > 0L && (bytesLeidos = fileInputStream.read(buffer)) > 0) {
                this.salida.write(buffer, 0, bytesLeidos);
                this.salida.flush();
                totalEscrito +=  bytesLeidos;
            }

            fileInputStream.close();
            System.out.println("Nombre del fichero " + nombreFichero + " BYTES ESCRTIOS " + totalEscrito);

        } catch (FileNotFoundException e) {
            System.out.println("Archivo no encontrado" + e.getClass());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Muestra los ficheros en la GUI
     */
    private void refrescarDescargas() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Estoy en el EDT: " + SwingUtilities.isEventDispatchThread());

                try {
                    DefaultListModel<String> modelo = new DefaultListModel<>();
                    for (String descarga : listaDescargas) {
                        modelo.addElement(descarga);
                    }
                    vista.listaGUI.setModel(modelo);
                } catch (NullPointerException ex) {
                    System.out.println("ERROR: " + ex.getMessage());
                    throw new RuntimeException(ex);
                } finally {
                    System.out.println("Ficheros subidos: ");
                    listaDescargas.forEach(System.out::println);
                }
            }
        });
    }


}
