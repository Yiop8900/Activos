package com.mycompany.activos.ui;

import com.mycompany.activos.model.Usuario;
import com.mycompany.activos.service.UsuarioService;
import com.mycompany.activos.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {

    private final JTextField txtUsuario = new JTextField(20);
    private final JPasswordField txtContrasena = new JPasswordField(20);
    private final JButton btnIngresar = new JButton("Ingresar");
    private final JLabel lblEstado = new JLabel(" ");

    public LoginFrame() {
        super("Inicio de Sesión");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitulo = new JLabel("Sistema de Activos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        // Usuario
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        panel.add(txtUsuario, gbc);

        // Contraseña
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        panel.add(txtContrasena, gbc);

        // Botón
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnIngresar, gbc);

        // Etiqueta de estado / error
        lblEstado.setForeground(Color.RED);
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        panel.add(lblEstado, gbc);

        add(panel);
        pack();
        setLocationRelativeTo(null);

        // Permitir Enter en el campo contraseña
        txtContrasena.addActionListener(e -> realizarLogin());
        btnIngresar.addActionListener(e -> realizarLogin());
    }

    private void realizarLogin() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            lblEstado.setText("Ingresa usuario y contraseña.");
            return;
        }

        btnIngresar.setEnabled(false);
        lblEstado.setForeground(Color.DARK_GRAY);
        lblEstado.setText("Verificando...");

        SwingWorker<Usuario, Void> worker = new SwingWorker<>() {
            @Override
            protected Usuario doInBackground() throws Exception {
                return new UsuarioService().login(usuario, contrasena);
            }

            @Override
            protected void done() {
                try {
                    Usuario u = get();
                    if (u != null) {
                        SessionManager.getInstance().setUsuarioActual(u);
                        dispose();
                        new MainFrame().setVisible(true);
                    } else {
                        lblEstado.setForeground(Color.RED);
                        lblEstado.setText("Usuario o contraseña incorrectos.");
                        btnIngresar.setEnabled(true);
                        txtContrasena.setText("");
                        txtContrasena.requestFocus();
                    }
                } catch (Exception ex) {
                    lblEstado.setForeground(Color.RED);
                    lblEstado.setText("Error de conexión: " + ex.getMessage());
                    btnIngresar.setEnabled(true);
                }
            }
        };

        worker.execute();
    }
}
