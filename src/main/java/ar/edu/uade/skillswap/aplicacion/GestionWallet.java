package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.aplicacion.puertos.Comprobante;
import ar.edu.uade.skillswap.aplicacion.puertos.ProcesadorPagos;
import ar.edu.uade.skillswap.aplicacion.puertos.UnidadDeTrabajo;
import ar.edu.uade.skillswap.aplicacion.puertos.WalletRepository;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.Validar;
import ar.edu.uade.skillswap.dominio.evento.EventoDominio;
import ar.edu.uade.skillswap.dominio.evento.PublicadorEventos;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.wallet.MovimientoCredito;
import ar.edu.uade.skillswap.dominio.wallet.Wallet;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Caso de uso: saldo, movimientos y carga de créditos de demo. */
public class GestionWallet {

    private final WalletRepository wallets;
    private final ProcesadorPagos procesadorPagos;
    private final UnidadDeTrabajo tx;
    private final PublicadorEventos eventos;
    private final Clock reloj;
    private final int limiteCargaDemo;

    public GestionWallet(WalletRepository wallets, ProcesadorPagos procesadorPagos, UnidadDeTrabajo tx,
                         PublicadorEventos eventos, Clock reloj, int limiteCargaDemo) {
        this.wallets = wallets;
        this.procesadorPagos = procesadorPagos;
        this.tx = tx;
        this.eventos = eventos;
        this.reloj = reloj;
        this.limiteCargaDemo = limiteCargaDemo;
    }

    public SkillCoins saldo(String userId) {
        return tx.ejecutar(() -> wallets.obtener(userId).getSaldo());
    }

    /** Más recientes primero. */
    public List<MovimientoCredito> movimientos(String userId) {
        return tx.ejecutar(() -> {
            List<MovimientoCredito> lista = new ArrayList<>(wallets.obtener(userId).getMovimientos());
            Collections.reverse(lista);
            return lista;
        });
    }

    public SkillCoins acreditarDemo(String userId, int importe) {
        Validar.que(importe > 0 && importe <= limiteCargaDemo,
                "La carga de demo debe estar entre 1 y " + limiteCargaDemo + " SkillCoins.");
        SkillCoins monto = SkillCoins.de(importe);
        SkillCoins saldo = tx.ejecutar(() -> {
            Wallet w = wallets.obtener(userId);
            Comprobante comp = procesadorPagos.acreditar(userId, monto);   // PATRÓN ADAPTER
            w.acreditar(monto, "Créditos promocionales", comp.codigo(), LocalDateTime.now(reloj));
            wallets.guardar(w);
            return w.getSaldo();
        });
        eventos.publicar(new EventoDominio(TipoEvento.CREDITOS_ACREDITADOS, LocalDateTime.now(reloj), userId,
                List.of(userId), "Créditos acreditados", "Se acreditaron " + importe + " SkillCoins en tu cuenta."));
        return saldo;
    }
}
