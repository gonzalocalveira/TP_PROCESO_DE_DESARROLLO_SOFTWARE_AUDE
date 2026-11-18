package ar.edu.uade.skillswap;

import ar.edu.uade.skillswap.controllers.UiControllers;
import ar.edu.uade.skillswap.dominio.comun.SkillSwapException;
import ar.edu.uade.skillswap.infraestructura.config.Configuracion;
import ar.edu.uade.skillswap.infraestructura.config.SkillSwapContexto;
import ar.edu.uade.skillswap.mock.MockSkillSwapControllers;
import ar.edu.uade.skillswap.ui.SkillSwapView;
import ar.edu.uade.skillswap.ui.Theme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Punto de entrada.
 *  - Sin argumentos: arranca con los controllers REALES (casos de uso + JPA/Hibernate + MySQL).
 *  - --mock: arranca con los controllers de ejemplo del kit (MockSkillSwapControllers).
 *  - --screenshot archivo.png: renderiza la pantalla de inicio en un PNG (sirve para probar sin ventana).
 * La vista SkillSwapView no se modificó: solo cambia la implementación de UiControllers que recibe.
 */
public final class SkillSwapApplication {
    private SkillSwapApplication() {}

    public static void main(String[] args) throws Exception {
        Theme.install();
        List<String> argumentos = Arrays.asList(args);
        instalarManejadorDeErrores();

        UiControllers controllers;
        String titulo;
        if (argumentos.contains("--mock")) {
            controllers = new MockSkillSwapControllers();
            titulo = "SkillSwap · Mock UI Kit";
        } else {
            SkillSwapContexto contexto;
            try {
                contexto = SkillSwapContexto.iniciar(Configuracion.cargar());
            } catch (RuntimeException e) {
                mostrarError("No se pudo iniciar SkillSwap", e.getMessage());
                throw e;
            }
            Runtime.getRuntime().addShutdownHook(new Thread(contexto::close));
            controllers = contexto.controllers();
            titulo = "SkillSwap · " + contexto.descripcionBase();
        }

        int i = argumentos.indexOf("--screenshot");
        if (i >= 0 && i + 1 < argumentos.size()) {
            SkillSwapView view = new SkillSwapView(controllers);
            view.setSize(1365, 768); view.doLayout(); layoutTree(view);
            BufferedImage image = new BufferedImage(1365,768,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);view.printAll(g);g.dispose();
            ImageIO.write(image,"png",new File(argumentos.get(i + 1)));
            System.exit(0);
            return;
        }
        UiControllers elegidos = controllers;
        SwingUtilities.invokeLater(() -> {
            JFrame frame=new JFrame(titulo);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(1120,700));frame.setSize(1365,820);frame.setLocationRelativeTo(null);
            frame.setContentPane(new SkillSwapView(elegidos));frame.setVisible(true);
        });
    }

    /**
     * La vista del kit no captura excepciones. Este manejador global muestra un diálogo con el
     * mensaje de negocio (ej: "Saldo insuficiente...") en lugar de dejar la app sin respuesta.
     */
    private static void instalarManejadorDeErrores() {
        Thread.setDefaultUncaughtExceptionHandler((hilo, e) -> {
            String mensaje = mensajeParaUsuario(e);
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println("[SkillSwap] " + mensaje);
                return;
            }
            SwingUtilities.invokeLater(() -> mostrarError("No se pudo completar la acción", mensaje));
        });
    }

    static String mensajeParaUsuario(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SkillSwapException negocio) {
                return negocio.getMessage();
            }
        }
        return "Ocurrió un error inesperado. Intentá nuevamente.";
    }

    private static void mostrarError(String titulo, String mensaje) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println(titulo + ": " + mensaje);
            return;
        }
        JOptionPane.showMessageDialog(null, mensaje, titulo, JOptionPane.WARNING_MESSAGE);
    }

    private static void layoutTree(Container c) {
        c.doLayout();
        for(Component x:c.getComponents()) if(x instanceof Container child) layoutTree(child);
    }
}
