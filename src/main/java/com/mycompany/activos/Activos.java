package com.mycompany.activos;

import com.mycompany.activos.ui.LoginFrame;

import javax.swing.*;

public class Activos {

    public static void main(String[] args) {
        // Aplicar Look & Feel Nimbus (más moderno que el estándar)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Si Nimbus no está disponible, usa el L&F por defecto del sistema
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
