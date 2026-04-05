package com.mycompany.activos.ui;

import com.mycompany.activos.model.Cliente;
import com.mycompany.activos.service.ClienteService;
import com.mycompany.activos.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientePanel extends JPanel {

    private final ClienteService clienteService = new ClienteService();
    private final boolean soloLectura = !SessionManager.getInstance().esAdmin();

    private final String[] COLUMNAS = {"#", "Cliente", "Rut"};
    private final DefaultTableModel modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private List<Cliente> clientesActuales;

    public ClientePanel() {
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
        JButton btnRefrescar = new JButton("Refrescar");
        panelBotones.add(btnRefrescar);

        if (!soloLectura) {
            JButton btnAgregar  = new JButton("Agregar");
            JButton btnEditar   = new JButton("Editar");
            JButton btnEliminar = new JButton("Eliminar");
            panelBotones.add(btnAgregar);
            panelBotones.add(btnEditar);
            panelBotones.add(btnEliminar);
            btnAgregar.addActionListener(e  -> abrirDialogoAgregar());
            btnEditar.addActionListener(e   -> abrirDialogoEditar());
            btnEliminar.addActionListener(e -> eliminarSeleccionado());
        }

        add(panelBotones, BorderLayout.SOUTH);
        btnRefrescar.addActionListener(e -> cargarDatos());
    }

    private void cargarDatos() {
        SwingWorker<List<Cliente>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Cliente> doInBackground() throws Exception {
                return clienteService.listarClientes();
            }

            @Override
            protected void done() {
                try {
                    clientesActuales = get();
                    modeloTabla.setRowCount(0);
                    int idx = 1;
                    for (Cliente c : clientesActuales) {
                        modeloTabla.addRow(new Object[]{idx++, c.getCliente(), c.getRut()});
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ClientePanel.this,
                        "Error al cargar clientes:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void abrirDialogoAgregar() {
        ClienteDialog dialogo = new ClienteDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Agregar Cliente", null
        );
        dialogo.setVisible(true);
        if (dialogo.esConfirmado()) {
            Cliente nuevo = dialogo.getCliente();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    clienteService.agregarCliente(nuevo);
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get();
                        cargarDatos();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ClientePanel.this,
                            "Error al agregar cliente:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void abrirDialogoEditar() {
        int filaTabla = tabla.getSelectedRow();
        if (filaTabla < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente para editar.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Cliente actual = clientesActuales.get(filaTabla);
        ClienteDialog dialogo = new ClienteDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Editar Cliente", actual
        );
        dialogo.setVisible(true);
        if (dialogo.esConfirmado()) {
            Cliente editado = dialogo.getCliente();
            final int index = filaTabla;
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    clienteService.actualizarCliente(index, editado);
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get();
                        cargarDatos();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ClientePanel.this,
                            "Error al editar cliente:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void eliminarSeleccionado() {
        int filaTabla = tabla.getSelectedRow();
        if (filaTabla < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente para eliminar.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Cliente c = clientesActuales.get(filaTabla);
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Eliminar al cliente \"" + c.getCliente() + "\" (RUT: " + c.getRut() + ")?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        final int index = filaTabla;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                clienteService.eliminarCliente(index);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    cargarDatos();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ClientePanel.this,
                        "Error al eliminar cliente:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // -------------------------------------------------------------------------
    // Diálogo interno para agregar/editar cliente
    // -------------------------------------------------------------------------
    private static class ClienteDialog extends JDialog {

        private final JTextField txtCliente = new JTextField(25);
        private final JTextField txtRut = new JTextField(25);
        private boolean confirmado = false;

        ClienteDialog(Frame parent, String titulo, Cliente clienteExistente) {
            super(parent, titulo, true);
            if (clienteExistente != null) {
                txtCliente.setText(clienteExistente.getCliente());
                txtRut.setText(clienteExistente.getRut());
            }

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Cliente:"), gbc);
            gbc.gridx = 1; panel.add(txtCliente, gbc);

            gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("RUT:"), gbc);
            gbc.gridx = 1; panel.add(txtRut, gbc);

            JPanel panelBotones = new JPanel();
            JButton btnGuardar = new JButton("Guardar");
            JButton btnCancelar = new JButton("Cancelar");
            panelBotones.add(btnGuardar);
            panelBotones.add(btnCancelar);

            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            panel.add(panelBotones, gbc);

            add(panel);
            pack();
            setLocationRelativeTo(parent);
            setResizable(false);

            btnGuardar.addActionListener(e -> {
                if (txtCliente.getText().trim().isEmpty() || txtRut.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                confirmado = true;
                dispose();
            });
            btnCancelar.addActionListener(e -> dispose());
        }

        boolean esConfirmado() { return confirmado; }

        Cliente getCliente() {
            return new Cliente(txtCliente.getText().trim(), txtRut.getText().trim());
        }
    }
}
