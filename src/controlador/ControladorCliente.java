package controlador;

import gui.VistaCliente;
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
    private Socket cliente;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private DefaultListModel modelo;
    private boolean estado;
    private String ficheroSeleccionado;
    private File fichero;
    private int opcion;
    private String nombreCliente;
    private boolean esDescarga;
    private WorkerDescarga descarga;
    private WorkerSubida workerEnvio;

    /**
     * Constructor de la clase ControladorCliente
     * @param vistaCliente
     */
    public ControladorCliente(VistaCliente vistaCliente) {
        this.vistaCliente = vistaCliente;
        vincularListener(this);

        listaDescargas = new ArrayList<>();
        ficheroSeleccionado = "";
        nombreCliente = "";
        esDescarga = true;
    }

    /**
     * Este método vincula un ActionListener a los botones de la vista del cliente.
     * Los botones incluidos son btnDescargarFichero, btnConectar, btnRefrescar, btnSubirFichero, btnCancelar y btnPausar.
     * Cuando se presiona alguno de estos botones, el ActionListener vinculado será activado para ejecutar la acción correspondiente.
     *
     * @param listener
     */
    private void vincularListener(ActionListener listener) {
        vistaCliente.btnDescargarFichero.addActionListener(listener);
        vistaCliente.btnConectar.addActionListener(listener);
        vistaCliente.btnRefrescar.addActionListener(listener);
        vistaCliente.btnSubirFichero.addActionListener(listener);

        vistaCliente.btnCancelar.addActionListener(listener);
        vistaCliente.btnPausar.addActionListener(listener);
    }

    /**
     * Este es un método que implementa la interfaz ActionListener y responde a los eventos de acción generados por los componentes de la vista de la aplicación.
     * El método utiliza un switch-case para ejecutar diferentes acciones en función del comando que se haya recibido. Las acciones que se pueden realizar son:
     * <p>
     * Conectar: obtiene la dirección IP del servidor y crea un hilo para establecer una conexión con el servidor en segundo plano.
     * <p>
     * Descargar: inicia la descarga de un archivo seleccionado en la lista de archivos disponibles en el servidor.
     * Si no hay ningún archivo seleccionado, muestra un mensaje de error. Si se produce un error de conexión, muestra un mensaje de error y cierra la aplicación.
     * <p>
     * Refrescar Archivos Disponibles: obtiene la lista de archivos disponibles en el servidor y actualiza la lista de archivos en la vista.
     * <p>
     * Subir: inicia la subida de un archivo seleccionado en el equipo del usuario al servidor.
     * <p>
     * Pausar: pausa la descarga o subida de un archivo en curso, según corresponda.
     * <p>
     * Reanudar: reanuda la descarga o subida de un archivo en curso, según corresponda.
     * <p>
     * Cancelar: cancela la descarga de un archivo en curso y elimina el archivo descargado del equipo del usuario. (NO FUNCIONA PORQUE CIERRA LA CONEXION, ELIMINA EL ARCHIVO
     * Y CANCELA LA CONEXION PERO CIERRA LA CONEXION)
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String comando = e.getActionCommand();
        switch (comando) {
            case "Conectar": {
                System.out.println("ESTOY EN EL EDT " + SwingUtilities.isEventDispatchThread() + " " + getClass());
                String ip = vistaCliente.txfIp.getText();
                int port = 12345;
                Thread hilo = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        conectar(ip, port);
                    }
                });
                hilo.start();
                break;
            }

            case "Descargar": {
                Thread hilo = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {

                                if (vistaCliente.listaGUI.isSelectionEmpty()) {
                                    JOptionPane.showMessageDialog(null, "Seleccione un fichero.", "Error de descarga", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    try {

                                        ficheroSeleccionado = (String) vistaCliente.listaGUI.getSelectedValue();
                                        opcion = 1;
                                        salida.writeInt(opcion);
                                        vistaCliente.btnPausar.setEnabled(true);
//                                        vistaCliente.btnCancelar.setEnabled(true);
                                        descargar(ficheroSeleccionado);
                                    } catch (IOException ex) {
                                        JOptionPane.showMessageDialog(null, "Conexion perdida", "Error de conexion", JOptionPane.ERROR_MESSAGE);
                                        System.exit(0);
                                    }
                                }

                            }
                        });
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
//                            throw  new RuntimeException(ex);
                        } catch (ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        } finally {

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
                    vistaCliente.btnCancelar.setEnabled(false);
                    vistaCliente.btnPausar.setEnabled(true);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }

            case "Pausar": {
                try {
                    if (descarga != null && workerEnvio == null) {
                        // pausar descarga
                        descarga.pausarDescarga(true);
                    } else if (workerEnvio != null && descarga == null) {
                        // pausar subida
                        workerEnvio.pausarSubida(true);
                    }
                } catch (NullPointerException ex) {
                } catch (InterruptedException ex) {
                } finally {
                    try {
                        descarga.pausarDescarga(true);
                    } catch (InterruptedException ex) {

                    } catch (NullPointerException ex) {

                    }
                }
                break;
            }

            case "Reanudar": {
                try {
                    if (descarga != null && workerEnvio == null) {
                        // reanudar descarga
                        descarga.pausarDescarga(false);
                    } else if (workerEnvio != null && descarga == null) {
                        // reanudar subida
                        workerEnvio.pausarSubida(false);
                    }
                } catch (NullPointerException ex) {
                } catch (InterruptedException ex) {
                } finally {
                    try {
                        descarga.pausarDescarga(false);
                    } catch (InterruptedException ex) {
                    } catch (NullPointerException ex) {
                    }
                }
                break;
            }

            case "Cancelar": {
                descarga.cancel(true);
                int x = JOptionPane.showConfirmDialog(null, "Desea eliminar el archivo", "Aviso", JOptionPane.YES_NO_OPTION);
                if (x == JOptionPane.YES_NO_OPTION) {
                    vistaCliente.lblEstado.setText("Archivo eliminado");
                    descarga.eliminarArchivo();
                    descarga.cerrarSocket();
                }
                break;
            }
        }
    }

    /**
     * Descarga el fichero seleccionado en la vista del cliente.
     * Si no hay ningún fichero seleccionado, muestra un mensaje de error.
     * Crea una instancia de la clase WorkerDescarga y la ejecuta.
     */
    private void descargar(String seleccion) {
        seleccion = (String) vistaCliente.listaGUI.getSelectedValue();
        fichero = new File(seleccion);
        System.out.println("Fichero seleccionado " + seleccion + " " + getClass());
        descarga = new WorkerDescarga(entrada, salida, vistaCliente, seleccion, fichero, cliente);
        descarga.execute();
    }


    /**
     * Abre un diálogo de selección de archivo y envía el archivo seleccionado al servidor mediante un WorkerSubida.
     */
    private void subirFichero() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona el archivo a subir");
        int seleccion = fileChooser.showOpenDialog(null);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            fichero = fileChooser.getSelectedFile();
            workerEnvio = new WorkerSubida(salida, vistaCliente, fichero);
            workerEnvio.execute();

        }

    }

    /**
     * Método encargado de actualizar la lista de ficheros en la vista del cliente.
     * Recibe una lista de nombres de ficheros y actualiza el modelo del JList con ellos.
     *
     * @param lista la lista de nombres de ficheros a mostrar en la vista
     */
    private void listaFicheros(List<String> lista) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Estoy en el EDT: " + SwingUtilities.isEventDispatchThread() + " " + getClass());
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
            }
        });
    }

    /**
     * Inicia la conexión con el servidor a través del socket.
     * Luego habilita los botones una vez establecida la conexion con el socket y además le asigna un ID a cada
     * uno de los clientes.
     */
    public void conectar(String ip, int port) {
        try {
            cliente = new Socket(ip, port);
            System.out.println("Cliente iniciado " + cliente.getInetAddress());
            salida = new ObjectOutputStream(cliente.getOutputStream());
            entrada = new ObjectInputStream(cliente.getInputStream());
            String nombreCliente = (String) entrada.readUTF();
            System.out.println(nombreCliente);
            System.out.println("Se ha creado el flujo");

            asignarNombreCliente(nombreCliente);
            habilitarBotones();

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Imposible establecer conexión.", "Error de conexion", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Asigna un ID a cada cliente que se conecte
     * @param nombre, ID DEL CLIENTE
     */
    private void asignarNombreCliente(String nombre) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                vistaCliente.txfCliente.setText(nombre);
                System.out.println("Estoy en el EDT " + SwingUtilities.isEventDispatchThread());
            }
        });
    }

    /**
     * Método privado que habilita varios botones de la interfaz de usuario de la vista del cliente.
     * Este método utiliza la clase SwingUtilities para evitar bloquear la interfaz de usuario principal.
     */
    private void habilitarBotones() {
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
    }

}
