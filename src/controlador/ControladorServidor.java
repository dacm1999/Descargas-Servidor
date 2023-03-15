package controlador;

import gui.VistaServidor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControladorServidor implements ActionListener {

    public VistaServidor vistaServidor;
    public ControladorServidor(VistaServidor vistaServidor){
        this.vistaServidor = vistaServidor;

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    private void vincularListeners(ActionListener listener) {
        vistaServidor.btnIniciar.addActionListener(listener);

    }
}
