package com.mycompany.activos.ui;

import com.mycompany.activos.model.Usuario;
import com.mycompany.activos.service.UsuarioService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsuarioPanel extends JPanel {

    private final UsuarioService usuarioService = new UsuarioService();

    private final String[] COLUMNAS = {"#", "Usuario", "Contraseña", "Rol"};
    private final DefaultTableModel modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private List<Usuario> usuariosActuales;

    public UsuarioPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
        cargarDatos();
    }

    private void initUI() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(0).setMaxWidth(40);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnAgregar   = new JButton("Agregar");
        JButton btnEditar    = new JButton("Editar");
        JButton btnEliminar  = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnRefrescar);
        add(panelBotones, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> abrirDialogoAgregar());
        btnEditar.addActionListener(e -> abrirDialogoEditar());
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        btnRefrescar.addActionListener(e -> cargarDatos());
    }

    private void cargarDatos() {
        SwingWorker<List<Usuario>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Usuario> doInBackground() throws Exception {
                return usuarioService.listarUsuarios();
            }

            @Override
            protected void done() {
                try {
                    usuariosActuales = get();
                    modeloTabla.setRowCount(0);
                    int idx = 1;
                    for (Usuario u : usuariosActuales) {
                        modeloTabla.addRow(new Object[]{
                            idx++, u.getUsuario(), "••••••••", u.getRol()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UsuarioPanel.this,
                        "Error al cargar usuarios:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void abrirDialogoAgregar() {
        UsuarioDialog dialogo = new UsuarioDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Agregar Usuario", null
        );
        dialogo.setVisible(true);
        if (dialogo.esConfirmado()) {
            Usuario nuevo = dialogo.getUsuario();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override protected Void doInBackground() throws Exception {
                    usuarioService.agregarUsuario(nuevo); return null;
                }
                @Override protected void done() {
                    try { get(); cargarDatos(); } catch (Exception ex) {
                        JOptionPane.showMessageDialog(UsuarioPanel.this,
                            "Error al agregar usuario:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void abrirDialogoEditar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario para editar.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Usuario actual = usuariosActuales.get(fila);
        UsuarioDialog dialogo = new UsuarioDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Editar Usuario", actual
        );
        dialogo.setVisible(true);
        if (dialogo.esConfirmado()) {
            Usuario editado = dialogo.getUsuario();
            final int index = fila;
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override protected Void doInBackground() throws Exception {
                    usuarioService.actualizarUsuario(index, editado); return null;
                }
                @Override protected void done() {
                    try { get(); cargarDatos(); } catch (Exception ex) {
                        JOptionPane.showMessageDialog(UsuarioPanel.this,
                            "Error al editar usuario:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario para eliminar.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Usuario u = usuariosActuales.get(fila);
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Eliminar al usuario \"" + u.getUsuario() + "\" (Rol: " + u.getRol() + ")?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        final int index = fila;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                usuarioService.eliminarUsuario(index); return null;
            }
            @Override protected void done() {
                try { get(); cargarDatos(); } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UsuarioPanel.this,
                        "Error al eliminar usuario:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // -------------------------------------------------------------------------
    // Diálogo interno para agregar/editar usuario
    // -------------------------------------------------------------------------
    private static class UsuarioDialog extends JDialog {

        private static final String[] ROLES = {"admin", "reclutador"};

        private final JTextField txtUsuario = new JTextField(25);
        private final JPasswordField txtContrasena = new JPasswordField(25);
        private final JComboBox<String> cmbRol = new JComboBox<>(ROLES);
        private boolean confirmado = false;

        UsuarioDialog(Frame parent, String titulo, Usuario usuarioExistente) {
            super(parent, titulo, true);
            if (usuarioExistente != null) {
                txtUsuario.setText(usuarioExistente.getUsuario());
                txtContrasena.setText(usuarioExistente.getContrasena());
                cmbRol.setSelectedItem(usuarioExistente.getRol());
            }

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Usuario:"), gbc);
            gbc.gridx = 1; panel.add(txtUsuario, gbc);

            gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Contraseña:"), gbc);
            gbc.gridx = 1; panel.add(txtContrasena, gbc);

            gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Rol:"), gbc);
            gbc.gridx = 1; panel.add(cmbRol, gbc);

            JPanel panelBotones = new JPanel();
            JButton btnGuardar  = new JButton("Guardar");
            JButton btnCancelar = new JButton("Cancelar");
            panelBotones.add(btnGuardar);
            panelBotones.add(btnCancelar);

            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            panel.add(panelBotones, gbc);

            add(panel);
            pack();
            setLocationRelativeTo(parent);
            setResizable(false);

            btnGuardar.addActionListener(e -> {
                if (txtUsuario.getText().trim().isEmpty()
                        || new String(txtContrasena.getPassword()).trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Usuario y contraseña son obligatorios.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                confirmado = true;
                dispose();
            });
            btnCancelar.addActionListener(e -> dispose());
        }

        boolean esConfirmado() { return confirmado; }

        Usuario getUsuario() {
            return new Usuario(
                txtUsuario.getText().trim(),
                new String(txtContrasena.getPassword()).trim(),
                (String) cmbRol.getSelectedItem()
            );
        }
    }
}
