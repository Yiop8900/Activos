package com.mycompany.activos.ui;

import com.mycompany.activos.util.SessionManager;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Sistema de Activos");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 450));
        setLocationRelativeTo(null);

        // Título con usuario y rol
        String usuario = SessionManager.getInstance().getUsuarioActual().getUsuario();
        String rol = SessionManager.getInstance().getUsuarioActual().getRol();
        setTitle("Sistema de Activos  —  " + usuario + " (" + rol + ")");

        // Barra de menú
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");

        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar Sesión");
        JMenuItem itemSalir = new JMenuItem("Salir");

        menuArchivo.add(itemCerrarSesion);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);
        menuBar.add(menuArchivo);
        setJMenuBar(menuBar);

        // Pestañas
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clientes",     new ClientePanel());
        tabs.addTab("Solicitudes",  new SolicitudPanel());

        if (SessionManager.getInstance().esAdmin()) {
            tabs.addTab("Usuarios", new UsuarioPanel());
        }

        add(tabs, BorderLayout.CENTER);

        // Acciones de menú
        itemCerrarSesion.addActionListener(e -> cerrarSesion());
        itemSalir.addActionListener(e -> System.exit(0));
    }

    private void cerrarSesion() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Deseas cerrar sesión?", "Cerrar Sesión",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().cerrarSesion();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
