package com.mycompany.activos.ui;

import com.mycompany.activos.model.Cliente;
import com.mycompany.activos.model.Solicitud;
import com.mycompany.activos.service.ClienteService;
import com.mycompany.activos.service.SolicitudService;
import com.mycompany.activos.service.UsuarioService;
import com.mycompany.activos.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SolicitudPanel extends JPanel {

    private final SolicitudService solicitudService = new SolicitudService();
    private final ClienteService   clienteService   = new ClienteService();
    private final UsuarioService   usuarioService   = new UsuarioService();

    private final boolean esAdmin      = SessionManager.getInstance().esAdmin();
    private final boolean esAnalista   = SessionManager.getInstance().esAnalista();
    private final boolean esSupervisor = SessionManager.getInstance().esSupervisor();

    private final String[] COLUMNAS = {
        "#", "Fecha Solicitud", "Cliente Solicitante", "Cargo", "Solicitudes",
        "Reclutados", "Validacion", "Citacion", "Asistencia", "Filtro Aprobados",
        "Seleccionados", "Estado", "Supervisor"
    };

    private final DefaultTableModel modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private List<Solicitud> solicitudesActuales;

    public SolicitudPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
        cargarDatos();
    }

    private void initUI() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(0).setMaxWidth(40);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnEditar    = new JButton("Editar");

        panelBotones.add(btnRefrescar);
        panelBotones.add(btnEditar);

        if (esAdmin) {
            JButton btnAgregar  = new JButton("Agregar");
            JButton btnEliminar = new JButton("Eliminar");
            panelBotones.add(btnAgregar);
            panelBotones.add(btnEliminar);
            btnAgregar.addActionListener(e  -> abrirDialogoAgregar());
            btnEliminar.addActionListener(e -> eliminarSeleccionado());
        }

        add(panelBotones, BorderLayout.SOUTH);

        btnRefrescar.addActionListener(e -> cargarDatos());
        btnEditar.addActionListener(e    -> abrirDialogoEditar());
    }

    private void cargarDatos() {
        SwingWorker<List<Solicitud>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Solicitud> doInBackground() throws Exception {
                return solicitudService.listarSolicitudes();
            }
            @Override
            protected void done() {
                try {
                    solicitudesActuales = get();
                    modeloTabla.setRowCount(0);
                    int idx = 1;
                    for (Solicitud s : solicitudesActuales) {
                        modeloTabla.addRow(new Object[]{
                            idx++,
                            s.getFechaSolicitud(), s.getClienteSolicitante(), s.getCargo(),
                            s.getSolicitudes(), s.getReclutados(), s.getValidacion(),
                            s.getCitacion(), s.getAsistencia(), s.getFiltroAprovados(),
                            s.getSeleccionados(), s.getEstado(), s.getSupervisor()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SolicitudPanel.this,
                        "Error al cargar solicitudes:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // Solo admin
    private void abrirDialogoAgregar() {
        // Carga clientes Y supervisores en paralelo antes de abrir el dialogo
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                List<Cliente> clientes     = clienteService.listarClientes();
                List<String>  supervisores = usuarioService.listarNombresSupervisores();
                return new Object[]{ clientes, supervisores };
            }
            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] res = get();
                    List<Cliente> clientes     = (List<Cliente>) res[0];
                    List<String>  supervisores = (List<String>)  res[1];

                    SolicitudDialog dialogo = new SolicitudDialog(
                        (Frame) SwingUtilities.getWindowAncestor(SolicitudPanel.this),
                        "Agregar Solicitud", null, clientes, supervisores
                    );
                    dialogo.setVisible(true);
                    if (dialogo.esConfirmado()) {
                        Solicitud nueva = dialogo.getSolicitud();
                        SwingWorker<Void, Void> sw = new SwingWorker<>() {
                            @Override protected Void doInBackground() throws Exception {
                                solicitudService.agregarSolicitud(nueva);
                                return null;
                            }
                            @Override protected void done() {
                                try { get(); cargarDatos(); }
                                catch (Exception ex) {
                                    JOptionPane.showMessageDialog(SolicitudPanel.this,
                                        "Error al agregar:\n" + ex.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        };
                        sw.execute();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SolicitudPanel.this,
                        "Error al cargar datos:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // Admin, Analista y Supervisor
    private void abrirDialogoEditar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una solicitud para editar.",
                "Sin seleccion", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Solicitud actual = solicitudesActuales.get(fila);

        if (esAdmin) {
            // Admin: carga clientes y supervisores, edita todos los campos
            SwingWorker<Object[], Void> worker = new SwingWorker<>() {
                @Override
                protected Object[] doInBackground() throws Exception {
                    List<Cliente> clientes     = clienteService.listarClientes();
                    List<String>  supervisores = usuarioService.listarNombresSupervisores();
                    return new Object[]{ clientes, supervisores };
                }
                @Override
                @SuppressWarnings("unchecked")
                protected void done() {
                    try {
                        Object[] res = get();
                        List<Cliente> clientes     = (List<Cliente>) res[0];
                        List<String>  supervisores = (List<String>)  res[1];

                        SolicitudDialog dialogo = new SolicitudDialog(
                            (Frame) SwingUtilities.getWindowAncestor(SolicitudPanel.this),
                            "Editar Solicitud", actual, clientes, supervisores
                        );
                        dialogo.setVisible(true);
                        if (dialogo.esConfirmado()) {
                            guardarEdicion(fila, dialogo.getSolicitud());
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(SolicitudPanel.this,
                            "Error al cargar datos:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();

        } else if (esAnalista) {
            // Analista: acumula Reclutados y Citacion
            AnalistaDialog dialogo = new AnalistaDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                actual
            );
            dialogo.setVisible(true);
            if (dialogo.esConfirmado()) {
                guardarEdicion(fila, dialogo.getSolicitudActualizada());
            }

        } else if (esSupervisor) {
            // Supervisor: acumula Asistencia, Filtro_Aprovados y Seleccionados
            SupervisorDialog dialogo = new SupervisorDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                actual
            );
            dialogo.setVisible(true);
            if (dialogo.esConfirmado()) {
                guardarEdicion(fila, dialogo.getSolicitudActualizada());
            }
        }
    }

    private void guardarEdicion(int fila, Solicitud editada) {
        SwingWorker<Void, Void> sw = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                solicitudService.actualizarSolicitud(fila, editada);
                return null;
            }
            @Override protected void done() {
                try { get(); cargarDatos(); }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(SolicitudPanel.this,
                        "Error al guardar:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        sw.execute();
    }

    // Solo admin
    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una solicitud para eliminar.",
                "Sin seleccion", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Solicitud s = solicitudesActuales.get(fila);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Eliminar la solicitud de \"" + s.getClienteSolicitante()
            + "\" - cargo: " + s.getCargo() + "?",
            "Confirmar eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        final int index = fila;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                solicitudService.eliminarSolicitud(index);
                return null;
            }
            @Override protected void done() {
                try { get(); cargarDatos(); }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(SolicitudPanel.this,
                        "Error al eliminar:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // -------------------------------------------------------------------------
    // Dialogo para ADMIN: edita todos los campos libremente
    // -------------------------------------------------------------------------
    private static class SolicitudDialog extends JDialog {

        private final JTextField        txtFecha       = new JTextField(20);
        private final JComboBox<String> cboCliente     = new JComboBox<>();
        private final JTextField        txtCargo       = new JTextField(20);
        private final JTextField        txtSolicitudes = new JTextField(10);
        private final JTextField        txtReclutados  = new JTextField(10);
        private final JTextField        txtValidacion  = new JTextField(10);
        private final JTextField        txtCitacion    = new JTextField(10);
        private final JTextField        txtAsistencia  = new JTextField(10);
        private final JTextField        txtFiltro      = new JTextField(10);
        private final JTextField        txtSelec       = new JTextField(10);
        private final JComboBox<String> cboEstado      = new JComboBox<>(new String[]{"Abierta", "Cerrada"});
        private final JComboBox<String> cboSupervisor  = new JComboBox<>();

        private boolean confirmado = false;

        SolicitudDialog(Frame parent, String titulo, Solicitud solicitudExistente,
                        List<Cliente> clientes, List<String> supervisores) {
            super(parent, titulo, true);

            for (Cliente c : clientes)    cboCliente.addItem(c.getCliente());
            cboSupervisor.addItem("");    // opcion vacia
            for (String sv : supervisores) cboSupervisor.addItem(sv);

            if (solicitudExistente != null) {
                txtFecha.setText(solicitudExistente.getFechaSolicitud());
                String cli = solicitudExistente.getClienteSolicitante();
                cboCliente.setSelectedItem(cli.isEmpty() ? null : cli);
                txtCargo.setText(solicitudExistente.getCargo());
                txtSolicitudes.setText(solicitudExistente.getSolicitudes());
                txtReclutados.setText(solicitudExistente.getReclutados());
                txtValidacion.setText(solicitudExistente.getValidacion());
                txtCitacion.setText(solicitudExistente.getCitacion());
                txtAsistencia.setText(solicitudExistente.getAsistencia());
                txtFiltro.setText(solicitudExistente.getFiltroAprovados());
                txtSelec.setText(solicitudExistente.getSeleccionados());
                String est = solicitudExistente.getEstado();
                cboEstado.setSelectedItem((est == null || est.isEmpty()) ? "Abierta" : est);
                String sup = solicitudExistente.getSupervisor();
                cboSupervisor.setSelectedItem((sup == null) ? "" : sup);
            } else {
                cboEstado.setSelectedItem("Abierta");
            }

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 5, 4, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;
            addFila(panel, gbc, row++, "Fecha Solicitud:",     txtFecha);
            addFila(panel, gbc, row++, "Cliente Solicitante:", cboCliente);
            addFila(panel, gbc, row++, "Cargo:",               txtCargo);
            addFila(panel, gbc, row++, "Solicitudes:",         txtSolicitudes);

            gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
            panel.add(new JSeparator(), gbc);
            gbc.gridwidth = 1;

            addFila(panel, gbc, row++, "Reclutados:",       txtReclutados);
            addFila(panel, gbc, row++, "Validacion:",       txtValidacion);
            addFila(panel, gbc, row++, "Citacion:",         txtCitacion);
            addFila(panel, gbc, row++, "Asistencia:",       txtAsistencia);
            addFila(panel, gbc, row++, "Filtro Aprobados:", txtFiltro);
            addFila(panel, gbc, row++, "Seleccionados:",    txtSelec);

            gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
            panel.add(new JSeparator(), gbc);
            gbc.gridwidth = 1;

            addFila(panel, gbc, row++, "Estado:",     cboEstado);
            addFila(panel, gbc, row++, "Supervisor:", cboSupervisor);

            JPanel panelBotones = new JPanel();
            JButton btnGuardar  = new JButton("Guardar");
            JButton btnCancelar = new JButton("Cancelar");
            panelBotones.add(btnGuardar);
            panelBotones.add(btnCancelar);

            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            panel.add(panelBotones, gbc);

            add(new JScrollPane(panel));
            setSize(440, 640);
            setLocationRelativeTo(parent);
            setResizable(false);

            btnGuardar.addActionListener(e -> {
                if (txtFecha.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "La fecha es obligatoria.",
                        "Validacion", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                confirmado = true;
                dispose();
            });
            btnCancelar.addActionListener(e -> dispose());
        }

        private void addFila(JPanel p, GridBagConstraints gbc, int row,
                             String label, JComponent campo) {
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
            p.add(new JLabel(label), gbc);
            gbc.gridx = 1;
            p.add(campo, gbc);
        }

        boolean esConfirmado() { return confirmado; }

        Solicitud getSolicitud() {
            String cliente = cboCliente.getSelectedItem() != null
                ? cboCliente.getSelectedItem().toString() : "";
            String estado = cboEstado.getSelectedItem() != null
                ? cboEstado.getSelectedItem().toString() : "Abierta";
            String supervisor = cboSupervisor.getSelectedItem() != null
                ? cboSupervisor.getSelectedItem().toString() : "";
            return new Solicitud(
                txtFecha.getText().trim(), cliente,
                txtCargo.getText().trim(), txtSolicitudes.getText().trim(),
                txtReclutados.getText().trim(), txtValidacion.getText().trim(),
                txtCitacion.getText().trim(), txtAsistencia.getText().trim(),
                txtFiltro.getText().trim(), txtSelec.getText().trim(),
                estado, supervisor
            );
        }
    }

    // -------------------------------------------------------------------------
    // Dialogo para ANALISTA: acumula Reclutados (E) y Citacion (G)
    // -------------------------------------------------------------------------
    private static class AnalistaDialog extends JDialog {

        private final Solicitud base;
        private final JTextField txtAgregarReclutados = new JTextField(10);
        private final JTextField txtAgregarCitacion   = new JTextField(10);
        private boolean confirmado = false;

        AnalistaDialog(Frame parent, Solicitud solicitud) {
            super(parent, "Actualizar Solicitud (Analista)", true);
            this.base = solicitud;

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            JLabel lblInfo = new JLabel("<html><b>Solicitud:</b> "
                + solicitud.getClienteSolicitante() + " \u2014 " + solicitud.getCargo() + "</html>");
            panel.add(lblInfo, gbc);
            gbc.gridwidth = 1;

            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            panel.add(new JSeparator(), gbc);
            gbc.gridwidth = 1;

            row++;
            addFila(panel, gbc, row++, "Reclutados actuales:",       etiquetaValor(solicitud.getReclutados()));
            addFila(panel, gbc, row++, "Sumar / Restar Reclutados:", txtAgregarReclutados);

            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            panel.add(new JSeparator(), gbc);
            gbc.gridwidth = 1;

            row++;
            addFila(panel, gbc, row++, "Citacion actual:",          etiquetaValor(solicitud.getCitacion()));
            addFila(panel, gbc, row++, "Sumar / Restar Citacion:",  txtAgregarCitacion);

            JPanel panelBotones = new JPanel();
            JButton btnGuardar  = new JButton("Guardar");
            JButton btnCancelar = new JButton("Cancelar");
            panelBotones.add(btnGuardar);
            panelBotones.add(btnCancelar);

            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            panel.add(panelBotones, gbc);

            add(panel);
            pack();
            setMinimumSize(new Dimension(380, 300));
            setLocationRelativeTo(parent);
            setResizable(false);

            btnGuardar.addActionListener(e -> {
                if (!esNumeroValido(txtAgregarReclutados.getText()) ||
                    !esNumeroValido(txtAgregarCitacion.getText())) {
                    JOptionPane.showMessageDialog(this,
                        "Ingresa un numero entero (positivo para sumar, negativo para restar).",
                        "Validacion", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int resRec = valorActual(base.getReclutados()) + parsearInt(txtAgregarReclutados.getText());
                int resCit = valorActual(base.getCitacion())   + parsearInt(txtAgregarCitacion.getText());
                if (resRec < 0 || resCit < 0) {
                    JOptionPane.showMessageDialog(this,
                        "El resultado no puede ser negativo.\n"
                        + "Reclutados resultante: " + resRec
                        + "  |  Citacion resultante: " + resCit,
                        "Validacion", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                confirmado = true;
                dispose();
            });
            btnCancelar.addActionListener(e -> dispose());
        }

        private static JLabel etiquetaValor(String valor) {
            String texto = (valor == null || valor.isBlank()) ? "0" : valor;
            JLabel lbl = new JLabel(texto);
            lbl.setFont(lbl.getFont().deriveFont(java.awt.Font.BOLD));
            lbl.setForeground(new Color(0, 80, 160));
            return lbl;
        }

        private static void addFila(JPanel p, GridBagConstraints gbc, int row,
                              String label, JComponent campo) {
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
            p.add(new JLabel(label), gbc);
            gbc.gridx = 1;
            p.add(campo, gbc);
        }

        private static boolean esNumeroValido(String texto) {
            if (texto == null || texto.isBlank()) return true;
            try { Integer.parseInt(texto.trim()); return true; }
            catch (NumberFormatException e) { return false; }
        }

        private static int parsearInt(String texto) {
            try { return Integer.parseInt(texto.trim()); }
            catch (Exception e) { return 0; }
        }

        private static int valorActual(String texto) {
            try { return Integer.parseInt(texto == null ? "0" : texto.trim()); }
            catch (Exception e) { return 0; }
        }

        boolean esConfirmado() { return confirmado; }

        Solicitud getSolicitudActualizada() {
            int nuevoReclutados = valorActual(base.getReclutados()) + parsearInt(txtAgregarReclutados.getText());
            int nuevoCitacion   = valorActual(base.getCitacion())   + parsearInt(txtAgregarCitacion.getText());
            return new Solicitud(
                base.getFechaSolicitud(), base.getClienteSolicitante(),
                base.getCargo(), base.getSolicitudes(),
                String.valueOf(nuevoReclutados),
                base.getValidacion(),
                String.valueOf(nuevoCitacion),
                base.getAsistencia(), base.getFiltroAprovados(), base.getSeleccionados(),
                base.getEstado(), base.getSupervisor()
            );
        }
    }

    // -------------------------------------------------------------------------
    // Dialogo para SUPERVISOR: acumula Asistencia (H), Filtro_Aprovados (I), Seleccionados (J)
    // -------------------------------------------------------------------------
    private static class SupervisorDialog extends JDialog {

        private final Solicitud base;
        private final JTextField txtAgregarAsistencia   = new JTextField(10);
        private final JTextField txtAgregarFiltro       = new JTextField(10);
        private final JTextField txtAgregarSeleccionados = new JTextField(10);
        private boolean confirmado = false;

        SupervisorDialog(Frame parent, Solicitud solicitud) {
            super(parent, "Actualizar Solicitud (Supervisor)", true);
            this.base = solicitud;

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            JLabel lblInfo = new JLabel("<html><b>Solicitud:</b> "
                + solicitud.getClienteSolicitante() + " \u2014 " + solicitud.getCargo() + "</html>");
            panel.add(lblInfo, gbc);
            gbc.gridwidth = 1;

            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            panel.add(new JSeparator(), gbc);
            gbc.gridwidth = 1;

            row++;
            addFila(panel, gbc, row++, "Asistencia actual:",          etiquetaValor(solicitud.getAsistencia()));
            addFila(panel, gbc, row++, "Sumar / Restar Asistencia:",  txtAgregarAsistencia);

            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            panel.add(new JSeparator(), gbc);
            gbc.gridwidth = 1;

            row++;
            addFila(panel, gbc, row++, "Filtro Aprobados actual:",        etiquetaValor(solicitud.getFiltroAprovados()));
            addFila(panel, gbc, row++, "Sumar / Restar Filtro Aprobados:", txtAgregarFiltro);

            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            panel.add(new JSeparator(), gbc);
            gbc.gridwidth = 1;

            row++;
            addFila(panel, gbc, row++, "Seleccionados actual:",          etiquetaValor(solicitud.getSeleccionados()));
            addFila(panel, gbc, row++, "Sumar / Restar Seleccionados:",  txtAgregarSeleccionados);

            JPanel panelBotones = new JPanel();
            JButton btnGuardar  = new JButton("Guardar");
            JButton btnCancelar = new JButton("Cancelar");
            panelBotones.add(btnGuardar);
            panelBotones.add(btnCancelar);

            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            panel.add(panelBotones, gbc);

            add(panel);
            pack();
            setMinimumSize(new Dimension(400, 360));
            setLocationRelativeTo(parent);
            setResizable(false);

            btnGuardar.addActionListener(e -> {
                if (!esNumeroValido(txtAgregarAsistencia.getText())    ||
                    !esNumeroValido(txtAgregarFiltro.getText())         ||
                    !esNumeroValido(txtAgregarSeleccionados.getText())) {
                    JOptionPane.showMessageDialog(this,
                        "Ingresa un numero entero (positivo para sumar, negativo para restar).",
                        "Validacion", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int resAsis  = valorActual(base.getAsistencia())     + parsearInt(txtAgregarAsistencia.getText());
                int resFilt  = valorActual(base.getFiltroAprovados()) + parsearInt(txtAgregarFiltro.getText());
                int resSel   = valorActual(base.getSeleccionados())   + parsearInt(txtAgregarSeleccionados.getText());
                if (resAsis < 0 || resFilt < 0 || resSel < 0) {
                    JOptionPane.showMessageDialog(this,
                        "El resultado no puede ser negativo.\n"
                        + "Asistencia: " + resAsis
                        + "  |  Filtro Aprobados: " + resFilt
                        + "  |  Seleccionados: " + resSel,
                        "Validacion", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                confirmado = true;
                dispose();
            });
            btnCancelar.addActionListener(e -> dispose());
        }

        private static JLabel etiquetaValor(String valor) {
            String texto = (valor == null || valor.isBlank()) ? "0" : valor;
            JLabel lbl = new JLabel(texto);
            lbl.setFont(lbl.getFont().deriveFont(java.awt.Font.BOLD));
            lbl.setForeground(new Color(0, 100, 0));
            return lbl;
        }

        private static void addFila(JPanel p, GridBagConstraints gbc, int row,
                              String label, JComponent campo) {
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
            p.add(new JLabel(label), gbc);
            gbc.gridx = 1;
            p.add(campo, gbc);
        }

        private static boolean esNumeroValido(String texto) {
            if (texto == null || texto.isBlank()) return true;
            try { Integer.parseInt(texto.trim()); return true; }
            catch (NumberFormatException e) { return false; }
        }

        private static int parsearInt(String texto) {
            try { return Integer.parseInt(texto.trim()); }
            catch (Exception e) { return 0; }
        }

        private static int valorActual(String texto) {
            try { return Integer.parseInt(texto == null ? "0" : texto.trim()); }
            catch (Exception e) { return 0; }
        }

        boolean esConfirmado() { return confirmado; }

        Solicitud getSolicitudActualizada() {
            int nuevoAsistencia = valorActual(base.getAsistencia())     + parsearInt(txtAgregarAsistencia.getText());
            int nuevoFiltro     = valorActual(base.getFiltroAprovados()) + parsearInt(txtAgregarFiltro.getText());
            int nuevoSelec      = valorActual(base.getSeleccionados())   + parsearInt(txtAgregarSeleccionados.getText());
            return new Solicitud(
                base.getFechaSolicitud(), base.getClienteSolicitante(),
                base.getCargo(), base.getSolicitudes(),
                base.getReclutados(), base.getValidacion(), base.getCitacion(),
                String.valueOf(nuevoAsistencia),
                String.valueOf(nuevoFiltro),
                String.valueOf(nuevoSelec),
                base.getEstado(), base.getSupervisor()
            );
        }
    }
}
